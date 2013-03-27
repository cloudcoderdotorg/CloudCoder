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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;

/**
 * A wrapper for {@link TestResult} that specifies the
 * name of the corresponding {@link TestCase}.
 * 
 * @author David Hovemeyer
 */
public class NamedTestResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private String testCaseName;
	private TestResult testResult;
	
	/**
	 * Default constructor.
	 */
	public NamedTestResult() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param testCaseName the name of the test case
	 * @param testResult   the {@link TestResult}
	 */
	public NamedTestResult(String testCaseName, TestResult testResult) {
		this.testCaseName = testCaseName;
		this.testResult = testResult;
	}
	
	/**
	 * Set the test case name.
	 * 
	 * @param testCaseName the test case name
	 */
	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}
	
	/**
	 * Set the test result.
	 * 
	 * @param testResult the test result
	 */
	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}
	
	/**
	 * @return the testCaseName
	 */
	public String getTestCaseName() {
		return testCaseName;
	}
	
	/**
	 * @return the testResult
	 */
	public TestResult getTestResult() {
		return testResult;
	}
}
