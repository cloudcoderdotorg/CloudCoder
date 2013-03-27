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
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.User;

public class GetProblemAndSubscriptionReceiptsForUserInCourse
		extends AbstractDatabaseRunnableNoAuthException<List<ProblemAndSubmissionReceipt>> {
	private final Course course;
	private final User forUser;
	private final Module module;
	private final User requestingUser;

	public GetProblemAndSubscriptionReceiptsForUserInCourse(Course course,
			User forUser, Module module, User requestingUser) {
		this.course = course;
		this.forUser = forUser;
		this.module = module;
		this.requestingUser = requestingUser;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
	 */
	@Override
	public List<ProblemAndSubmissionReceipt> run(Connection conn) throws SQLException {
		
		// Users can get their own problems/submission receipts,
		// but must be registered an an instructor to see another user's.
		if (requestingUser.getId() != forUser.getId()) {
			CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, course.getId(), requestingUser.getId(), this);
			if (!regList.isInstructor()) {
				getLogger().warn("Attempt by user {} to get problems/subscription receipts for user {}",
						requestingUser.getId(), forUser.getId());
				return new ArrayList<ProblemAndSubmissionReceipt>();
			}
		}
		
		// See: https://gist.github.com/4408441
		PreparedStatement stmt = prepareStatement(
				conn,
				"select p.*, m.*, sr.*, e.*, sr_ids.max_sr_event_id" +
				"  from cc_problems as p" +
				" join (select p.problem_id, sm.max_sr_event_id" +
				"         from cc_problems as p" +
				"       left join (select e.problem_id as problem_id, max(sr.event_id) as max_sr_event_id" +
				"                    from cc_submission_receipts as sr, cc_events as e" +
				"                  where e.id = sr.event_id" +
				"                    and e.user_id = ?" +
				"                  group by e.problem_id) as sm on p.problem_id = sm.problem_id" +
				"        where p.course_id = ?" +
				"        ) as sr_ids on sr_ids.problem_id = p.problem_id" +
				" join cc_modules as m on p.module_id = m.id " +
				" left join cc_submission_receipts as sr on sr.event_id = sr_ids.max_sr_event_id" +
				" left join cc_events as e on e.id = sr_ids.max_sr_event_id" + 
				" where p.deleted = 0" +
				"   and p.problem_id in" +
				"          (select p.problem_id from cc_problems as p" +
				"          join cc_course_registrations as cr on cr.course_id = p.course_id and cr.user_id = ?" +
				"          where" +
				"             p.course_id = ?" +
				"             and (   p.visible <> 0" +
				"                  or cr.registration_type >= ?" +
				"                  or p.problem_id in (select q.problem_id" +
				"                                        from cc_quizzes as q, cc_course_registrations as cr" +
				"                                       where cr.user_id = ?" +
				"                                         and cr.course_id = ?" +
				"                                         and q.course_id = cr.course_id" +
				"                                         and q.section = cr.section" +
				"                                         and q.start_time <= ?" +
				"                                         and (q.end_time >= ? or q.end_time = 0))))"
		);
		stmt.setInt(1, forUser.getId());
		stmt.setInt(2, course.getId());
		stmt.setInt(3, requestingUser.getId());
		stmt.setInt(4, course.getId());
		stmt.setInt(5, CourseRegistrationType.INSTRUCTOR.ordinal());
		stmt.setInt(6, forUser.getId());
		stmt.setInt(7, course.getId());
		long currentTime = System.currentTimeMillis();
		stmt.setLong(8, currentTime);
		stmt.setLong(9, currentTime);
		
		List<ProblemAndSubmissionReceipt> result = new ArrayList<ProblemAndSubmissionReceipt>();
		
		ResultSet resultSet = executeQuery(stmt);
		
		while (resultSet.next()) {
			Problem problem = new Problem();
			int index = DBUtil.loadModelObjectFields(problem, Problem.SCHEMA, resultSet);
			
			Module problemModule = new Module();
			index = DBUtil.loadModelObjectFields(problemModule, Module.SCHEMA, resultSet, index);
			
			// If a module was specified, only return problems in that module
			if (module != null && problemModule.getId() != module.getId()) {
				continue;
			}
			
			// Is there a submission receipt?
			SubmissionReceipt receipt;
			if (resultSet.getObject(index) != null) {
				// Yes
				receipt = new SubmissionReceipt();
				index = DBUtil.loadModelObjectFields(receipt, SubmissionReceipt.SCHEMA, resultSet, index);
				Event event = new Event();
				index = DBUtil.loadModelObjectFields(event, Event.SCHEMA, resultSet, index);
				receipt.setEvent(event);
			} else {
				// No
				receipt = null;
			}
			
			ProblemAndSubmissionReceipt problemAndSubmissionReceipt = new ProblemAndSubmissionReceipt();
			problemAndSubmissionReceipt.setProblem(problem);
			problemAndSubmissionReceipt.setReceipt(receipt);
			problemAndSubmissionReceipt.setModule(problemModule);
			
			result.add(problemAndSubmissionReceipt);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "retrieving problems and subscription receipts for course";
	}
}