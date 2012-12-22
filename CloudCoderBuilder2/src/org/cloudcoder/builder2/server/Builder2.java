// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.builder2.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.model.Tester;
import org.cloudcoder.builder2.tester.TesterFactory;
import org.cloudcoder.builder2.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "Builder" process.  It runs separately from the CloudCoder server,
 * and waits to receive submissions (Problem and program text).
 * For each submission received, it compiles it, tests it,
 * and reports back a SubmissionResult.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class Builder2 implements Runnable {
	private static final Logger logger=LoggerFactory.getLogger(Builder2.class);

	private volatile boolean shutdownRequested;
	private volatile boolean working;
	private NoConnectTimer noConnectTimer;
	private WebappSocketFactory webappSocketFactory;
	private Map<Integer, Problem> problemIdToProblemMap;
	private Map<Integer, List<TestCase>> problemIdToTestCaseListMap;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	/**
	 * Constructor.
	 * 
	 * @param webappSocketFactory the {@link WebappSocketFactory} that will create socket
	 *                            connections to the webapp
	 */
	public Builder2(WebappSocketFactory webappSocketFactory) {
		this.shutdownRequested = false;
		this.noConnectTimer = new NoConnectTimer();
		this.webappSocketFactory = webappSocketFactory;
		this.problemIdToProblemMap = new HashMap<Integer, Problem>();
		this.problemIdToTestCaseListMap = new HashMap<Integer, List<TestCase>>();
	}

	public void run() {
		requestLoop:
			while (!shutdownRequested) {
				try {
					if (this.socket == null) {
						attemptToConnectToServer();
						continue requestLoop;
					}

					working = false;
					Integer problemId = safeReadObject();
					working = true;

					// The CloudCoder app may send us a negative problem id as
					// a keepalive signal.  We can just ignore these.
					if (problemId < 0) {
						//logger.debug("Received keepalive signal from CloudCoder app");
						continue requestLoop;
					}

					Problem problem = problemIdToProblemMap.get(problemId);
					List<TestCase> testCaseList = problemIdToTestCaseListMap.get(problemId);

					// let the server know whether or not we have this
					// problem cached
					out.writeObject((Boolean) (problem != null));
					out.flush();

					// if we don't have the problem, the server will
					// send it to us
					if (problem == null) {
						problem = safeReadObject();
						testCaseList = safeReadObject();
					}

					// read program text
					String programText = safeReadObject();

					// Test the submission!
					SubmissionResult result = testSubmission(problem, testCaseList, programText);

					// Send the SubmissionResult back to the webapp
					out.writeObject(result);
					out.flush();
				} catch (IOException e) {
					// Quite possibly, this is a routine shutdown of the CloudCoder server.
					// We'll try connecting again soon.
					logger.error("Error communicating with server");
					socket = null;
					in = null;
					out = null;
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Class not found reading message", e);
				}
			}
	}

	/**
	 * Test a submission.
	 * 
	 * @param problem       the {@link Problem}
	 * @param testCaseList  the list of {@link TestCase}s
	 * @param programText   the submitted program text
	 * @return a {@link SubmissionResult} for the submission
	 */
	private SubmissionResult testSubmission(Problem problem, List<TestCase> testCaseList, String programText) {
		SubmissionResult result;
		try {
			// Based on the ProblemType, find a Tester
			Tester tester = TesterFactory.getTester(problem.getProblemType());
			if (tester == null) {
				throw new InternalBuilderException(Builder2.class, problem.getProblemType() + " problems not supported yet");
			}

			// Create and populate a BuilderSubmission
			BuilderSubmission submission = new BuilderSubmission();
			submission.addArtifact(problem);
			submission.addArtifact(ArrayUtil.toArray(testCaseList, TestCase.class));
			submission.addArtifact(new ProgramSource[]{new ProgramSource(programText)});

			try {
				// Build and test
				tester.execute(submission);
				
				// Get the SubmissionResult
				result = submission.getArtifact(SubmissionResult.class);
				if (result == null) {
					throw new InternalBuilderException("Tester did not create a SubmissionResult");
				}
			} finally {
				// Clean up all temporary resources created during building/testing
				submission.executeAllCleanupActions();
			}
		} catch (Throwable e) {
			CompilationResult compres = new CompilationResult(CompilationOutcome.BUILDER_ERROR);
			logger.error("Internal error building and testing submission", e);
			result = new SubmissionResult(compres);
			result.setTestResults(new TestResult[0]);
		}
		
		logger.info("Sending SubmissionResult back to server");
		
		if (result.getTestResults() == null) {
			logger.error("Null TestResult - should not happen");
			result.setTestResults(new TestResult[0]);
		} else {
			logger.info("{} results", result.getTestResults().length);
		}
			
		return result;
	}

	private Socket createSecureSocket() throws IOException, GeneralSecurityException {
		return webappSocketFactory.connectToWebapp();
	}

	public void attemptToConnectToServer() {
		try {
			try {
				this.socket=createSecureSocket();
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
			this.in = new ObjectInputStream(socket.getInputStream());
			logger.info("Connected!");
			noConnectTimer.connected();
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// ClientCoder server may not be running right now...try again soon
			//logger.error("Cannot connect to CloudCoder server");
			noConnectTimer.notConnected();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ee) {
				// ignore
			}
		}
	}

	@SuppressWarnings("unchecked")
	private<E> E safeReadObject() throws IOException, ClassNotFoundException {
		Object o = in.readObject();
		if (o == null) {
			throw new IOException("Could not read!");
		}
		return (E) o;
	}

	public void shutdown() {
		shutdownRequested = true;
		if (working) {
			logger.warn("shutdown(): cannot close worker socket because working=true");
		} else {
			try {
				// Rude, but effective.
				Socket s = socket;
				if (s != null) {
					s.close();
				}
			} catch (IOException e) {
				logger.error("Unable to close client socket, but Builder is shutting down anyway",e);
			}
		}
	}

	/**
	 * A main method for running the Builder interactively (during development).
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Running the builder interactively (type \"shutdown\" to quit)");

		Builder2Daemon daemon = new Builder2Daemon();

		daemon.start("instance");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String command = reader.readLine();
			if (command == null || command.trim().equals("shutdown")) {
				break;
			}
		}

		daemon.shutdown();

		System.out.println("Builder exiting");
	}
}
