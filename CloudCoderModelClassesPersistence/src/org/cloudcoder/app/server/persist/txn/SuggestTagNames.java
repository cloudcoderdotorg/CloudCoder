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
import org.cloudcoder.app.shared.model.RepoProblemTag;

/**
 * Transaction to suggest tag names for repository exercises based
 * on the names of existing tags.
 */
public class SuggestTagNames extends
		AbstractDatabaseRunnableNoAuthException<List<String>> {
	private final String term;

	public SuggestTagNames(String term) {
		this.term = term;
	}

	@Override
	public List<String> run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select distinct " + RepoProblemTag.NAME.getName() +
				"  from " + RepoProblemTag.SCHEMA.getDbTableName() +
				" where "+ RepoProblemTag.NAME.getName() + " like ? " +
				" order by "+ RepoProblemTag.NAME.getName() + " asc"
		);
		stmt.setString(1, term + "%");
		
		List<String> result = new ArrayList<String>();
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			result.add(resultSet.getString(1));
		}
		
		return result;
	}

	@Override
	public String getDescription() {
		return " suggesting tag names";
	}
}