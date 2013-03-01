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

package org.cloudcoder.app.server.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.json.JSONConversion;

/**
 * Servlet to export exercise data ({@link Problem} and {@link TestCase}s)
 * in JSON format.  Only course instructors are allowed to
 * access this data.
 * 
 * @author David Hovemeyer
 */
public class ExerciseData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// The ProblemsAuthorizationFilter should have verified that the
		// authenticated user is an instructor. 
		User user = (User) req.getAttribute(RequestAttributeKeys.USER_KEY);
		
		ProblemURLInfo info = ProblemURLInfo.fromRequest(req);
		if (info == null) {
			AdminServletUtil.badRequest(resp);
			return;
		}
		
		Pair<Problem, Quiz> pair = Database.getInstance().getProblem(user, info.getProblemId());
		if (pair == null) {
			AdminServletUtil.forbidden(resp);
			return;
		}
		
		TestCase[] testCaseList = Database.getInstance().getTestCasesForProblem(user, true, info.getProblemId());
		if (testCaseList == null) {
			AdminServletUtil.forbidden(resp);
			return;
		}
		
		// It appears that we have a valid request, and that the user
		// is authorized to access both the problem and the test case list.
		// Send back the data in JSON format.
		
		ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
		problemAndTestCaseList.setProblem(pair.getLeft());
		problemAndTestCaseList.setTestCaseList(testCaseList);
		
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");
		JSONConversion.writeProblemAndTestCaseData(problemAndTestCaseList, resp.getWriter());
	}
}
