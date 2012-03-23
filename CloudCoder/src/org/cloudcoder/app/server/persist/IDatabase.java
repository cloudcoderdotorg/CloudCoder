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

package org.cloudcoder.app.server.persist;

import java.util.List;
import java.util.Properties;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.ProblemSummary;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Thin abstraction layer for interactions with database.
 * 
 * @author David Hovemeyer
 */
public interface IDatabase {
	public ConfigurationSetting getConfigurationSetting(ConfigurationSettingName name);
	
	/**
	 * Authenticate a user.  If props is null, empty, or does not contain
	 * a valid or recognized loginService, then we assume that passwords
	 * are stored directly in the database.
	 * 
	 * @param userName  the username
	 * @param password  the password
	 * @param props     properties file containing additional properties requires for
	 *     authentication (IMAP settings, LDAP settings, etc)
	 * @return the authenticated User, or null if the username/password doesn't correspond to a known user
	 */
	public User authenticateUser(String userName, String password, Properties props);
	public Problem getProblem(User user, int problemId);

	/**
	 * Get Problem with given problem id.
	 * 
	 * @param problemId the problem id
	 * @return the Problem with that problem id, or null if there is no such Problem
	 */
	public Problem getProblem(int problemId);
	
	public Change getMostRecentChange(User user, int problemId);
	public Change getMostRecentFullTextChange(User user, int problemId);
	public List<Change> getAllChangesNewerThan(User user, int problemId, int baseRev);
	
	/**
	 * Get all of the courses in which given user is registered.
	 * Each returned item is a triple consisting of {@link Course},
	 * {@link Term}, and {@link CourseRegistration}.
	 * 
	 * @param user the User
	 * @return list of triples (Course, Term, CourseRegistration)
	 */
	public List<? extends Object[]> getCoursesForUser(User user);
	public ProblemList getProblemsInCourse(User user, Course course);
	public List<ProblemAndSubmissionReceipt> getProblemAndSubscriptionReceiptsInCourse(User user, Course course);
	public void storeChanges(Change[] changeList);
	public List<TestCase> getTestCasesForProblem(int problemId);
	public void insertSubmissionReceipt(SubmissionReceipt receipt, TestResult[] testResultList);
	public void getOrAddLatestSubmissionReceipt(User user, Problem problem);
	public void addProblem(Problem problem);
	public void addTestCases(Problem problem, List<TestCase> testCaseList);

	/**
	 * Create a {@link ProblemSummary} describing the submissions for
	 * the given {@link Problem}.
	 * 
	 * @param problem the Problem
	 * @return a ProblemSummary describing the submissions for the Problem
	 */
	public ProblemSummary createProblemSummary(Problem problem);

	/**
	 * Get SubmissionReceipt with given id.
	 * 
	 * @param submissionReceiptId the submission receipt id
	 * @return the SubmissionReceipt with the given id, or null if there is no such
	 *         SubmissionReceipt
	 */
	public SubmissionReceipt getSubmissionReceipt(int submissionReceiptId);

	/**
	 * Get the Change with given id.
	 * 
	 * @param changeId the event id of the Change
	 * @return the Change with the given event id
	 */
	public Change getChange(int changeEventId);

	/**
	 * Insert TestResults.
	 * 
	 * @param testResults         the TestResults
	 * @param submissionReceiptId the id of the SubmissionReceipt with which these
	 *                            TestResults are associated
	 */
	public void replaceTestResults(TestResult[] testResults, int submissionReceiptId);

	/**
	 * Update a SubmissionReceipt.  This can be useful if the submission
	 * was tested incorrectly and the receipt is being updated following
	 * a retest.
	 * 
	 * @param receipt the SubmissionReceipt to update
	 */
	public void updateSubmissionReceipt(SubmissionReceipt receipt);
}
