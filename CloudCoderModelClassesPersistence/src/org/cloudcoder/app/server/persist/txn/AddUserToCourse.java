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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to register a user in a course.
 * Authenticated user must be an instructor in the course.
 */
public class AddUserToCourse extends AbstractDatabaseRunnable<Boolean> {
	private final int courseId;
	private final User authenticatedUser;
	private final EditedUser editedUser;

	/**
	 * Constructor.
	 * 
	 * @param courseId            the unique id of the course
	 * @param authenticatedUser   the authenticated user, who must be an instructor in the course 
	 * @param editedUser          the user to add to the course
	 */
	public AddUserToCourse(int courseId, User authenticatedUser,
			EditedUser editedUser) {
		this.courseId = courseId;
		this.authenticatedUser = authenticatedUser;
		this.editedUser = editedUser;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException, CloudCoderAuthenticationException {
		// Make sure authenticated user is an instructor in the course
		CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, courseId, authenticatedUser.getId(), this);
		if (!regList.isInstructor()) {
			getLogger().warn("Attempt by non-instructor user {} to add new user to course {}", authenticatedUser.getId(), courseId);
			throw new CloudCoderAuthenticationException("Only an instructor can add user to course");
		}

		// Add the new user
		User user = editedUser.getUser();
		int userId = ConfigurationUtil.createOrUpdateUser(conn, user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail(), editedUser.getPassword(), user.getWebsite());
		user.setId(userId);
		ConfigurationUtil.registerUser(conn, user.getId(), courseId, editedUser.getRegistrationType(), editedUser.getSection());
		
		return true;
	}

	@Override
	public String getDescription() {
		return " adding user to course";
	}
}