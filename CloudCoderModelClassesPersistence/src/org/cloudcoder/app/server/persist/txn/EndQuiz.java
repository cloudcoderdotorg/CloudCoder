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
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to end a {@link Quiz}.
 */
public class EndQuiz extends AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final Quiz quiz;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param quiz  the {@link Quiz} to end
	 * @param user  the {@link User} (instructor)
	 */
	public EndQuiz(Quiz quiz, User user) {
		this.quiz = quiz;
		this.user = user;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"update cc_quizzes as q" +
				"  join cc_course_registrations as cr on  cr.course_id = q.course_id" +
				"                                     and cr.section = q.section" +
				"                                     and cr.user_id = ?" +
				"                                     and q.problem_id = ?" +
				"                                     and q.section = ?" +
				"                                     and q.course_id = ?" +
				" set end_time = ?"
		);
		stmt.setInt(1, user.getId());
		stmt.setInt(2, quiz.getProblemId());
		stmt.setInt(3, quiz.getSection());
		stmt.setInt(4, quiz.getCourseId());
		long currentTime = System.currentTimeMillis();
		stmt.setLong(5, currentTime);
		
		int updateCount = stmt.executeUpdate();
		return updateCount > 0;
	}

	@Override
	public String getDescription() {
		return " ending quiz";
	}
}