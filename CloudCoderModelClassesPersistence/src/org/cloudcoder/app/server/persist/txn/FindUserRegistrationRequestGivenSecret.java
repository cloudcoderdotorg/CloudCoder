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
import org.cloudcoder.app.shared.model.UserRegistrationRequest;

/**
 * Transaction to find a {@link UserRegistrationRequest} given its associated secret.
 */
public class FindUserRegistrationRequestGivenSecret extends
		AbstractDatabaseRunnableNoAuthException<UserRegistrationRequest> {
	private final String secret;

	/**
	 * Constructor.
	 * 
	 * @param secret the secret
	 */
	public FindUserRegistrationRequestGivenSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public UserRegistrationRequest run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select * from " + UserRegistrationRequest.SCHEMA.getDbTableName() + " as urr " +
				" where urr." + UserRegistrationRequest.SECRET.getName() + " = ?"
		);
		stmt.setString(1, secret);
		
		ResultSet resultSet = executeQuery(stmt);
		if (!resultSet.next()) {
			return null;
		}
		UserRegistrationRequest request = new UserRegistrationRequest();
		Queries.loadGeneric(request, resultSet, 1, UserRegistrationRequest.SCHEMA);
		return request;
	}

	@Override
	public String getDescription() {
		return " finding user registration request";
	}
}