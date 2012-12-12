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

package org.cloudcoder.app.shared.model;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a {@link Problem} and {@link TestCase}s for the problem,
 * as assigned in a specific {@link Course}.
 * 
 * @author David Hovemeyer
 */
public class ProblemAndTestCaseList implements ActivityObject, IProblemAndTestCaseData<Problem, TestCase> {
	private static final long serialVersionUID = 1L;
	
	private Problem problem;
	private TestCase[] testCaseList;
	
	/**
	 * Constructor.
	 */
	public ProblemAndTestCaseList() {
		testCaseList = new TestCase[0];
	}
	
	/**
	 * Set the Problem.
	 * @param problem the Problem to set
	 */
	public void setProblem(Problem problem) {
		this.problem = problem;
	}
	
	/**
	 * @return the Problem
	 */
	@Override
	public Problem getProblem() {
		return problem;
	}
	
	/**
	 * Set the list of TestCases.
	 * @param testCaseList the list of TestCases
	 */
	public void setTestCaseList(TestCase[] testCaseList) {
		this.testCaseList = testCaseList;
	}
	
	/**
	 * @return the list of TestCases
	 */
	public TestCase[] getTestCaseList() {
		return testCaseList;
	}
	
	@Override
	public List<TestCase> getTestCaseData() {
		if (testCaseList == null) {
			throw new IllegalStateException();
		}
		return Arrays.asList(testCaseList);
	}

	/**
	 * Add a TestCase.
	 * 
	 * @param testCase the TestCase to add
	 */
	public void addTestCase(TestCase testCase) {
		// FIXME: probably we should just use a List to store the TestCases
		TestCase[] larger = new TestCase[testCaseList.length + 1];
		System.arraycopy(testCaseList, 0, larger, 0, testCaseList.length);
		larger[testCaseList.length] = testCase;
		testCaseList = larger;
	}

	/**
	 * Remove a TestCase.
	 * 
	 * @param testCase the TestCase to remove
	 */
	public void removeTestCase(TestCase testCase) {
		// FIXME: probably we should just use a List to store the TestCases
		TestCase[] smaller = new TestCase[testCaseList.length - 1];
		int index = 0;
		for (TestCase tc : testCaseList) {
			if (tc != testCase) {
				smaller[index++] = tc;
			}
		}
		testCaseList = smaller;
	}

	/**
	 * Copy all data in the given ProblemAndTestCaseList object into this one.
	 * To the extent possible, the {@link Problem} object and {@link TestCase}
	 * objects in this ProblemAndTestCaseList will be modified in place,
	 * rather than a new {@link Problem} and {@link TestCase}s being
	 * created. 
	 * 
	 * @param other another ProblemAndTestCaseList object
	 */
	public void copyFrom(ProblemAndTestCaseList other) {
		if (other.problem == null) {
			this.problem = null;
		} else {
			if (this.problem == null) {
				this.problem = new Problem();
			}
			this.problem.copyFrom(other.problem);
		}

		if (other.testCaseList == null) {
			this.testCaseList = null;
		} else {
			if (this.testCaseList.length != other.testCaseList.length) {
				int min = Math.min(this.testCaseList.length, other.testCaseList.length);
				TestCase[] newTestCaseList = new TestCase[other.testCaseList.length];
				// Preserve as many existing TestCase objects as possible
				for (int i = 0; i < min; i++) {
					newTestCaseList[i] = this.testCaseList[i];
				}
				// Create new TestCase objects if the list has been expanded
				for (int i = min; i < newTestCaseList.length; i++) {
					newTestCaseList[i] = new TestCase();
				}
				this.testCaseList = newTestCaseList;
			}
			//this.testCaseList = new TestCase[other.testCaseList.length];
			for (int i = 0; i < other.testCaseList.length; i++) {
				this.testCaseList[i].copyFrom(other.testCaseList[i]);
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ProblemAndTestCaseList)) {
			return false;
		}
		
		ProblemAndTestCaseList other = (ProblemAndTestCaseList) obj;
		
		return ModelObjectUtil.equals(this.problem, other.problem)
				&& ModelObjectUtil.arrayEquals(this.testCaseList, other.testCaseList);
	}
}
