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
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserRegistrationRequest;
import org.cloudcoder.app.shared.model.UserRegistrationRequestStatus;

/**
 * Database transaction to complete a user registration
 * given the {@link UserRegistrationRequest}.
 */
public class CompleteRegistration extends
		AbstractDatabaseRunnableNoAuthException<OperationResult> {
	private final UserRegistrationRequest request;

	/**
	 * Constructor.
	 * 
	 * @param request the {@link UserRegistrationRequest}
	 */
	public CompleteRegistration(UserRegistrationRequest request) {
		this.request = request;
	}

	@Override
	public OperationResult run(Connection conn) throws SQLException {
		// Copy information from request into a User object
		User user = new User();
		for (ModelObjectField<? super User, ?> field : User.SCHEMA.getFieldList()) {
			Object val = field.get(request);
			field.setUntyped(user, val);
		}
		
		// Attempt to insert the User
		try {
			DBUtil.storeModelObject(conn, user);
		} catch (SQLException e) {
			// Check to see if it was a duplicate key error, which would mean
			// an account with the same username or password has already been
			// created.
			if (e.getSQLState().equals("23000")) {
				throw new SQLException("A user account with the same username or email address has already been created.");
			} else {
				throw e;
			}
		}
		
		// Successfully added User - now set request status to CONFIRMED
		request.setStatus(UserRegistrationRequestStatus.CONFIRMED);
		DBUtil.updateModelObject(conn, request, UserRegistrationRequest.SCHEMA);
		
		// Success!
		return new OperationResult(true, "User account " + user.getUsername() + " created successfully!");
	}

	@Override
	public String getDescription() {
		return " completing user registration request";
	}
}