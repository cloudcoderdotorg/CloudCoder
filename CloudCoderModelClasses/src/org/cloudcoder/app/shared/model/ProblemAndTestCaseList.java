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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private List<TestCase> testCaseList;
	
	/**
	 * Constructor.
	 */
	public ProblemAndTestCaseList() {
		testCaseList = new ArrayList<TestCase>();
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
		this.testCaseList.clear();
		this.testCaseList.addAll(Arrays.asList(testCaseList));
	}
	
	/**
	 * @return the list of TestCases
	 */
	public TestCase[] getTestCaseList() {
		return testCaseList.toArray(new TestCase[testCaseList.size()]);
	}
	
	@Override
	public List<TestCase> getTestCaseData() {
		if (testCaseList == null) {
			throw new IllegalStateException();
		}
		return Collections.unmodifiableList(testCaseList);
	}

	/**
	 * Add a TestCase.
	 * 
	 * @param testCase the TestCase to add
	 */
	public void addTestCase(TestCase testCase) {
		testCaseList.add(testCase);
	}
	
	/**
	 * Remove test case with given index.
	 * 
	 * @param index the index
	 */
	public void removeTestCase(int index) {
		testCaseList.remove(index);
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

		if (other.testCaseList.size() >= this.testCaseList.size()) {
			// Other test case list is at least as large: copy
			// as many test cases as possible in place, and then add
			// additional new test cases as needed
			for (int i = 0; i < this.testCaseList.size(); i++) {
				this.testCaseList.get(i).copyFrom(other.testCaseList.get(i));
			}
			for (int i = this.testCaseList.size(); i < other.testCaseList.size(); i++) {
				TestCase tc = new TestCase();
				tc.copyFrom(other.testCaseList.get(i));
				this.testCaseList.add(tc);
			}
		} else {
			// Other test case list is smaller: truncate this test case list
			// and then copy each test case in-place
			this.testCaseList.subList(other.testCaseList.size(), this.testCaseList.size()).clear();
			for (int i = 0; i < other.testCaseList.size(); i++) {
				this.testCaseList.get(i).copyFrom(other.testCaseList.get(i));
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
				&& this.testCaseList.equals(other.testCaseList);
	}
}
