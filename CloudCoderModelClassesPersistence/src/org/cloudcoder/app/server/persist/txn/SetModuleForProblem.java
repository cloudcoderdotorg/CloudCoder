// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to set the {@link Module} for a {@link Problem}.
 */
public class SetModuleForProblem extends AbstractDatabaseRunnable<Module> {
	private final String moduleName;
	private final User user;
	private final Problem problem;

	/**
	 * Constructor.
	 * 
	 * @param moduleName the name of the module
	 * @param user       the authenticated {@link User}, who must be an instructor in the
	 *                   problem's course
	 * @param problem    the {@link Problem}
	 */
	public SetModuleForProblem(String moduleName, User user, Problem problem) {
		this.moduleName = moduleName;
		this.user = user;
		this.problem = problem;
	}

	@Override
	public Module run(Connection conn) throws SQLException, CloudCoderAuthenticationException {
		// Verify that user is an instructor in the course
		// (throwing CloudCoderAuthenticationException if not)
		PreparedStatement verifyInstructorStmt = prepareStatement(
				conn,
				"select cr.id from cc_course_registrations as cr, cc_problems as p " +
				" where cr.user_id = ? " +
				"   and p.problem_id = ? " +
				"   and cr.course_id = p.course_id " +
				"   and cr.registration_type >= ?"
		);
		verifyInstructorStmt.setInt(1, user.getId());
		verifyInstructorStmt.setInt(2, problem.getProblemId());
		verifyInstructorStmt.setInt(3, CourseRegistrationType.INSTRUCTOR.ordinal());
		
		ResultSet verifyInstructorResultSet = executeQuery(verifyInstructorStmt);
		if (!verifyInstructorResultSet.next()) {
			getLogger().info(
					"Attempt by user {} to set module for problem {} without instructor permission",
					user.getId(),
					problem.getProblemId());
			throw new CloudCoderAuthenticationException("Only an instructor can set the module for an exercise");
		}
		
		// See if the module exists already
		PreparedStatement findExisting = prepareStatement(
				conn,
				"select m.* from cc_modules as m where m.name = ?"
		);
		findExisting.setString(1, moduleName);
		
		Module module = new Module();
		ResultSet findExistingResultSet = executeQuery(findExisting);
		if (findExistingResultSet.next()) {
			// Use existing module
			DBUtil.loadModelObjectFields(module, Module.SCHEMA, findExistingResultSet);
		} else {
			// Module doesn't exist, so add it
			module.setName(moduleName);
			DBUtil.storeModelObject(conn, module);
		}
		
		// Update the problem to use the new module
		problem.setModuleId(module.getId());
		DBUtil.updateModelObject(conn, problem, Problem.SCHEMA);
		
		return module;
	}

	@Override
	public String getDescription() {
		return " setting module for problem";
	}
}