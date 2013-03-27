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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.ConfigurationUtil;

/**
 * Transaction to edit {@link User} information given the user id and all data values
 * (username, first name, last name, etc.)
 */
public class EditUserGivenUserData extends AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final String username;
	private final String lastname;
	private final String email;
	private final String passwd;
	private final int userId;
	private final String firstname;

	public EditUserGivenUserData(String username, String lastname,
			String email, String passwd, int userId, String firstname) {
		this.username = username;
		this.lastname = lastname;
		this.email = email;
		this.passwd = passwd;
		this.userId = userId;
		this.firstname = firstname;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
	 */
	@Override
	public Boolean run(Connection conn) throws SQLException {
	    getLogger().info("Editing user "+userId);
	    ConfigurationUtil.updateUser(conn, userId, username,
	            firstname, lastname, email, passwd);
	    return true;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
	    return "Updating user record";
	}
}