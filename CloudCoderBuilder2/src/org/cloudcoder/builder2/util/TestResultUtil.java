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

package org.cloudcoder.builder2.util;

import org.cloudcoder.app.shared.model.ITestCase;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.CommandResult;

/**
 * Utility methods for creating {@link TestResult}s.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class TestResultUtil {
	/**
	 * Create a TestResult for an executed test that timed out.
	 * 
	 * @param p         the test output
	 * @param problem   the {@link Problem}
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForTimeout(CommandResult p, Problem problem, ITestCase testCase) {
		TestResult testResult = new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
		        "timeout",
		        StringUtil.merge(p.getStdout()),
		        StringUtil.merge(p.getStderr()));
		return testResult;
	}

	/**
	 * Create a TestResult for an executed test that passed.
	 * 
	 * @param p         the test output
	 * @param problem   the {@link Problem}
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForPassedTest(CommandResult p, Problem problem, TestCase testCase) {
		return createTestResult(p, problem, TestOutcome.PASSED, testCase);
	}

	/**
	 * Create a TestResult for an executed test that caused a core dump.
	 * 
	 * @param p         the test output
	 * @param problem   the {@link Problem}
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForCoreDump(CommandResult p, Problem problem, TestCase testCase) {
		return createTestResult(p, problem, TestOutcome.FAILED_WITH_EXCEPTION, testCase);
	}

	/**
	 * Create a TestResult for an executed test that failed due to a failed assertion.
	 * 
	 * @param p         the test output
	 * @param problem   the {@link Problem}
	 * @param testCase  the test case that was executed
	 * @return the TestResult
	 */
	public static TestResult createTestResultForFailedAssertion(CommandResult p, Problem problem, TestCase testCase) {
		return createTestResult(p, problem, TestOutcome.FAILED_ASSERTION, testCase);
	}

	/**
	 * Create a TestResult to indicate that a TestCase couldn't be executed
	 * because of an internal error.
	 * 
	 * @param p         the ProcessRunner for the TestCase
	 * @param problem   the {@link Problem}
	 * @param testCase  the TestCase
	 * @return the TestResult
	 */
	public static TestResult createTestResultForInternalError(CommandResult p, Problem problem, ITestCase testCase) {
		TestResult testResult = new TestResult(
				TestOutcome.INTERNAL_ERROR,
				"The test failed to execute",
				StringUtil.merge(p.getStdout()),
				StringUtil.merge(p.getStderr()));
		return testResult;
	}
	
	/**
	 * Create a {@link TestResult} for a case where a {@link Command}
	 * did not exit normally.
	 * 
	 * @param p        the {@link CommandResult}
	 * @param problem   the {@link Problem}
	 * @param testCase the {@link TestCase} that was being executed
	 * @return the {@link TestResult}
	 */
	public static TestResult createTestResultForAbnormalExit(CommandResult p, Problem problem, TestCase testCase) {
		switch (p.getStatus()) {
		case COULD_NOT_START:
		case UNKNOWN:
			return createTestResultForInternalError(p, problem, testCase);
		case KILLED_BY_SIGNAL:
			return createTestResultForCoreDump(p, problem, testCase);
		case TIMED_OUT:
			return createTestResultForTimeout(p, problem, testCase);
		case FILE_SIZE_LIMIT_EXCEEDED:
			return createTestResultForLimitExceeded(p, problem, testCase);
		default:
			throw new IllegalArgumentException("Invalid process status: " + p.getStatus());
		}
	}

	/**
	 * Create a {@link TestResult} for a test that failed because a runtime
	 * limit was exceeded. 
	 * 
	 * @param p         the {@link CommandResult}
	 * @param problem   the {@link Problem}
	 * @param testCase  the {@link TestCase}
	 * @return the {@link TestResult}
	 */
	public static TestResult createTestResultForLimitExceeded(CommandResult p, Problem problem, TestCase testCase) {
		return new TestResult(
				TestOutcome.FAILED_BY_SECURITY_MANAGER,
				"File size limit exceeded - " + p.getStatusMessage(),
				"",
				"");
	}

	/**
	 * Create a generic {@link TestResult} for a passed test.
	 * 
	 * @param problem   the {@link Problem}
	 * @param testCase the {@link TestCase}
	 * @return the {@link TestResult}
	 */
	public static TestResult createResultForPassedTest(Problem problem, TestCase testCase) {
		return createTestResult(null, problem, TestOutcome.PASSED, testCase);
	}

	/**
	 * Create a generic {@link TestResult} for a failed test.
	 * This method assumes that we don't have the actual output
	 * of the submitted code (i.e. the expected result was 5
	 * but we expected 10).  It may not be possible to capture
	 * the return value of the method in the test harness for
	 * all languages that require unit testing at the
	 * method or function level.
	 * 
	 * @param problem  the {@link Problem}
	 * @param testCase the {@link TestCase}
	 * @return The {@link TestResult}
	 */
	public static TestResult createResultForFailedTest(Problem problem, TestCase testCase) {
	    return createTestResult(null, problem, TestOutcome.FAILED_ASSERTION, testCase, null);
	}
	
	/**
	 * Create a generic {@link TestResult} for a failed test.
	 * 
	 * @param problem  the {@link Problem}
	 * @param testCase the {@link TestCase}
	 * @param output   the actual output of calling the 
	 *                 method written by the client
	 * @return the {@link TestResult}
	 */
	public static TestResult createResultForFailedTest(Problem problem, TestCase testCase, String output) {
		return createTestResult(null, problem, TestOutcome.FAILED_ASSERTION, testCase, output);
	}

	/**
	 * Create a generic timeout {@link TestResult}.
	 * 
	 * @return the {@link TestResult}
	 */
	public static TestResult createResultForTimeout() {
		return new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
				"Took too long!  Check for infinite loops, or recursion without a proper base case");
	}

	private static TestResult createTestResult(CommandResult p, Problem problem, TestOutcome outcome, TestCase testCase) {
	    return createTestResult(p, problem, outcome, testCase, null);
	}
	
	/**
	 * Helper method to create a standard test result.
	 * If a {@link CommandResult} is passed, its stdout/stderr will be
	 * added to the test result.
	 * 
	 * @param p         the {@link CommandResult} (null if the test was not executed as a {@link Command})
	 * @param problem   the {@link Problem}
	 * @param outcome   the {@link TestOutcome}
	 * @param testCase  the {@link TestCase}
	 * @param output    the actual output of the method (if null then no information will be displayed)
	 * @return the {@link TestResult}
	 */
	private static TestResult createTestResult(CommandResult p, Problem problem, TestOutcome outcome, TestCase testCase, String output) {
		StringBuilder buf = new StringBuilder();
		buf.append(outcome.getShortMessage());

		if (!testCase.isSecret()) {
			buf.append(" for input (" + testCase.getInput() + ")");
			if (problem.getProblemType().isOutputLiteral()) {
				buf.append(", expected output=" + testCase.getOutput());
				if (output!=null && outcome!=TestOutcome.PASSED) {
				    // include the actual output, if we have it
				    buf.append(", actual output="+output);
				}
			}
		}

		TestResult testResult = new TestResult(outcome, buf.toString());

		if (p != null) {
			if (outcome.isDisplayProcessStatus() &&
					p.getStatusMessage() != null && !p.getStatusMessage().equals("")) {
				// Process did not complete normally, so add the ProcessRunner's status message
				buf.append(" - " + p.getStatusMessage());
			}
	
			if (!testCase.isSecret()) {
				testResult.setStdout(StringUtil.merge(p.getStdout()));
				testResult.setStderr(StringUtil.merge(p.getStderr()));
			} else {
				testResult.setStdout("Secret test - output is not revealed");
				testResult.setStderr("Secret test - output is not revealed");
			}
		}

		return testResult;
	}
}
