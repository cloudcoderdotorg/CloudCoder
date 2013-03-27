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
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get all users in course.
 */
public class GetUsersInCourse extends AbstractDatabaseRunnableNoAuthException<List<User>> {
	private final int sectionNumber;
	private final int courseId;

	/**
	 * Constructor.
	 * 
	 * @param sectionNumber the section number (0 for all users in all sections)
	 * @param courseId      the course id
	 */
	public GetUsersInCourse(int sectionNumber, int courseId) {
		this.sectionNumber = sectionNumber;
		this.courseId = courseId;
	}

	@Override
	public List<User> run(Connection conn) throws SQLException
	{
	    PreparedStatement stmt=prepareStatement(conn, 
	            "select u.* " +
	                    " from " + User.SCHEMA.getDbTableName() + " as u, " +
	                    CourseRegistration.SCHEMA.getDbTableName()+" as reg " +
	                    " where u.id =  reg.user_id " +
	                    "   and reg.course_id = ? " +
	                    "   and (? = 0 or reg.section = ?)" // section number of 0 means "all sections"
	    );
	    stmt.setInt(1, courseId);
	    stmt.setInt(2, sectionNumber);
	    stmt.setInt(3, sectionNumber);

	    ResultSet resultSet = executeQuery(stmt);

	    List<User> users=new LinkedList<User>();
	    while (resultSet.next()) {
	        User u=new User();
	        Queries.load(u, resultSet, 1);
	        users.add(u);
	    }
	    return users;
	}

	@Override
	public String getDescription() {
	    return "retrieving users in courseId "+courseId;
	}
}