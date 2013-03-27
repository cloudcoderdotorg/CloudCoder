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
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserAndSubmissionReceipt;

/**
 * Transaction to find the best submission receipts for each student
 * for a given problem, checking to ensure that the authenicated user
 * is an instructor in the course in which the problem is assigned.
 */
public class GetBestSubmissionReceiptsForProblemForAuthenticatedUser
		extends AbstractDatabaseRunnableNoAuthException<List<UserAndSubmissionReceipt>> {
	private final int section;
	private final Problem problem;
	private final User authenticatedUser;

	/**
	 * Constructor.
	 * 
	 * @param section            the course section (0 for all sections)
	 * @param problem            the {@link Problem}
	 * @param authenticatedUser  the authenticated {@link User}, who should be an
	 *                           instructor in the course
	 */
	public GetBestSubmissionReceiptsForProblemForAuthenticatedUser(
			int section, Problem problem, User authenticatedUser) {
		this.section = section;
		this.problem = problem;
		this.authenticatedUser = authenticatedUser;
	}

	@Override
	public List<UserAndSubmissionReceipt> run(Connection conn) throws SQLException {
		CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, problem.getCourseId(), authenticatedUser.getId(), this);
		if (!regList.isInstructor()) {
			// user is not an instructor
			return new ArrayList<UserAndSubmissionReceipt>();
		}

		return Queries.doGetBestSubmissionReceipts(conn, problem, section, this);
	}

	@Override
	public String getDescription() {
		return " getting best submission receipts for problem";
	}
}