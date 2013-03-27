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

/**
 * Get a {@link Change} given its event id.
 */
public class GetChangeGivenChangeEventId extends AbstractDatabaseRunnableNoAuthException<Change> {
	private final int changeEventId;

	/**
	 * Constructor.
	 * 
	 * @param changeEventId the event id of the change to get
	 */
	public GetChangeGivenChangeEventId(int changeEventId) {
		this.changeEventId = changeEventId;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
	 */
	@Override
	public Change run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select ch.*, e.* " +
				"  from " + Change.SCHEMA.getDbTableName() + " as ch, " + Event.SCHEMA.getDbTableName() + " as e " +
				" where e.id = ? and ch.event_id = e.id");
		stmt.setInt(1, changeEventId);
		
		ResultSet resultSet = executeQuery(stmt);
		if (resultSet.next()) {
			Change change = Queries.getChangeAndEvent(resultSet);
			return change;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "get text change";
	}
}