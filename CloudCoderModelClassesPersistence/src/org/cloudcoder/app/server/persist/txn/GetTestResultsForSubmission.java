// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Get all {@link TestResult}s for a given submission.
 * The authenticated user must be an instructor in the course in
 * which the submission's {@link Problem} is defined.
 */
public class GetTestResultsForSubmission extends
		AbstractDatabaseRunnableNoAuthException<NamedTestResult[]> {
	private final SubmissionReceipt receipt;
	private final User authenticatedUser;
	private final Problem problem;

	/**
	 * Constructor.
	 * 
	 * @param receipt            the {@link SubmissionReceipt} to get the test results for
	 * @param authenticatedUser  the authenticated user, who must be an instructor in the course
	 * @param problem            the {@link Problem} the submission was for
	 */
	public GetTestResultsForSubmission(SubmissionReceipt receipt,
			User authenticatedUser, Problem problem) {
		this.receipt = receipt;
		this.authenticatedUser = authenticatedUser;
		this.problem = problem;
	}

	@Override
	public NamedTestResult[] run(Connection conn) throws SQLException {
		CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, problem.getCourseId(), authenticatedUser.getId(), this);
		
		// Get all test results
		PreparedStatement stmt = prepareStatement(
				conn,
				"select tr.*,  e.* from cc_test_results as tr, cc_submission_receipts as sr, cc_events as e " +
				" where e.id = ?" +
				"   and tr.submission_receipt_event_id = e.id " +
				"   and sr.event_id = e.id " +
				"   and (e.user_id = ? or ? = 1) " +
				"   and e.problem_id = ? " +
				" order by tr.id asc"
		);
		stmt.setInt(1, receipt.getEventId());
		stmt.setInt(2, authenticatedUser.getId());
		stmt.setInt(3, regList.isInstructor() ? 1 : 0);
		stmt.setInt(4, problem.getProblemId());
		
		// Get the test results (which we are assumed as stored in order by id)
		List<TestResult> testResults = new ArrayList<TestResult>();
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			TestResult testResult = new TestResult();
			DBUtil.loadModelObjectFields(testResult, TestResult.SCHEMA, resultSet);
			testResults.add(testResult);
		}
		
		// Get the test cases (so we can find out the test case names)
		List<TestCase> testCases = Queries.doGetTestCasesForProblem(conn, problem.getProblemId(), this);
		
		// Build the list of NamedTestResults
		NamedTestResult[] results = new NamedTestResult[testResults.size()];
		for (int i = 0; i < results.length; i++) {
			String testCaseName = (i < testCases.size() ? testCases.get(i).getTestCaseName() : ("t" + i));
			NamedTestResult namedTestResult = new NamedTestResult(testCaseName, testResults.get(i));
			results[i] = namedTestResult;
		}

		return results;
	}

	@Override
	public String getDescription() {
		return " getting test results for submission";
	}
}