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
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.StartedQuiz;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction for a student to start or continue work on a {@link Quiz}.
 */
public class StudentStartOrContinueQuiz extends AbstractDatabaseRunnableNoAuthException<StartedQuiz> {
	private final Quiz quiz;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param quiz the {@link Quiz}
	 * @param user the {@link User} (student)
	 */
	public StudentStartOrContinueQuiz(Quiz quiz, User user) {
		this.quiz = quiz;
		this.user = user;
	}

	@Override
	public StartedQuiz run(Connection conn) throws SQLException {
		PreparedStatement query = prepareStatement(
				conn,
				"select sq.* from cc_started_quizzes as sq, cc_quizzes as q " +
				" where sq.user_id = ? " +
				"   and sq.quiz_id = ? " +
				"   and q.id = sq.quiz_id " +
				"   and q.start_time <= ? " +
				"   and (q.end_time = 0 or q.end_time > ?)"
		);
		query.setInt(1, user.getId());
		query.setInt(2, quiz.getId());
		long currentTime = System.currentTimeMillis();
		query.setLong(3, currentTime);
		query.setLong(4, currentTime);
		
		StartedQuiz startedQuiz = new StartedQuiz();
		ResultSet queryResult = executeQuery(query);
		if (queryResult.next()) {
			// Found the StartedQuiz
			DBUtil.loadModelObjectFields(startedQuiz, StartedQuiz.SCHEMA, queryResult);
		} else {
			// StartedQuiz doesn't exist yet, so create it
			startedQuiz.setQuizId(quiz.getId());
			startedQuiz.setUserId(user.getId());
			startedQuiz.setStartTime(currentTime);
			DBUtil.storeModelObject(conn, startedQuiz);
		}
		
		return startedQuiz;
	}

	@Override
	public String getDescription() {
		return " checking for started quiz";
	}
}