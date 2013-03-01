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

package org.cloudcoder.builder2.batch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.app.shared.model.json.ReflectionFactory;
import org.cloudcoder.builder2.server.Builder2;
import org.cloudcoder.daemon.IOUtil;

/**
 * Front-end for batch-mode testing.
 * 
 * @author David Hovemeyer
 */
public class BatchMain {
	public static void main(String[] args) throws IOException {
		// Shut log4j up
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new NullAppender());
		
		if (args.length != 2) {
			System.err.println("Usage: java -jar cloudcoderBuilder.jar batch <exercise JSON> <source file list>");
			System.exit(1);
		}
		
		String exerciseJSON = args[0];
		String sourceFileListFilename = args[1];

		ProblemAndTestCaseList exercise = new ProblemAndTestCaseList();
		
		// Read the exercise (Problem and TestCases)
		Reader r = null;
		try {
			r = new BufferedReader(new FileReader(exerciseJSON));
			JSONConversion.readProblemAndTestCaseData(
					exercise,
					ReflectionFactory.forClass(Problem.class),
					ReflectionFactory.forClass(TestCase.class),
					r);
		} finally {
			IOUtil.closeQuietly(r);
		}
		TestCase[] testCaseList = exercise.getTestCaseList();
		
		// Read the list of source files to test
		List<String> sourceFileList = new ArrayList<String>();
		BufferedReader r2 = null;
		try {
			r2 = new BufferedReader(new FileReader(sourceFileListFilename));
			while (true) {
				String sourceFile = r2.readLine();
				if (sourceFile == null) {
					break;
				}
				sourceFileList.add(sourceFile);
			}
		} finally {
			IOUtil.closeQuietly(r2);
		}
		
		// Test each submission
		for (String sourceFile : sourceFileList) {
			// Read the program text
			FileReader fileReader = new FileReader(sourceFile);
			String programText;
			try {
				programText = IOUtils.toString(fileReader);
			} finally {
				IOUtil.closeQuietly(fileReader);
			}
			
			// Test the submission
			SubmissionResult result =
					Builder2.testSubmission(exercise.getProblem(), Arrays.asList(testCaseList), programText);
			
			// FIXME: For now, just a hard-coded output format summarizing compilation status and test results
			System.out.print(sourceFile);
			System.out.print(":");
			System.out.print(result.getCompilationResult().getOutcome());
			TestResult[] testResults = result.getTestResults();
			boolean allPassed = true;
			for (int i = 0; i < testCaseList.length; i++) {
				System.out.print(",");
				System.out.print(testCaseList[i].getTestCaseName());
				System.out.print("=");
				if (i >= testResults.length) {
					System.out.print("false");
				} else {
					boolean passed = testResults[i].getOutcome() == TestOutcome.PASSED;
					System.out.print(String.valueOf(passed));
					if (!passed) {
						allPassed = false;
					}
				}
			}
			System.out.println();
			
			// If the submission did not pass all tests,
			// write failure report to stderr.
			if (!allPassed) {
				writeFailureReport(sourceFile, result, testCaseList);
			}
		}
	}

	private static void writeFailureReport(String sourceFile, SubmissionResult result, TestCase[] testCaseList) {
		System.err.println("File: " + sourceFile);
		if (result.getCompilationResult().getOutcome() != CompilationOutcome.SUCCESS){
			System.err.println("Did not compile");
		}
		TestResult[] testResults = result.getTestResults();
		for (int i = 0; i < testResults.length; i++) {
			System.err.println(testCaseList[i].getTestCaseName());
			System.err.println(testResults[i].getStdout());
		}
	}
}
