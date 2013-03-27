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
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.User;

/**
 * Get a {@link Problem} for a given {@link User}.
 * Does not return the problem (and associated quiz, if any)
 * unless the user is permitted to see it.
 */
public class GetProblemForUser extends
		AbstractDatabaseRunnableNoAuthException<Pair<Problem, Quiz>> {
	private final int problemId;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param problemId the unique id of the {@link Problem}
	 * @param user      the {@link User} for whom were are getting the problem (and maybe quiz)
	 */
	public GetProblemForUser(int problemId, User user) {
		this.problemId = problemId;
		this.user = user;
	}

	@Override
	public Pair<Problem, Quiz> run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select p.*, r.* from " + Problem.SCHEMA.getDbTableName() + " as p, " + Course.SCHEMA.getDbTableName() + " as c, " + CourseRegistration.SCHEMA.getDbTableName() + " as r " +
				" where p.problem_id = ? " +
				"   and c.id = p.course_id " +
				"   and r.course_id = c.id " +
				"   and r.user_id = ?"
		);
		stmt.setInt(1, problemId);
		stmt.setInt(2, user.getId());
		
		ResultSet resultSet = executeQuery(stmt);
		
		if (!resultSet.next()) {
			// no such problem, or user is not registered in the course
			// in which the problem is assigned
			return null;
		}
		
		// Get Problem and CourseRegistration
		Problem problem = new Problem();
		CourseRegistration reg = new CourseRegistration();
		
		int index = DBUtil.loadModelObjectFields(problem, Problem.SCHEMA, resultSet);
		index = DBUtil.loadModelObjectFields(reg, CourseRegistration.SCHEMA, resultSet, index);
		
		// Check to see if user is authorized to see this problem
		
		// Instructors are always allowed to see problems, even if not visible
		if (reg.getRegistrationType().isInstructor()) {
			return new Pair<Problem, Quiz>(problem, null);
		}
		
		// Problem is visible?
		if (problem.isVisible()) {
			return new Pair<Problem, Quiz>(problem, null);
		}
		
		// See if there is an ongoing quiz
		Quiz quiz = Queries.doFindQuiz(problem.getProblemId(), reg.getSection(), System.currentTimeMillis(), conn, this);
		if (quiz != null) {
			System.out.println("Found quiz for problem " + problem.getProblemId());
			return new Pair<Problem, Quiz>(problem, quiz);
		}
		
		return null;
	}

	@Override
	public String getDescription() {
		return "retrieving problem";
	}
}