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

package org.cloudcoder.builder2.tester;

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.builder2.ccompiler.CCompilerBuildStep;
import org.cloudcoder.builder2.cfunction.AddCFunctionScaffoldingBuildStep;
import org.cloudcoder.builder2.cfunction.CheckCFunctionCommandResultsBuildStep;
import org.cloudcoder.builder2.cfunction.CreateCFunctionTestCommandsBuildStep;
import org.cloudcoder.builder2.cfunction.CreateSecretSuccessAndFailureCodesBuildStep;
import org.cloudcoder.builder2.commandrunner.CheckCommandResultsUsingRegexBuildStep;
import org.cloudcoder.builder2.commandrunner.CreateCommandInputsForEachTestCaseBuildStep;
import org.cloudcoder.builder2.commandrunner.CreateLimitedCommandExecutionPreferencesBuildStep;
import org.cloudcoder.builder2.commandrunner.ExecuteCommandForEachCommandInputBuildStep;
import org.cloudcoder.builder2.commandrunner.NativeExecutableToCommandForEachCommandInputBuildStep;
import org.cloudcoder.builder2.javacompiler.BytecodeToBytecodeExecutableBuildStep;
import org.cloudcoder.builder2.javacompiler.JavaCompilerBuildStep;
import org.cloudcoder.builder2.javacompiler.LoadClassesBuildStep;
import org.cloudcoder.builder2.javamethod.AddJavaMethodScaffoldingBuildStep;
import org.cloudcoder.builder2.javamethod.AddJavaMethodTestDriverBuildStep;
import org.cloudcoder.builder2.javamethod.ExecuteJavaMethodTestsBuildStep;
import org.cloudcoder.builder2.javaprogram.JavaProgramToCommandForEachCommandInputBuildStep;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.Tester;
import org.cloudcoder.builder2.pythonfunction.TestPythonFunctionBuildStep;
import org.cloudcoder.builder2.rubymethod.AddRubyMethodScaffoldingBuildStep;
import org.cloudcoder.builder2.rubymethod.TestRubyMethodBuildStep;
import org.cloudcoder.builder2.submissionresult.CreateSubmissionResultBuildStep;

/**
 * Factory to create {@link Tester} objects.
 * 
 * @author David Hovemeyer
 */
public abstract class TesterFactory {
	/**
	 * Get a {@link Tester} for given {@link ProblemType}.
	 * 
	 * @param problemType the {@link ProblemType}
	 * @return the {@link Tester}, or null if there is no tester for this problem type
	 */
	public static Tester getTester(ProblemType problemType) {
		return PROBLEM_TYPE_TO_TESTER_MAP.get(problemType);
	}
	
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#C_PROGRAM}
	 * submission.
	 */
	private static final IBuildStep[] C_PROGRAM_TESTER_STEPS = {
		new CCompilerBuildStep(),
		new CreateLimitedCommandExecutionPreferencesBuildStep(),
		new CreateCommandInputsForEachTestCaseBuildStep(),
		new NativeExecutableToCommandForEachCommandInputBuildStep(),
		new ExecuteCommandForEachCommandInputBuildStep(),
		new CheckCommandResultsUsingRegexBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#C_FUNCTION}
	 * submission.
	 */
	private static final IBuildStep[] C_FUNCTION_TESTER_STEPS = {
		new AddCFunctionScaffoldingBuildStep(),
		new CCompilerBuildStep(),
		new CreateLimitedCommandExecutionPreferencesBuildStep(),
		new CreateSecretSuccessAndFailureCodesBuildStep(),
		new CreateCFunctionTestCommandsBuildStep(),
		new ExecuteCommandForEachCommandInputBuildStep(),
		new CheckCFunctionCommandResultsBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#JAVA_PROGRAM}
	 * submission.
	 */
	private static final IBuildStep[] JAVA_PROGRAM_TESTER_STEPS = {
		new JavaCompilerBuildStep(),
		new BytecodeToBytecodeExecutableBuildStep(),
		new CreateCommandInputsForEachTestCaseBuildStep(),
		new JavaProgramToCommandForEachCommandInputBuildStep(),
		new ExecuteCommandForEachCommandInputBuildStep(),
		new CheckCommandResultsUsingRegexBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#JAVA_METHOD}
	 * submission.
	 */
	private static final IBuildStep[] JAVA_METHOD_BUILD_STEPS = {
		new AddJavaMethodScaffoldingBuildStep(),
		new AddJavaMethodTestDriverBuildStep(),
		new JavaCompilerBuildStep(),
		new LoadClassesBuildStep(),
		new ExecuteJavaMethodTestsBuildStep(),
	};
	
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#PYTHON_FUNCTION}
	 * submission.
	 */
	private static final IBuildStep[] PYTHON_FUNCTION_BUILD_STEPS = {
		new TestPythonFunctionBuildStep(),
	};
	
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#RUBY_METHOD}
	 * submission.
	 */
	private static final IBuildStep[] RUBY_METHOD_BUILD_STEPS = {
		new AddRubyMethodScaffoldingBuildStep(),
		new TestRubyMethodBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	/**
	 * Create a {@link Tester} with the given list of {@link IBuildStep}s.
	 * 
	 * @param stepList list of {@link IBuildStep}s
	 * @return the {@link Tester}
	 */
	private static Tester createTester(IBuildStep[] stepList) {
		Tester tester = new Tester();
		for (IBuildStep buildStep : stepList) {
			tester.addBuildStep(buildStep);
		}
		return tester;
	}
	
	/**
	 * Map of {@link ProblemType} values to {@link Tester} objects.
	 */
	private static final Map<ProblemType, Tester> PROBLEM_TYPE_TO_TESTER_MAP = new HashMap<ProblemType, Tester>();
	static {
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.C_PROGRAM, createTester(C_PROGRAM_TESTER_STEPS));
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.C_FUNCTION, createTester(C_FUNCTION_TESTER_STEPS));
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.JAVA_PROGRAM, createTester(JAVA_PROGRAM_TESTER_STEPS));
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.JAVA_METHOD, createTester(JAVA_METHOD_BUILD_STEPS));
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.PYTHON_FUNCTION, createTester(PYTHON_FUNCTION_BUILD_STEPS));
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.RUBY_METHOD, createTester(RUBY_METHOD_BUILD_STEPS));
	}
}
