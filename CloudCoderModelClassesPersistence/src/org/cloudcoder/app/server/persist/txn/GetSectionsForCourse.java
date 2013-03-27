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
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get all sections for a given {@link Course}.
 * Authenticated user must be an instructor in the course.
 */
public class GetSectionsForCourse extends AbstractDatabaseRunnableNoAuthException<Integer[]> {
	private final Course course;
	private final User authenticatedUser;

	/**
	 * Constructor.
	 * 
	 * @param course             the {@link Course}
	 * @param authenticatedUser  the authenticated {@link User}, who must be an
	 *                           instructor in the course
	 */
	public GetSectionsForCourse(Course course, User authenticatedUser) {
		this.course = course;
		this.authenticatedUser = authenticatedUser;
	}

	@Override
	public Integer[] run(Connection conn) throws SQLException {
		// Make sure user is an instructor in the course
		CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, course.getId(), authenticatedUser.getId(), this);
		if (!regList.isInstructor()) {
			return new Integer[0];
		}
		
		PreparedStatement stmt = prepareStatement(
				conn,
				"select distinct cr.section from cc_course_registrations as cr " +
				" where cr.course_id = ? " +
				" order by cr.section asc"
		);
		stmt.setInt(1, course.getId());
		
		List<Integer> result = new ArrayList<Integer>();
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			result.add(resultSet.getInt(1));
		}
		
		return result.toArray(new Integer[result.size()]);
	}

	@Override
	public String getDescription() {
		return " getting sections for course";
	}
}