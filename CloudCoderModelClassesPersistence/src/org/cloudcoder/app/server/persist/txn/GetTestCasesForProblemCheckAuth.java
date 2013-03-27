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
import java.sql.SQLException;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get all {@link TestCase}s for a {@link Problem},
 * and (optionally) checking that the authenticated {@link User} is authorized to see them
 * (by being an instructor in the course in which the problem
 * is assigned.)
 */
public class GetTestCasesForProblemCheckAuth extends AbstractDatabaseRunnableNoAuthException<TestCase[]> {
	private final User authenticatedUser;
	private final boolean requireInstructor;
	private final int problemId;

	/**
	 * Constructor.
	 * 
	 * @param authenticatedUser the authenticated {@link User}
	 * @param requireInstructor true if the test cases should only be returned if the
	 *                          authenticated user is an instructor
	 * @param problemId         unique id of the {@link Problem}
	 */
	public GetTestCasesForProblemCheckAuth(User authenticatedUser,
			boolean requireInstructor, int problemId) {
		this.authenticatedUser = authenticatedUser;
		this.requireInstructor = requireInstructor;
		this.problemId = problemId;
	}

	@Override
	public TestCase[] run(Connection conn) throws SQLException {
		// Find the problem
		Problem problem = new Problem();
		problem.setProblemId(problemId);
		DBUtil.loadModelObject(conn, problem);
		
		// Check user's registration in the course
		CourseRegistrationList regList = Queries.doGetCourseRegistrations(
				conn, problem.getCourseId(), authenticatedUser.getId(), this);
		
		if (regList.getList().isEmpty()) {
			// Not registered, deny
			return new TestCase[0];
		}

		if (requireInstructor && !regList.isInstructor()) {
			// Authenticated user is not an instructor
			return new TestCase[0];
		}
		
		// Allow the request, so go ahead and return the test cases
		List<TestCase> result = Queries.doGetTestCasesForProblem(conn, problemId, this);
		return result.toArray(new TestCase[result.size()]);
	}

	@Override
	public String getDescription() {
		return " getting test cases for problem";
	}
}