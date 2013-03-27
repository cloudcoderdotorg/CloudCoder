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
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;

/**
 * Transaction to get a {@link RepoProblemAndTestCaseList} (a repository exercise)
 * given its hash code.
 */
public class GetRepoProblemAndTestCaseListGivenHash extends
		AbstractDatabaseRunnableNoAuthException<RepoProblemAndTestCaseList> {
	private final String hash;

	/**
	 * Constructor.
	 * 
	 * @param hash the hash code
	 */
	public GetRepoProblemAndTestCaseListGivenHash(String hash) {
		this.hash = hash;
	}

	@Override
	public RepoProblemAndTestCaseList run(Connection conn) throws SQLException {
		// Query to find the RepoProblem
		PreparedStatement findRepoProblem = prepareStatement(
				conn,
				"select * from " + RepoProblem.SCHEMA.getDbTableName() + " as rp " +
				" where rp." + RepoProblem.HASH.getName() + " = ?");
		findRepoProblem.setString(1, hash);
		
		ResultSet repoProblemRs = executeQuery(findRepoProblem);
		if (!repoProblemRs.next()) {
			return null;
		}
		
		RepoProblem repoProblem = new RepoProblem();
		Queries.loadGeneric(repoProblem, repoProblemRs, 1, RepoProblem.SCHEMA);
		
		RepoProblemAndTestCaseList result = new RepoProblemAndTestCaseList();
		result.setProblem(repoProblem);
		
		// Find all RepoTestCases associated with the RepoProblem
		Queries.doFindRepoTestCases(repoProblem, result, conn, this);
		
		return result;
	}

	@Override
	public String getDescription() {
		return " retrieving problem and test cases from the repository";
	}
}