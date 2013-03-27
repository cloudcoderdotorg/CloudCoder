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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get the submission text given a {@link SubmissionReceipt},
 * the {@link Problem} the submission is for, and the {@link User} who
 * submitted it.  The authenticated user must either be an instructor in the course,
 * or must be the same as the submitter.
 */
public class GetSubmissionText extends AbstractDatabaseRunnableNoAuthException<ProblemText> {
	private final SubmissionReceipt receipt;
	private final User authenticatedUser;
	private final Problem problem;
	private final User submitter;

	/**
	 * Constructor.
	 * 
	 * @param receipt            the {@link SubmissionReceipt}
	 * @param authenticatedUser  the authenticated {@link User}
	 * @param problem            the {@link Problem} the submission is for
	 * @param submitter          the {@link User} who made the submission
	 */
	public GetSubmissionText(SubmissionReceipt receipt,
			User authenticatedUser, Problem problem, User submitter) {
		this.receipt = receipt;
		this.authenticatedUser = authenticatedUser;
		this.problem = problem;
		this.submitter = submitter;
	}

	@Override
	public ProblemText run(Connection conn) throws SQLException {
		// Check authenticated user's course registrations
		CourseRegistrationList regList =
				Queries.doGetCourseRegistrations(conn, problem.getCourseId(), authenticatedUser.getId(), this);

		// Note that the queries require that either
		//   (1) the authenticated user is the submitter of the change event
		//       specified in the submission receipt as the last edit, or
		//   (2) the authenticated user is an instructor in the course
		
		// There are two cases:
		//   - the normal case where we know the event if of the full-text
		//     change containing the submission text
		//   - the case where the submission receipt is the initial one
		//     where the status is SubmissionStatus.STARTED, in which case
		//     we look for the user's first full-text submission
		//     (which the client webapp will typically create using the
		//     problem's skeleton code)

		ProblemText result;
		
		if (receipt.getLastEditEventId() < 0) {
			// Don't know the submission receipt: look for the first full-text change
			// for the user/problem
			PreparedStatement stmt = prepareStatement(
					conn,
					"select oc.text from cc_changes as oc " +
					" where oc.event_id = " +
					"   (select min(e.id) from cc_changes as c, cc_events as e " +
					"     where c.event_id = e.id " +
					"       and e.user_id = ? " +
					"       and e.problem_id = ? " +
					"       and c.type = ? " +
					"       and (e.user_id = ? or ? = 1))"
			);
			stmt.setInt(1, submitter.getId());
			stmt.setInt(2, problem.getProblemId());
			stmt.setInt(3, ChangeType.FULL_TEXT.ordinal());
			stmt.setInt(4, authenticatedUser.getId());
			stmt.setInt(5, regList.isInstructor() ? 1 : 0);
			
			ResultSet resultSet = executeQuery(stmt);
			if (resultSet.next()) {
				result = new ProblemText(resultSet.getString(1), false);
			} else {
				result = new ProblemText("", false);
			}
		} else {
			// We have the event id of the full-text change, so just
			// find it.
			PreparedStatement stmt = prepareStatement(
					conn,
					"select c.text from cc_changes as c, cc_events as e " +
					" where c.event_id = e.id " +
					"   and c.event_id = ? " +
					"   and (e.user_id = ? or ? = 1) " +
					"   and e.problem_id = ?"
			);
			stmt.setInt(1, receipt.getLastEditEventId());
			stmt.setInt(2, authenticatedUser.getId());
			stmt.setInt(3, regList.isInstructor() ? 1 : 0);
			stmt.setInt(4, problem.getProblemId());
			
			ResultSet resultSet = executeQuery(stmt);
			if (resultSet.next()) {
				// Got it
				result = new ProblemText(resultSet.getString(1), false);
			} else {
				// No such edit event, or user is not authorized to see it
				result = new ProblemText("", false);
			}
		}
		
		return result;
	}

	@Override
	public String getDescription() {
		return " getting submission text";
	}
}