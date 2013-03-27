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
import org.cloudcoder.app.shared.model.Problem;

/**
 * Transaction to get {@link Problem} given its unique id.
 */
public class GetProblemForProblemId extends AbstractDatabaseRunnableNoAuthException<Problem> {
	private final int problemId;

	/**
	 * Constructor.
	 * 
	 * @param problemId the unique id of the problem to get
	 */
	public GetProblemForProblemId(int problemId) {
		this.problemId = problemId;
	}

	@Override
	public Problem run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select * from " + Problem.SCHEMA.getDbTableName() + " where problem_id = ?");
		stmt.setInt(1, problemId);
		
		ResultSet resultSet = executeQuery(stmt);
		if (resultSet.next()) {
			Problem problem = new Problem();
			Queries.loadGeneric(problem, resultSet, 1, Problem.SCHEMA);
			return problem;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "get problem";
	}
}