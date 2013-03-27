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
import java.sql.SQLException;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * Transaction to add {@link TestCase}s to a {@link Problem}.
 */
public class AddTestCasesToProblem extends
		AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final Problem problem;
	private final List<TestCase> testCaseList;

	/**
	 * Constructor.
	 * 
	 * @param problem      the {@link Problem}
	 * @param testCaseList the {@link TestCase}s to add to the problem
	 */
	public AddTestCasesToProblem(Problem problem,
			List<TestCase> testCaseList) {
		this.problem = problem;
		this.testCaseList = testCaseList;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		AbstractDatabaseRunnableNoAuthException<?> databaseRunnable = this;
		
		return Queries.doInsertTestCases(problem, testCaseList, conn,
				databaseRunnable);
	}

	@Override
	public String getDescription() {
		return "adding test case";
	}
}