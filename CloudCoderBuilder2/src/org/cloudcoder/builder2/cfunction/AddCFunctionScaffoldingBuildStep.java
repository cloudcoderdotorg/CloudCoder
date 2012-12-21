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
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.StringUtil;

/**
 * Add test scaffolding to C program source consisting of a single
 * function to add a main function that calls the function
 * for each test case, checks it against the expected
 * return value, and returns an exit value indicating success
 * or failure.  This is used for {@link ProblemType#C_FUNCTION}
 * submissions.  It works by replacing the {@link ProgramSource}
 * submission artifact with the scaffolded version.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class AddCFunctionScaffoldingBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {

		ProgramSource[] programSourceList = submission.getArtifact(ProgramSource[].class);
		if (programSourceList == null) {
			throw new InternalBuilderException(this.getClass(), "No ProgramSource list");
		}
		
		if (programSourceList.length != 1) {
			throw new InternalBuilderException(this.getClass(), "C_FUNCTION problems only support a single source file");
		}

		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}

		Problem problem = submission.getArtifact(Problem.class);
		if (problem == null) {
			throw new InternalBuilderException(this.getClass(), "No Problem");
		}

		String programText = programSourceList[0].getProgramText();
		if (!programText.endsWith("\n")) {
			programText = programText + "\n";
		}

		int prologueLength = 3;
		int programTextLength = StringUtil.countLines(programText);

		StringBuilder test = new StringBuilder();
		test.append("#include <string.h>\n");  // 3 lines of prologue
		test.append("#include <stdlib.h>\n");
		test.append("#include <stdio.h>\n");

		// The program text is the user's function
		test.append(programText);
		test.append("\n");

		// The eq macro will test the function's return value against
		// the expected return value.
		test.append("#undef eq\n");
		test.append("#define eq(a,b) ((a) == (b))\n");

		// Generate a main() function which can run all of the test cases.
		// argv[1] specifies the test case to execute by name.
		// argv[2] and argv[3] specify the exit values to use to indicate
		// whether or not the tested function's return value matched the
		// expected value.
		test.append("int main(int argc, char ** argv) {\n");
		test.append("  int rcIfEqual = atoi(argv[2]);\n");
		test.append("  int rcIfNotEqual = atoi(argv[3]);\n");
		// Make it a bit harder to steal the exit codes
		test.append("  argv[2] = 0;\n");
		test.append("  argv[3] = 0;\n");

		// Generate calls to execute test cases.
		for (TestCase t : testCaseList) {
			test.append("  if (strncmp(argv[1], \"" +t.getTestCaseName()+"\", "+
					t.getTestCaseName().length()+")==0) {\n");
			test.append("    return eq("+problem.getTestname()+
					"("+t.getInput()+"), ("+t.getOutput()+")) ? rcIfEqual : rcIfNotEqual;\n");
			test.append("  }\n");
		}

		// We return 99 if an invalid test case was provided: shouldn't
		// happen in practice.
		test.append("  return 99;\n");
		test.append("}\n");
		String result = test.toString();
		System.out.println(result);

		int epilogueLength = StringUtil.countLines(result) - programTextLength - prologueLength;

		// Create new ProgramSource artifact with scaffolded source
		ProgramSource scaffoldedProgramSource = new ProgramSource(result, prologueLength, epilogueLength);
		submission.addArtifact(new ProgramSource[]{scaffoldedProgramSource});
	}

}
