package org.cloudcoder.builder2.commandrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.CommandResult;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProcessStatus;
import org.cloudcoder.builder2.util.StringUtil;
import org.cloudcoder.builder2.util.TestResultUtil;

/**
 * Check {@link CommandResult}s by checking each line of standard output
 * against a regular expression (specified in the corresponding
 * {@link TestCase}).  Creates an array of {@link TestResult}s as a
 * result artifact.
 * 
 * @author David Hovemeyer
 */
public class CheckCommandResultsUsingRegexBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		// Get Problem
		Problem problem = submission.getArtifact(Problem.class);
		if (problem == null) {
			throw new InternalBuilderException(this.getClass(), "No Problem");
		}
		
		// Get TestCase list
		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}
		
		// Get CommandResult list
		CommandResult[] commandResultList = submission.getArtifact(CommandResult[].class);
		if (commandResultList == null) {
			throw new InternalBuilderException(this.getClass(), "No CommandResult list");
		}
		
		// Create a TestResult for each TestCase/CommandResult
		TestResult[] testResultList = new TestResult[testCaseList.length];
		for (int i = 0; i < testCaseList.length; i++) {
			testResultList[i] = createTestResult(commandResultList[i], problem, testCaseList[i]);
		}
		
		// Add list of TestResults as artifact
		submission.addArtifact(testResultList);
	}
	
	private static final Pattern REGEX_OPTIONS = Pattern.compile("\\$([ij]+)$");

	private TestResult createTestResult(CommandResult commandResult, Problem problem, TestCase testCase) {
		// Check whether the command completed normally.
		if (commandResult.getStatus() != ProcessStatus.EXITED) {
			return TestResultUtil.createTestResultForAbnormalExit(commandResult, problem, testCase);
		}
		
		// Special case: if the stdout is completely empty, it is possible
		// that the expected output of the program is a blank line, and
		// thus empty output is correct.  So, if the stdout is empty,
		// change it to a single empty line.  (These are the kinds of
		// things you learn when you have actual students submitting code.)
		List<String> stdoutAsList = commandResult.getStdout();
		if (stdoutAsList.isEmpty()) {
			stdoutAsList = new ArrayList<String>();
			stdoutAsList.add("");
		}
		
		// Handle regex options.
		boolean caseInsensitive = false;
		boolean joinOutputLines = false;
		String regex = testCase.getOutput();
		Matcher optionsMatcher = REGEX_OPTIONS.matcher(regex);
		if (optionsMatcher.find()) {
			String options = optionsMatcher.group(1);
			if (options.contains("i")) {
				caseInsensitive = true;
			}
			if (options.contains("j")) {
				joinOutputLines = true;
			}
			regex = regex.substring(0, optionsMatcher.start());
		}
		
		// If the "j" regex option was specified, join all of the
		// output lines into a single line (with a single space
		// separating each original line).
		if (joinOutputLines) {
			String oneLine = StringUtil.mergeOneLine(stdoutAsList);
			stdoutAsList = new ArrayList<String>();
			stdoutAsList.add(oneLine);
		}
		
		// Scan through its output to see if there is a line
		// matching the test case output regular expression.
		boolean foundMatchingOutput = false;
		Pattern pat = Pattern.compile(regex, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
		for (String line : stdoutAsList) {
			Matcher m = pat.matcher(line);
			if (m.matches()) {
				// Match!
				foundMatchingOutput = true;
				break;
			}
		}
		
		return foundMatchingOutput
				? TestResultUtil.createTestResultForPassedTest(commandResult, problem, testCase)
				: TestResultUtil.createTestResultForFailedAssertion(commandResult, problem, testCase);
	}

}
