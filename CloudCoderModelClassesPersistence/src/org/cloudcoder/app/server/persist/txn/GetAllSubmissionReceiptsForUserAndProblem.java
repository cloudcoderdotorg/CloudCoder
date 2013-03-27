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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get all submission receipts for a {@link User}'s work on
 * a given {@link Problem}.
 */
public class GetAllSubmissionReceiptsForUserAndProblem extends AbstractDatabaseRunnableNoAuthException<SubmissionReceipt[]> {
	private final Problem problem;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param problem  the {@link Problem}
	 * @param user     the {@link User}
	 */
	public GetAllSubmissionReceiptsForUserAndProblem(Problem problem, User user) {
		this.problem = problem;
		this.user = user;
	}

	@Override
	public SubmissionReceipt[] run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select sr.*, e.* from cc_submission_receipts as sr, cc_events as e " +
				"  where sr.event_id = e.id " +
				"    and e.user_id = ? " +
				"    and e.problem_id = ? " +
				" order by e.timestamp asc"
		);
		stmt.setInt(1, user.getId());
		stmt.setInt(2, problem.getProblemId());
		
		ArrayList<SubmissionReceipt> result = new ArrayList<SubmissionReceipt>();
		
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			int index = 1;
			SubmissionReceipt receipt = new SubmissionReceipt();
			index = DBUtil.loadModelObjectFields(receipt, SubmissionReceipt.SCHEMA, resultSet, index);
			Event event = new Event();
			index = DBUtil.loadModelObjectFields(event, Event.SCHEMA, resultSet, index);
			
			receipt.setEvent(event);
			
			result.add(receipt);
		}
		
		return result.toArray(new SubmissionReceipt[result.size()]);
	}

	@Override
	public String getDescription() {
		return " getting subscription receipts for user";
	}
}