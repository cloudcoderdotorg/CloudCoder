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
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.User;

/**
 * Delete a {@link Problem} by marking it as deleted.
 * Note that the problem (and its test cases, submissions, etc.)
 * is not actually removed from the database.
 */
public class DeleteProblem extends AbstractDatabaseRunnable<Boolean> {
	private final User user;
	private final Problem problem;
	private final Course course;

	public DeleteProblem(User user, Problem problem, Course course) {
		this.user = user;
		this.problem = problem;
		this.course = course;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException, CloudCoderAuthenticationException {
		// verify that the user is an instructor in the course
		CourseRegistrationList courseReg = Queries.doGetCourseRegistrations(conn, course.getId(), user.getId(), this);
		if (!courseReg.isInstructor()) {
			throw new CloudCoderAuthenticationException("Only instructor can delete a problem");
		}
		
		// Delete the problem
		// Note that we do NOT delete the problem from the database.
		// Instead, we just set the deleted flag to true, which prevents the
		// problem from coming up in future searches.  Because lots
		// of information is linked to a problem, and serious database
		// corruption could occur if a problem id were reused, this
		// is a much safer approach than physical deletion.
		PreparedStatement stmt = prepareStatement(
				conn,
				"update " + Problem.SCHEMA.getDbTableName() +
				"   set " + Problem.DELETED.getName() + " = 1 " +
				" where " + Problem.PROBLEM_ID.getName() + " = ?");
		stmt.setInt(1, problem.getProblemId());
		
		stmt.executeUpdate();
		
		return true;
	}

	@Override
	public String getDescription() {
		return " deleting problem";
	}
}