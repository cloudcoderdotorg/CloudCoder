// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemSearchCriteria;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
import org.cloudcoder.app.shared.model.RepoProblemTag;

/**
 * Perform a search for {@link RepoProblem}s matching specified criteria.
 * 
 * @author David Hovemeyer
 */
public class RepoProblemSearch {
	private RepoProblemSearchCriteria searchCriteria;
	private List<RepoProblemSearchResult> searchResultList;

	/**
	 * Constructor.
	 */
	public RepoProblemSearch() {
		this.searchResultList = new ArrayList<RepoProblemSearchResult>();
	}

	/**
	 * Set the search criteria.
	 * 
	 * @param searchCriteria the {@link RepoProblemSearchCriteria}
	 */
	public void setSearchCriteria(RepoProblemSearchCriteria searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	/**
	 * Execute the search.
	 * 
	 * @param conn        the database connection
	 * @param dbRunnable  the {@link AbstractDatabaseRunnableNoAuthException} to use
	 *                    to keep track of database resources
	 * @throws SQLException
	 */
	public void execute(Connection conn, AbstractDatabaseRunnableNoAuthException<?> dbRunnable) throws SQLException {
		// Special case: if no tags are specified,
		// then the search criteria are invalid and we return no results.
		// The client Javascript should refuse to submit such searches
		// (and inform the user of the error).
		if (searchCriteria.isEmpty()) {
			return;
		}

		// Search by tags and (maybe) language
		StringBuilder sql = new StringBuilder()
		.append("select rp.*, rpt.* ")
		.append("  from ")
		.append(RepoProblem.SCHEMA.getDbTableName())
		.append(" as rp, ")
		.append(RepoProblemTag.SCHEMA.getDbTableName())
		.append(" as rpt ")
		.append(" where rp.id = rpt.repo_problem_id and (");

		for (int i = 0; i < searchCriteria.getTagList().size(); i++) {
			if (i > 0) {
				sql.append(" or ");
			}
			sql.append("rpt.name = ?");
		}

		sql.append(")");

		sql.append(" order by rp." + RepoProblem.ID.getName() + " asc");

		String query = sql.toString();
		System.out.println("query: " + query);

		PreparedStatement stmt = dbRunnable.prepareStatement(
				conn,
				query
				);

		int index = 1;
		for (String tag : searchCriteria.getTagList()) {
			stmt.setString(index++, tag);
		}

		ResultSet resultSet = dbRunnable.executeQuery(stmt);

		RepoProblemSearchResult curSearchResult = null;

		while (resultSet.next()) {
			RepoProblem repoProblem = new RepoProblem();
			DBUtil.loadModelObjectFields(repoProblem, RepoProblem.SCHEMA, resultSet);

			if (curSearchResult == null || curSearchResult.getRepoProblem().getId() != repoProblem.getId()) {
				curSearchResult = new RepoProblemSearchResult();
				curSearchResult.setRepoProblem(repoProblem);
				searchResultList.add(curSearchResult);
			}

			RepoProblemTag repoProblemTag = new RepoProblemTag();
			DBUtil.loadModelObjectFields(repoProblemTag, RepoProblemTag.SCHEMA, resultSet, RepoProblem.SCHEMA.getNumFields() + 1);

			curSearchResult.addMatchedTag(repoProblemTag.getName());
		}
	}

	/**
	 * Get list of {@link RepoProblemSearchResult}s.
	 * 
	 * @return list of {@link RepoProblemSearchResult}s
	 */
	public List<RepoProblemSearchResult> getSearchResultList() {
		return searchResultList;
	}
}
