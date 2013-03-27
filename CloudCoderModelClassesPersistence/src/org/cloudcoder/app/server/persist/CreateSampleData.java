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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.IProblem;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ITestCase;
import org.cloudcoder.app.shared.model.ITestCaseData;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.Term;

/**
 * Create sample data.
 * 
 * @author David Hovemeyer
 */
public class CreateSampleData {

	/**
	 * Create a demo course.
	 * 
	 * @param conn  the database connection
	 * @param term  the term in which the course should be created
	 * @return the course id of the newly-created course
	 * @throws SQLException
	 */
	public static int createDemoCourse(Connection conn, Term term) throws SQLException {
		Course course = new Course();
		course.setName("CCDemo");
		course.setTitle("CloudCoder demo course");
		course.setTermId(term.getId());
		course.setTerm(term);
		course.setYear(2012);
		course.setUrl("http://cloudcoder.org/");
		DBUtil.storeModelObject(conn, course);
		return course.getId();
	}

	public static void populateSampleTestCase(ITestCase testCase, Integer problemId) {
		testCase.setProblemId(problemId);
		CreateSampleData.populateSampleTestCaseData(testCase);
	}

	public static void populateSampleTestCaseData(ITestCaseData testCase) {
		testCase.setTestCaseName("hello");
		testCase.setInput("");
		testCase.setOutput("^\\s*Hello\\s*,\\s*world\\s*$i");
		testCase.setSecret(false);
	}

	public static void populateSampleProblem(IProblem problem, int courseId) {
		problem.setCourseId(courseId);
		problem.setWhenAssigned(System.currentTimeMillis());
		problem.setWhenDue(problem.getWhenAssigned() + (24L*60*60*1000));
		problem.setVisible(true);
		problem.setProblemAuthorship(ProblemAuthorship.ORIGINAL);
		CreateSampleData.populateSampleProblemData(problem);
	}

	public static void populateSampleProblemData(IProblemData problemData) {
		problemData.setProblemType(ProblemType.C_PROGRAM);
		problemData.setTestname("hello");
		problemData.setBriefDescription("Print hello, world");
		problemData.setDescription(
				"<p>Print a line with the following text:</p>\n" +
				"<blockquote><pre>Hello, world</pre></blockquote>\n"
		);
	
		problemData.setSkeleton(
				"#include <stdio.h>\n\n" +
				"int main(void) {\n" +
				"\t// TODO - add your code here\n\n" +
				"\treturn 0;\n" +
				"}\n"
				);
		problemData.setSchemaVersion(Problem.SCHEMA.getVersion());
		problemData.setAuthorName("David Hovemeyer");
		problemData.setAuthorEmail("dhovemey@ycp.edu");
		problemData.setAuthorWebsite("http://faculty.ycp.edu/~dhovemey");
		problemData.setTimestampUtc(1349008031587L);
		problemData.setLicense(ProblemLicense.CC_ATTRIB_SHAREALIKE_3_0);
		problemData.setParentHash("");
	}
	
	public static void populateSampleCFunctionProblem(IProblem problem, int courseId) {
		problem.setCourseId(courseId);
		problem.setWhenAssigned(System.currentTimeMillis());
		problem.setWhenDue(problem.getWhenAssigned() + (24L*60*60*1000));
		problem.setVisible(true);
		problem.setProblemAuthorship(ProblemAuthorship.ORIGINAL);
		populateSampleCFunctionProblemData(problem);
	}

	private static void populateSampleCFunctionProblemData(IProblemData problemData) {
		problemData.setProblemType(ProblemType.C_FUNCTION);
		problemData.setTestname("addIntegers");
		problemData.setBriefDescription("Add two integers");
		problemData.setDescription(
				"<p>Complete the <code>addIntegers</code> function so that it\n" +
				"returns the sum of the two integer parameters (<code>a</code>\n" +
				"and <code>b</code>) passed to it.</p>"
		);
	
		problemData.setSkeleton(
				"int addIntegers(int a, int b) {\n" +
				"\t//TODO - add your code here\n" +
				"}"
				);
		problemData.setSchemaVersion(Problem.SCHEMA.getVersion());
		problemData.setAuthorName("David Hovemeyer");
		problemData.setAuthorEmail("dhovemey@ycp.edu");
		problemData.setAuthorWebsite("http://faculty.ycp.edu/~dhovemey");
		problemData.setTimestampUtc(1356125346562L);
		problemData.setLicense(ProblemLicense.CC_ATTRIB_SHAREALIKE_3_0);
		problemData.setParentHash("");
	}
	
	public static void populateSampleCFunctionTestCases(ITestCase[] testCases, int problemId) {
		for (ITestCase tc : testCases) {
			tc.setProblemId(problemId);
		}
		populateSampleCFunctionTestCaseData(testCases);
	}

	public static void populateSampleCFunctionTestCaseData(ITestCaseData[] testCases) {
		if (testCases.length != 3) {
			throw new IllegalArgumentException();
		}
		testCases[0].setTestCaseName("t0");
		testCases[0].setInput("2, 3");
		testCases[0].setOutput("5");
		testCases[0].setSecret(false);
		testCases[1].setTestCaseName("t1");
		testCases[1].setInput("-3, -11");
		testCases[1].setOutput("-14");
		testCases[1].setSecret(false);
		testCases[2].setTestCaseName("t2");
		testCases[2].setInput("0, 7");
		testCases[2].setOutput("7");
		testCases[2].setSecret(false);
	}

}
