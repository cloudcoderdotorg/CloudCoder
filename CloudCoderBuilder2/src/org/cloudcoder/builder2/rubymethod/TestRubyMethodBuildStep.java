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

package org.cloudcoder.builder2.rubymethod;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.KillableTaskManager;
import org.cloudcoder.builder2.javasandbox.SandboxUtil;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.ArrayUtil;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build step to test a {@link ProblemType#RUBY_METHOD} submission.
 * Assumes that {@link AddRubyMethodScaffoldingBuildStep} has already
 * been run.  Produces an array of {@link TestResult} objects as
 * a result artifact, one per {@link TestCase}.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class TestRubyMethodBuildStep implements IBuildStep {
	private static Logger logger = LoggerFactory.getLogger(TestRubyMethodBuildStep.class);
	
	public static final long TIMEOUT_LIMIT = 5000;
	
	private static ScriptingContainer container;
	
	// Preload classes that will be needed to test the Ruby submission in the
	// IsolatedTask, and create the ScriptingContainer.
	static {
		new RubyTester();
		TestResultUtil.createResultForTimeout();
		container = new ScriptingContainer(LocalContextScope.CONCURRENT);
		container.runScriptlet("true");
	}

	@Override
	public void execute(BuilderSubmission submission) {
		ProgramSource[] programSourceList = submission.getArtifact(ProgramSource[].class);
		if (programSourceList == null) {
			throw new InternalBuilderException(this.getClass(), "No ProgramSource list");
		}
		
		if (programSourceList.length != 1) {
			throw new InternalBuilderException(this.getClass(), "Only one source file is expected");
		}
		final String testSource = programSourceList[0].getProgramText();

		// Get Problem
		final Problem problem = submission.getArtifact(Problem.class);
		if (problem == null) {
			throw new InternalBuilderException(this.getClass(), "No Problem");
		}
		
		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}
		
		// Compile the test scriptlet
		// TODO: do this in a sandbox?
		final Object receiver;
		try {
			System.out.println("Test source:");
			System.out.println(testSource);
			receiver = container.runScriptlet(testSource);
			System.out.println("Object returned by compilation: " + receiver);
		} catch (ParseFailedException e) {
			CompilerDiagnostic diag = createRubyCompilerDiagnostic(e);
			failedCompilation(submission, diag);
			return;
		} catch (RuntimeException e) {
			CompilerDiagnostic diag = new CompilerDiagnostic(1, 1, 1, 1, "Unexpected compilation error");
			failedCompilation(submission, diag);
			return;
		}
		
		// Create a RubyTester in an IsolatedTask for each TestCase
		List<IsolatedTask<TestResult>> tasks = new ArrayList<IsolatedTask<TestResult>>();
		for (final TestCase testCase : testCaseList) {
			IsolatedTask<TestResult> task = new IsolatedTask<TestResult>() {
				@Override
				public TestResult execute() throws Throwable {
					RubyTester tester = new RubyTester();
					return tester.execute(container, receiver, problem, testCase);
				}
			};
			tasks.add(task);
		}
		
		// Execute the tasks in a KillableTaskManager
		KillableTaskManager<TestResult> pool = new KillableTaskManager<TestResult>(
				tasks,
				TIMEOUT_LIMIT,
				new KillableTaskManager.TimeoutHandler<TestResult>() {
					@Override
					public TestResult handleTimeout() {
						return TestResultUtil.createResultForTimeout();
					}
				}
		);
		pool.setThreadNamePrefix("RubyTest_"); // enable Ruby-specific security manager rules
		pool.run();
		
		// merge outcomes with their buffered inputs for stdout/stderr
		List<TestResult> testResults = SandboxUtil.getStdoutStderr(pool);

		// Add array of TestResults as submission artifact
		submission.addArtifact(ArrayUtil.toArray(testResults, TestResult.class));
	}

	private void failedCompilation(BuilderSubmission submission,
			CompilerDiagnostic diag) {
		CompilationResult compres = new CompilationResult(CompilationOutcome.FAILURE);
		compres.setCompilerDiagnosticList(new CompilerDiagnostic[]{diag});
		submission.addArtifact(compres);
		submission.addArtifact(new TestResult[0]);
	}

	private CompilerDiagnostic createRubyCompilerDiagnostic(ParseFailedException e) {
		String msg = e.getMessage();
		
		try {
			int colon = msg.indexOf(':');
			if (colon >= 0) {
				int nextColon = msg.indexOf(':', colon + 1);
				if (nextColon >= 0) {
					int lineNumber = Integer.parseInt(msg.substring(colon+1, nextColon));
					
					// TODO
					// In the second/third lines of the message there is information
					// (source text and a line with a caret)
					// about where in the source line the error occurs.
					// Could use this to compute the column.
					// For now, just discard it.
					int nl = msg.indexOf('\n', nextColon + 1);
					String desc = (nl >= 0) ? msg.substring(nextColon+1, nl) : msg.substring(nextColon+1);
					
					return new CompilerDiagnostic(lineNumber, lineNumber, 1, 1, desc);
				}
			}
		} catch (NumberFormatException ex) {
			logger.error("Error converting Ruby error message to compiler diagnostic", ex);
		}
		
		return new CompilerDiagnostic(1, 1, 1, 1, "Unknown compilation error");
	}

}
