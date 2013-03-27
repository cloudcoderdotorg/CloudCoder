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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.User;

/**
 * Store a {@link Problem} and its {@link TestCase}s in the database,
 * replacing the existing version (if any).
 */
public class StoreProblemAndTestCaseList extends
		AbstractDatabaseRunnable<ProblemAndTestCaseList> {
	private final ProblemAndTestCaseList problemAndTestCaseList;
	private final Course course;
	private final User user;

	/**
	 * Constructor.
	 * 
	 * @param problemAndTestCaseList  the {@link ProblemAndTestCaseList}
	 * @param course                  the {@link Course}
	 * @param user                    the authenticated {@link User}, who must be an instructor in the course
	 */
	public StoreProblemAndTestCaseList(
			ProblemAndTestCaseList problemAndTestCaseList, Course course,
			User user) {
		this.problemAndTestCaseList = problemAndTestCaseList;
		this.course = course;
		this.user = user;
	}

	@Override
	public ProblemAndTestCaseList run(Connection conn)
			throws SQLException, CloudCoderAuthenticationException {
		// Ensure problem and course id match.
		if (!problemAndTestCaseList.getProblem().getCourseId().equals((Integer) course.getId())) {
			throw new CloudCoderAuthenticationException("Problem does not match course");
		}
		
		// Check that user is registered as an instructor in the course.
		boolean isInstructor = false;
		List<? extends Object[]> courses = Queries.doGetCoursesForUser(user, conn, this);
		for (Object[] tuple : courses) {
			CourseRegistration courseReg = (CourseRegistration) tuple[2];
			if (courseReg.getCourseId() == course.getId()
					&& courseReg.getRegistrationType().ordinal() >= CourseRegistrationType.INSTRUCTOR.ordinal()) {
					isInstructor = true;
				break;
			}
		}
		if (!isInstructor) {
			throw new CloudCoderAuthenticationException("not instructor in course");
		}
		
		// If the problem id is not set, then insert the problem.
		// Otherwise, update the existing problem.
		if (problemAndTestCaseList.getProblem().getProblemId() == null
				|| problemAndTestCaseList.getProblem().getProblemId() < 0) {
			// Insert problem and test cases
			Queries.doInsertProblem(problemAndTestCaseList.getProblem(), conn, this);
			Queries.doInsertTestCases(
					problemAndTestCaseList.getProblem(),
					problemAndTestCaseList.getTestCaseData(),
					conn,
					this);
		} else {
			// Update problem and test cases
			Queries.doUpdateProblem(problemAndTestCaseList.getProblem(), conn, this);
			
			// We can achieve the effect of updating the test cases by deleting
			// and then reinserting
			Queries.doDeleteTestCases(problemAndTestCaseList.getProblem().getProblemId(), conn, this);
			Queries.doInsertTestCases(
					problemAndTestCaseList.getProblem(),
					problemAndTestCaseList.getTestCaseData(),
					conn,
					this);
		}
		
		// Success!
		return problemAndTestCaseList;
	}

	@Override
	public String getDescription() {
		return " storing problem and test cases";
	}
}