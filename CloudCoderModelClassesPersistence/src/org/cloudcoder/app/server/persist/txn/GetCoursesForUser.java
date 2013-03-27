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
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get all {@link Course}s for which given {@link User}
 * is registered.
 */
public class GetCoursesForUser extends AbstractDatabaseRunnableNoAuthException<List<? extends Object[]>> {
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param user the {@link User}
	 */
	public GetCoursesForUser(User user) {
		this.user = user;
	}

	@Override
	public List<? extends Object[]> run(Connection conn) throws SQLException {
		return Queries.doGetCoursesForUser(user, conn, this);
	}

	@Override
	public String getDescription() {
		return " retrieving courses for user";
	}
}