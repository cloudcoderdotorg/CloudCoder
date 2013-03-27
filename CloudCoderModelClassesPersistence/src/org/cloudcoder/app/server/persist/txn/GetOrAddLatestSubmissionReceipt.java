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
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get the latest {@link SubmissionReceipt} for a [@link User}'s
 * work on a given {@link Problem}, or add one if the user has not started
 * the problem yet.
 */
public class GetOrAddLatestSubmissionReceipt extends
		AbstractDatabaseRunnableNoAuthException<SubmissionReceipt> {
	private final User user;
	private final Problem problem;

	/**
	 * Constructor.
	 * 
	 * @param user    the user
	 * @param problem the problem
	 */
	public GetOrAddLatestSubmissionReceipt(User user, Problem problem) {
		this.user = user;
		this.problem = problem;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
	 */
	@Override
	public SubmissionReceipt run(Connection conn) throws SQLException {
		// Get most recent submission receipt for user/problem
		PreparedStatement stmt = prepareStatement(
				conn,
				"select r.*, e.* from " + SubmissionReceipt.SCHEMA.getDbTableName() + " as r, " + Event.SCHEMA.getDbTableName() + " as e " +
				" where r.event_id = e.id " +
				"   and e.id = (select max(ee.id) from " + SubmissionReceipt.SCHEMA.getDbTableName() + " as rr, " + Event.SCHEMA.getDbTableName() + " as ee " +
				"                where rr.event_id = ee.id " +
				"                  and ee.problem_id = ? " +
				"                  and ee.user_id = ?)");
		stmt.setInt(1, problem.getProblemId());
		stmt.setInt(2, user.getId());
		
		ResultSet resultSet = executeQuery(stmt);
		if (resultSet.next()) {
			SubmissionReceipt submissionReceipt = Queries.loadSubmissionReceiptAndEvent(resultSet);
			return submissionReceipt;
		}
		
		// There is no submission receipt in the database yet, so add one
		// with status STARTED
		SubmissionStatus status = SubmissionStatus.STARTED;
		SubmissionReceipt receipt = SubmissionReceipt.create(user, problem, status, -1, 0, 0);
		Queries.doInsertSubmissionReceipt(receipt, new TestResult[0], conn, this);
		return receipt;
		
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "adding initial submission receipt if necessary";
	}
}