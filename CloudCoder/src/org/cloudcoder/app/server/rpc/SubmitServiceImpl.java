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

package org.cloudcoder.app.server.rpc;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.SubmitService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.submitsvc.DefaultSubmitService;
import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;
import org.cloudcoder.app.server.submitsvc.ISubmitService;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.IContainsEvent;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.QuizEndedException;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * RPC servlet to handle problem submissions.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class SubmitServiceImpl extends RemoteServiceServlet implements SubmitService {
	private static final long serialVersionUID = 1L;

	private static final Logger logger=LoggerFactory.getLogger(SubmitServiceImpl.class);

	@Override
	public void submit(int problemId, String programText) throws CloudCoderAuthenticationException, SubmissionException, QuizEndedException {
		// Make sure that client is authenticated and has permission to edit the given problem
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());

		HttpSession session = getThreadLocalRequest().getSession();

		// The Problem should be stored in the user's session
		Problem problem = (Problem) session.getAttribute(SessionAttributeKeys.PROBLEM_KEY);
		if (problem == null || problem.getProblemId() != problemId) {
			throw new CloudCoderAuthenticationException();
		}
		
		// If the user is working on this Problem as a Quiz,
		// check whether the quiz has ended
		Quiz quiz = (Quiz) session.getAttribute(SessionAttributeKeys.QUIZ_KEY);
		if (quiz != null) {
			// Reload the object from the database
			if (!Database.getInstance().reloadModelObject(quiz)) {
				logger.error("Could not reload Quiz object");
			} else {
				long currentTime = System.currentTimeMillis();
				if (quiz.getEndTime() != 0 && currentTime > quiz.getEndTime()) {
					// Quiz has ended
					throw new QuizEndedException();
				}
			}
		}

		// Insert a full-text change into the database.
		Change fullTextChange = new Change(
				ChangeType.FULL_TEXT,
				0, 0, 0, 0,
				System.currentTimeMillis(),
				user.getId(), problem.getProblemId(),
				programText);
		Database.getInstance().storeChanges(new Change[]{fullTextChange});
		
		// Get test cases.  (TODO: cache them?)
		List<TestCase> testCaseList = Database.getInstance().getTestCasesForProblem(problemId);
		
		ISubmitService submitService = DefaultSubmitService.getInstance();

		logger.info("Passing submission to submit service...");
		IFutureSubmissionResult future = submitService.submitAsync(problem, testCaseList, programText);
		
		// Put the full-text Change and IFutureSubmissionResult in the user's session.
		addSessionObjects(session, fullTextChange, future);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.SubmitService#checkSubmission()
	 */
	@Override
	public SubmissionResult checkSubmission() throws CloudCoderAuthenticationException, SubmissionException {
		// Make sure user is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());

		HttpSession session = getThreadLocalRequest().getSession();

		// The Problem should be stored in the user's session
		Problem problem = (Problem) session.getAttribute(SessionAttributeKeys.PROBLEM_KEY);
		if (problem == null) {
			throw new CloudCoderAuthenticationException();
		}
		
		// Retrieve session objects for submission
		IFutureSubmissionResult future =
				(IFutureSubmissionResult) session.getAttribute(SessionAttributeKeys.FUTURE_SUBMISSION_RESULT_KEY);
		Change fullTextChange =
				(Change) session.getAttribute(SessionAttributeKeys.FULL_TEXT_CHANGE_KEY);
		
		if (future == null) {
			throw new SubmissionException("No pending submission in session");
		}
		if (fullTextChange == null) {
			throw new SubmissionException("No full-text change for pending submission in session");
		}
		
		// See if the SubmissionResult is ready
		SubmissionResult result;
		try {
			result = future.poll();
		} catch (SubmissionException e) {
			// If poll() throws an exception, the submission completed
			// with an error, but it did complete, so clear the session objects.
			clearSessionObjects(session);
			throw e;
		}
		if (result == null) {
			// submission result not ready yet
			return null;
		}
		
		// We are just trusting that the submission result is for the
		// correct problem...

		// Add a SubmissionReceipt to the database
		SubmissionReceipt receipt = createSubmissionReceipt(fullTextChange, result, user, problem);
		Database.getInstance().insertSubmissionReceipt(receipt, result.getTestResults());
		
		int numResult=0;
		if (result!=null && result.getTestResults()!=null) {
		    numResult=result.getTestResults().length;
		}
		logger.info("Compilation "+result.getCompilationResult()+", received " +numResult+" TestResults");
		
		// Clear session objects for submission
		clearSessionObjects(session);
		
		return result;
	}

	private void addSessionObjects(HttpSession session, Change fullTextChange, IFutureSubmissionResult future) {
		session.setAttribute(SessionAttributeKeys.FUTURE_SUBMISSION_RESULT_KEY, future);
		session.setAttribute(SessionAttributeKeys.FULL_TEXT_CHANGE_KEY, fullTextChange);
	}

	private void clearSessionObjects(HttpSession session) {
		session.removeAttribute(SessionAttributeKeys.FUTURE_SUBMISSION_RESULT_KEY);
		session.removeAttribute(SessionAttributeKeys.FULL_TEXT_CHANGE_KEY);
	}

	private SubmissionReceipt createSubmissionReceipt(IContainsEvent mostRecentChange, SubmissionResult result, User user, Problem problem) {
		SubmissionStatus status = result.determineSubmissionStatus();

		SubmissionReceipt receipt = SubmissionReceipt.create(user, problem, status, mostRecentChange.getEventId(),
				result.getNumTestsAttempted(), result.getNumTestsPassed());
		return receipt;
	}
}
