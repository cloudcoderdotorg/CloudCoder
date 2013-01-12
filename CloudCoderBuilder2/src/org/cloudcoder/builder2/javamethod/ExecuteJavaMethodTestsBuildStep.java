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

package org.cloudcoder.builder2.javamethod;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.KillableTaskManager;
import org.cloudcoder.builder2.javasandbox.SandboxUtil;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.LoadedClasses;

/**
 * Execute {@link ProblemType#JAVA_METHOD} tests and create a
 * {@link SubmissionResult}.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class ExecuteJavaMethodTestsBuildStep implements IBuildStep {
    public static final long TIMEOUT_LIMIT = 2000;

	@Override
	public void execute(BuilderSubmission submission) {
		// Get Problem
		Problem problem = submission.getArtifact(Problem.class);
		if (problem == null) {
			throw new InternalBuilderException(this.getClass(), "No Problem");
		}

		// Get LoadedClasses
		LoadedClasses loadedClasses = submission.getArtifact(LoadedClasses.class);
		if (loadedClasses == null) {
			throw new InternalBuilderException(this.getClass(), "No LoadedClasses");
		}
		
		// Get TestCase list
		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}
		
		// Get the loaded Class for the Tester class
		Class<?> testerCls_;
		try {
			testerCls_ = loadedClasses.getClassLoader().loadClass("Tester");
		} catch (ClassNotFoundException e) {
			// LoadClassesBuildStep tries loading all compiled classes,
			// so this should not happen
			throw new InternalBuilderException(this.getClass(), "Unexpectedly failed to load TesterClass", e);
		}
		final Class<?> testerCls = testerCls_;
		
        // create a list of tasks to be executed
        List<IsolatedTask<TestResult>> tasks = new ArrayList<IsolatedTask<TestResult>>();
        for (final TestCase t : testCaseList) {
            tasks.add(new IsolatedTaskRunner(testerCls, problem, t));
        }

        KillableTaskManager<TestResult> pool = new KillableTaskManager<TestResult>(
                tasks, 
                TIMEOUT_LIMIT,
                new KillableTaskManager.TimeoutHandler<TestResult>() {
                    @Override
                    public TestResult handleTimeout() {
                        return new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
                                "Took too long!  Check for infinite loops, or recursion without a proper base case");
                    }
                });

        // run each task in a separate thread
        pool.run();

        // merge outcomes with their buffered inputs for stdout/stderr
        List<TestResult> outcomes = SandboxUtil.getStdoutStderr(pool);
        SubmissionResult result=new SubmissionResult(new CompilationResult(CompilationOutcome.SUCCESS));
        result.setTestResults(outcomes.toArray(new TestResult[outcomes.size()]));
        
        // Add the completed SubmissionResult
        submission.addArtifact(result);
	}

}
