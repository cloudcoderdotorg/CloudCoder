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

package org.cloudcoder.builder2.rubymethod;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test a {@link ProblemType#RUBY_METHOD} submission on a given 
 * {@link TestCase}, returning a {@link TestResult}.
 * This object should be used in an {@link IsolatedTask}.
 * 
 * @author David Hovemeyer
 */
public class RubyTester {
	private static Logger logger = LoggerFactory.getLogger(RubyTester.class);
	
	/**
	 * Execute the test and return the {@link TestResult} indicating whether the
	 * test passed or failed.
	 * 
	 * @param container  the {@link ScriptingContainer}
	 * @param testSource the Ruby test source code, produced by {@link AddRubyMethodScaffoldingBuildStep}
	 * @param testCase   the {@link TestCase} to execute
	 * @return the {@link TestResult} indicating whether the test passed or failed
	 */
	public TestResult execute(ScriptingContainer container, String testSource, TestCase testCase) {
		Object receiver = container.runScriptlet(testSource);
		//System.out.println("testSource:");
		//System.out.println(testSource);
		
		Object result_ = container.callMethod(receiver, "_test", testCase.getTestCaseName());
		if (!(result_ instanceof Boolean)) {
			return new TestResult(TestOutcome.INTERNAL_ERROR, "_test method did not return a Boolean result");
		}
		
		Boolean result = (Boolean) result_;
		
		return result
				? TestResultUtil.createResultForPassedTest(testCase)
				: TestResultUtil.createResultForFailedTest(testCase);
	}
}
