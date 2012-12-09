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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.KillableTaskManager;
import org.cloudcoder.builder2.javasandbox.SandboxUtil;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.ArrayUtil;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.jruby.Ruby;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

/**
 * Build step to test a {@link ProblemType#RUBY_METHOD} submission.
 * Assumes that {@link AddRubyMethodScaffoldingBuildStep} has already
 * been run.  Produces an array of {@link TestResult} objects as
 * a result artifact, one per {@link TestCase}.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class TestRubyMethodBuildStep implements IBuildStep {
	public static final long TIMEOUT_LIMIT = 5000;
	
	private static ScriptingContainer container;
	
	// Preload classes that will be needed to test the Ruby submission in the
	// IsolatedTask, and create the ScriptingContainer.
	static {
		new RubyTester();
		TestResultUtil.createResultForTimeout();
		container = new ScriptingContainer(LocalContextScope.CONCURRENT);
		container.runScriptlet("true");
	}

	@Override
	public void execute(BuilderSubmission submission) {
		ProgramSource[] programSourceList = submission.getArtifact(ProgramSource[].class);
		if (programSourceList == null) {
			throw new InternalBuilderException(this.getClass(), "No ProgramSource list");
		}
		
		if (programSourceList.length != 1) {
			throw new InternalBuilderException(this.getClass(), "Only one source file is expected");
		}
		final String testSource = programSourceList[0].getProgramText();
		
		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}
		
		// TODO: it would be nice to check whether or not the code will compile
		
		// Create a RubyTester in an IsolatedTask for each TestCase
		List<IsolatedTask<TestResult>> tasks = new ArrayList<IsolatedTask<TestResult>>();
		for (final TestCase testCase : testCaseList) {
			IsolatedTask<TestResult> task = new IsolatedTask<TestResult>() {
				@Override
				public TestResult execute() throws Throwable {
					RubyTester tester = new RubyTester();
					return tester.execute(container, testSource, testCase);
				}
			};
			tasks.add(task);
		}
		
		// Execute the tasks in a KillableTaskManager
		KillableTaskManager<TestResult> pool = new KillableTaskManager<TestResult>(
				tasks,
				TIMEOUT_LIMIT,
				new KillableTaskManager.TimeoutHandler<TestResult>() {
					@Override
					public TestResult handleTimeout() {
						return TestResultUtil.createResultForTimeout();
					}
				}
		);
		pool.setThreadNamePrefix("RubyTest_"); // enable Ruby-specific security manager rules
		pool.run();
		
		// merge outcomes with their buffered inputs for stdout/stderr
		List<TestResult> testResults = SandboxUtil.getStdoutStderr(pool);

		// Add array of TestResults as submission artifact
		submission.addArtifact(ArrayUtil.toArray(testResults, TestResult.class));
	}

}
