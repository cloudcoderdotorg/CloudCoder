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
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get the {@link Module}s for a {@link Course},  meaning
 * all of the modules into which problems in the course have
 * been categorized.
 */
public class GetModulesForCourse extends AbstractDatabaseRunnableNoAuthException<Module[]> {
	private final User user;
	private final Course course;

	public GetModulesForCourse(User user, Course course) {
		this.user = user;
		this.course = course;
	}

	@Override
	public Module[] run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select m.* from cc_modules as m " +
				" where m.id in " +
				"   (select p.module_id from cc_problems as p, cc_course_registrations as cr " +
				"     where p.course_id = cr.course_id " +
				"       and cr.course_id = ? " +
				"       and cr.user_id = ?) " +
				" order by m.name"
		);
		stmt.setInt(1, course.getId());
		stmt.setInt(2, user.getId());
		
		ResultSet resultSet = executeQuery(stmt);
		List<Module> result = new ArrayList<Module>();
		while (resultSet.next()) {
			Module module = new Module();
			DBUtil.loadModelObjectFields(module, Module.SCHEMA, resultSet);
			result.add(module);
		}
		
		return result.toArray(new Module[result.size()]);
	}

	@Override
	public String getDescription() {
		return " getting modules in course";
	}
}