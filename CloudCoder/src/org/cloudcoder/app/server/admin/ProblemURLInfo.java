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

package org.cloudcoder.app.server.admin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Object representing the course, problem, and/or user involved in
 * a request to the {@link Problems} servlet.
 * 
 * @author David Hovemeyer
 */
public class ProblemURLInfo {
	private final int courseId;
	private final int section;
	private final int problemId;
	private final int userId;
	
	/**
	 * Constructor.
	 * 
	 * @param courseId   the course id
	 * @param section    the section
	 * @param problemId  the problem id
	 * @param userId     the user id (student)
	 */
	public ProblemURLInfo(int courseId, int section, int problemId, int userId) {
		this.courseId = courseId;
		this.section = section;
		this.problemId = problemId;
		this.userId = userId;
	}
	
	/**
	 * @return the course id
	 */
	public int getCourseId() {
		return courseId;
	}
	
	/**
	 * @return the section (0 if a section was not specified)
	 */
	public int getSection() {
		return section;
	}
	
	/**
	 * @return the problem id (-1 if a problem id was not specified)
	 */
	public int getProblemId() {
		return problemId;
	}
	
	/**
	 * @return the user id (-1 if a user id was not specified)
	 */
	public int getUserId() {
		return userId;
	}
	
	private static final Pattern PROBLEM_URLINFO_PATTERN = Pattern.compile("(\\d+)(-\\d+)?(/(\\d+)(/(\\d+))?)?");
	private static final int COURSE_ID_GROUP = 1;
	private static final int SECTION_GROUP = 2;
	private static final int PROBLEM_ID_GROUP = 4;
	private static final int USER_ID_GROUP = 6;
	
	/**
	 * Parse the path info of a request to the {@link Problems} servlet.
	 * 
	 * @param req a request to the Problems servlet
	 * @return the ProblemURLInfo encoded in the request's path info,
	 *         or null if the path info does not encode a valid request
	 */
	public static ProblemURLInfo fromRequest(HttpServletRequest req) {
		// Problem URLs are of the form:
		//
		// - admin/problems/<course id> for all problems in course
		// - admin/problems/<course id>/<problem id> for specific problem in course
		// - admin/problems/<course id>/<problem id>/<user id> for
		//   specific student's work on specific problem in course
		
		// <course id> may optionally have a hyphen and a section appended
		
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			return null;
		}
		//System.out.println("Problem pathInfo is " + pathInfo);
		
		if (pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		
		Matcher m = PROBLEM_URLINFO_PATTERN.matcher(pathInfo);
		if (!m.matches()) {
			return null;
		}
		
		int courseId;
		int section = 0;
		int problemId = -1;
		int userId = -1;
		
		courseId = Integer.parseInt(m.group(COURSE_ID_GROUP));
		String optSection = m.group(SECTION_GROUP);
		if (nonEmptyMatch(optSection)) {
			// skip leading "-"
			optSection = optSection.substring(1);
			section = Integer.parseInt(optSection);
		}
		String optProblemId = m.group(PROBLEM_ID_GROUP);
		if (nonEmptyMatch(optProblemId)) {
			problemId = Integer.parseInt(optProblemId);
		}
		String optUserId = m.group(USER_ID_GROUP);
		if (nonEmptyMatch(optUserId)) {
			userId = Integer.parseInt(optUserId);
		}

		return new ProblemURLInfo(courseId, section, problemId, userId);
	}

	private static boolean nonEmptyMatch(String s) {
		return s != null && !s.equals("");
	}
}
