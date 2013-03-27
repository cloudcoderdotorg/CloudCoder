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
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to find an in-progress {@link Quiz} for a given
 * problem and instructor.
 */
public class FindCurrentQuiz extends AbstractDatabaseRunnableNoAuthException<Quiz> {
	private final Problem problem;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param problem the {@link Problem}
	 * @param user    the {@link User} (instructor)
	 */
	public FindCurrentQuiz(Problem problem, User user) {
		this.problem = problem;
		this.user = user;
	}

	@Override
	public Quiz run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select q.* from cc_quizzes as q, cc_course_registrations as cr " +
				" where cr.user_id = ? " +
				"   and cr.course_id = ? " +
				"   and cr.registration_type >= ? " +
				"   and q.course_id = cr.course_id " +
				"   and q.section = cr.section " +
				"   and q.problem_id = ? " +
				"   and q.start_time <= ? " +
				"   and (q.end_time >= ? or q.end_time = 0)"
		);
		stmt.setInt(1, user.getId());
		stmt.setInt(2, problem.getCourseId());
		stmt.setInt(3, CourseRegistrationType.INSTRUCTOR.ordinal());
		stmt.setInt(4, problem.getProblemId());
		long currentTime = System.currentTimeMillis();
		stmt.setLong(5, currentTime);
		stmt.setLong(6, currentTime);
		
		ResultSet resultSet = executeQuery(stmt);
		if (!resultSet.next()) {
			return null;
		}
		
		Quiz quiz = new Quiz();
		DBUtil.loadModelObjectFields(quiz, Quiz.SCHEMA, resultSet);
		return quiz;
	}

	@Override
	public String getDescription() {
		return " finding current quiz for problem";
	}
}