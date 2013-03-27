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
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.User;

/**
 * Retrieve all {@link Change}s for given {@link User} and {@link Problem}
 * newer than a particular base revision (change event id).
 */
public class GetAllChangesNewerThan extends AbstractDatabaseRunnableNoAuthException<List<Change>> {
	private final int problemId;
	private final User user;
	private final int baseRev;

	/**
	 * Constructor.
	 * 
	 * @param problemId the {@link Problem} id
	 * @param user      the {@link User}
	 * @param baseRev   the base revision (change event id)
	 */
	public GetAllChangesNewerThan(int problemId, User user, int baseRev) {
		this.problemId = problemId;
		this.user = user;
		this.baseRev = baseRev;
	}

	@Override
	public List<Change> run(Connection conn) throws SQLException {
		List<Change> result = new ArrayList<Change>();
		
		PreparedStatement stmt = prepareStatement(
				conn,
				"select c.* from " + Change.SCHEMA.getDbTableName() + " as c, " + Event.SCHEMA.getDbTableName() + " as e " +
				" where c.event_id = e.id " +
				"   and e.id > ? " +
				"   and e.user_id = ? " +
				"   and e.problem_id = ? " +
				" order by e.id asc"
		);
		stmt.setInt(1, baseRev);
		stmt.setInt(2, user.getId());
		stmt.setInt(3, problemId);
		
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			Change change = new Change();
			Queries.load(change, resultSet, 1);
			result.add(change);
		}
		
		return result;
	}

	@Override
	public String getDescription() {
		return " retrieving most recent text changes";
	}
}