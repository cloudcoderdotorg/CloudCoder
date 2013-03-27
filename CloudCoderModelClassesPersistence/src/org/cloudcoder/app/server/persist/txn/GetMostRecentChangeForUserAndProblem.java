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
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get the most recent {@link Change} recording a {@link User}'s
 * work on a {@link Problem}. 
 */
public class GetMostRecentChangeForUserAndProblem extends AbstractDatabaseRunnableNoAuthException<Change> {
	private final int problemId;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param problemId the unique id of the problem
	 * @param user      the {@link User}
	 */
	public GetMostRecentChangeForUserAndProblem(int problemId, User user) {
		this.problemId = problemId;
		this.user = user;
	}

	@Override
	public Change run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select c.* from " + Change.SCHEMA.getDbTableName() + " as c, " + Event.SCHEMA.getDbTableName() + " as e " +
				" where c.event_id = e.id " +
				"   and e.id = (select max(ee.id) from " + Change.SCHEMA.getDbTableName() + " as cc, " + Event.SCHEMA.getDbTableName() + " as ee " +
				"                where cc.event_id = ee.id " +
				"                  and ee.problem_id = ? " +
				"                  and ee.user_id = ?)"
		);
		stmt.setInt(1, problemId);
		stmt.setInt(2, user.getId());
		
		ResultSet resultSet = executeQuery(stmt);
		if (!resultSet.next()) {
			return null;
		}
		
		Change change = new Change();
		Queries.load(change, resultSet, 1);
		return change;
	}

	public String getDescription() {
		return "retrieving latest code change";
	}
}