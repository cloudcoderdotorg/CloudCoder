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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.StartedQuiz;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to find an unfinished quiz for a student, if any exists.
 */
public class FindUnfinishedQuizForStudent extends AbstractDatabaseRunnableNoAuthException<StartedQuiz> {
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param user the {@link User} (student)
	 */
	public FindUnfinishedQuizForStudent(User user) {
		this.user = user;
	}

	@Override
	public StartedQuiz run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select sq.* from cc_started_quizzes as sq, cc_quizzes as q " +
				" where sq.quiz_id = q.id " +
				"   and sq.user_id = ? " +
				"   and q.start_time < ? " +
				"   and (q.end_time = 0 or q.end_time > ?)"
		);
		stmt.setInt(1, user.getId());
		
		long currentTime = System.currentTimeMillis();
		stmt.setLong(2, currentTime);
		stmt.setLong(3, currentTime);
		
		ResultSet resultSet = executeQuery(stmt);
		StartedQuiz result = null;
		if (resultSet.next()) {
			result = new StartedQuiz();
			DBUtil.loadModelObjectFields(result, StartedQuiz.SCHEMA, resultSet);
			return result;
		}
		
		return result;
	}

	@Override
	public String getDescription() {
		return " finding unfinished quiz for user";
	}
}