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
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to get all {@link Problem}s for given {@link User} in
 * given {@link Course}.
 */
public class GetProblemsInCourse extends AbstractDatabaseRunnableNoAuthException<ProblemList> {
	private final Course course;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param course the {@link Course}
	 * @param user   the {@link User}
	 */
	public GetProblemsInCourse(Course course, User user) {
		this.course = course;
		this.user = user;
	}

	@Override
	public ProblemList run(Connection conn) throws SQLException {
		return new ProblemList(Queries.doGetProblemsInCourse(user, course, conn, this));
	}

	@Override
	public String getDescription() {
		return "retrieving problems for course";
	}
}