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
 * Transaction to get the most popular tags for a repository exercise.
 */
public class GetRepoProblemTags extends
		AbstractDatabaseRunnableNoAuthException<List<RepoProblemTag>> {
	private final int repoProblemId;

	public GetRepoProblemTags(int repoProblemId) {
		this.repoProblemId = repoProblemId;
	}

	@Override
	public List<RepoProblemTag> run(Connection conn) throws SQLException {
		// Order the tags by decreasing order of popularity
		// and (secondarily) ascending name order.
		// Return at most 8 tags.
		PreparedStatement stmt = prepareStatement(
				conn,
				"select rpt.*, count(rpt." + RepoProblemTag.NAME.getName() + ") as count " +
				"  from " + RepoProblemTag.SCHEMA.getDbTableName() + " as rpt " +
				" where rpt." + RepoProblemTag.REPO_PROBLEM_ID.getName() + " = ? " +
				" group by rpt." + RepoProblemTag.NAME.getName() + " " +
				" order by count desc, rpt." + RepoProblemTag.NAME.getName() + " asc " +
				" limit 8"
				);
		stmt.setInt(1, repoProblemId);
		
		ResultSet resultSet = executeQuery(stmt);
		List<RepoProblemTag> result = new ArrayList<RepoProblemTag>();
		while (resultSet.next()) {
			RepoProblemTag tag = new RepoProblemTag();
			
			Queries.loadGeneric(tag, resultSet, 1, RepoProblemTag.SCHEMA);
			
			// Because these tags are aggregated from (potentially) multiple
			// records in the table, we set the user id to 0, so there is no
			// confusion over whether the tag is linked to a specific user.
			tag.setUserId(0);
			
			// Set the count (number of users who added this tag to this problem)
			tag.setCount(resultSet.getInt(RepoProblemTag.SCHEMA.getNumFields() + 1));
			
			result.add(tag);
		}
		
		return result;
	}

	@Override
	public String getDescription() {
		return " getting tags for repository exercise"; 
	}
}