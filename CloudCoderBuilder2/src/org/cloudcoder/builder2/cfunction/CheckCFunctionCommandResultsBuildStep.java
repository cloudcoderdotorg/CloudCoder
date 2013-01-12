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

package org.cloudcoder.builder2.cfunction;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.CommandResult;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProcessStatus;
import org.cloudcoder.builder2.util.TestResultUtil;

/**
 * For each {@link CommandResult} produced by a scaffolded {@link ProblemType#C_FUNCTION}
 * executable, check the result to see if the test passed or failed.
 * Creates an array of {@link TestResult} objects (one for each
 * {@link CommandResult}.
 * 
 * @author David Hovemeyer
 */
public class CheckCFunctionCommandResultsBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		CommandResult[] commandResultList = submission.getArtifact(CommandResult[].class);
		if (commandResultList == null) {
			throw new InternalBuilderException(this.getClass(), "No CommandResult list");
		}
		
		Problem problem = submission.getArtifact(Problem.class);
		if (problem == null) {
			throw new InternalBuilderException(this.getClass(), "No Problem");
		}
		
		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}
		
		SecretSuccessAndFailureCodes codes = submission.getArtifact(SecretSuccessAndFailureCodes.class);
		if (codes == null) {
			throw new InternalBuilderException(this.getClass(), "No SecretSuccessAndFailureCodes");
		}
		
		TestResult[] testResultList = new TestResult[commandResultList.length];
		
		for (int i = 0; i < commandResultList.length; i++) {
			if (commandResultList[i].getStatus() != ProcessStatus.EXITED) {
				// Abnormal process exit
				testResultList[i] = TestResultUtil.createTestResultForAbnormalExit(commandResultList[i], problem, testCaseList[i]);
			} else {
				// Check exit code
				int exitCode = commandResultList[i].getExitCode();
				if (exitCode == codes.getSuccessCode()) {
					testResultList[i] = TestResultUtil.createTestResultForPassedTest(commandResultList[i], problem, testCaseList[i]);
				} else {
					testResultList[i] = TestResultUtil.createTestResultForFailedAssertion(commandResultList[i], problem, testCaseList[i]);
				}
			}
		}
		
		submission.addArtifact(testResultList);
	}

}
