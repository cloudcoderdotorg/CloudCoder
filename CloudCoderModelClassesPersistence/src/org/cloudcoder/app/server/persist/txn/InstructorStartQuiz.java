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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction for an instructor to start a quiz for a given problem/course/section.
 */
public class InstructorStartQuiz extends AbstractDatabaseRunnable<Quiz> {
	private final int section;
	private final User user;
	private final Problem problem;

	/**
	 * Constructor
	 * 
	 * @param section  the section number
	 * @param user     the {@link User} (instructor)
	 * @param problem  the {@link Problem} to be given as a quiz
	 */
	public InstructorStartQuiz(int section, User user, Problem problem) {
		this.section = section;
		this.user = user;
		this.problem = problem;
	}

	@Override
	public Quiz run(Connection conn) throws SQLException, CloudCoderAuthenticationException {
		// Find the user's course registration in the course/section
		PreparedStatement findReg = prepareStatement(
				conn,
				"select cr.* from cc_course_registrations as cr " +
				" where cr.user_id = ? " +
				"   and cr.course_id = ? " +
				"   and cr.section = ? " +
				"   and cr.registration_type >= ?"
		);
		findReg.setInt(1, user.getId());
		findReg.setInt(2, problem.getCourseId());
		findReg.setInt(3, section);
		findReg.setInt(4, CourseRegistrationType.INSTRUCTOR.ordinal());
		
		ResultSet resultSet = executeQuery(findReg);
		
		if (!resultSet.next()) {
			throw new CloudCoderAuthenticationException("User is not an instructor in given course/section");
		}
		
		// Delete previous quiz for this problem/section (if any)
		PreparedStatement delOldQuiz = prepareStatement(
				conn,
				"delete from cc_quizzes where problem_id = ? and section = ?"
		);
		delOldQuiz.setInt(1, problem.getProblemId());
		delOldQuiz.setInt(2, section);
		delOldQuiz.executeUpdate();
		
		// Create the quiz record
		Quiz quiz = new Quiz();
		quiz.setProblemId(problem.getProblemId());
		quiz.setCourseId(problem.getCourseId());
		quiz.setSection(section);
		quiz.setStartTime(System.currentTimeMillis());
		quiz.setEndTime(0L);
		PreparedStatement insertQuiz = prepareStatement(
				conn,
				"insert into cc_quizzes values (" +
				DBUtil.getInsertPlaceholdersNoId(Quiz.SCHEMA) +
				")",
				PreparedStatement.RETURN_GENERATED_KEYS
		);
		DBUtil.bindModelObjectValuesForInsert(quiz, Quiz.SCHEMA, insertQuiz);
		
		insertQuiz.executeUpdate();
		
		ResultSet generatedKey = getGeneratedKeys(insertQuiz);
		if (!generatedKey.next()) {
			throw new SQLException("Could not get generated key for inserted Quiz");
		}
		quiz.setId(generatedKey.getInt(1));
		
		return quiz;
	}

	@Override
	public String getDescription() {
		return " starting quiz";
	}
}