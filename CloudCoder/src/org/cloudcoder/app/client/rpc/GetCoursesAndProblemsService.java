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

package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * RPC service for getting {@link Course}s and {@link Problem}s
 * for the currently-authenticated client.
 */
@RemoteServiceRelativePath("getCoursesAndProblems")
public interface GetCoursesAndProblemsService extends RemoteService {
	/**
	 * Get the courses the client user is registered for.
	 * 
	 * @return list of courses the client user is registered for
	 * @throws CloudCoderAuthenticationException if the client is not authenticated
	 */
	public Course[] getCourses() throws CloudCoderAuthenticationException;
	
	/**
	 * Get {@link CourseAndCourseRegistration} objects representing
	 * all of the courses the client is registered for, along with
	 * registration information (such as instructor status) for each
	 * course.
	 * 
	 * @return list of {@link CourseAndCourseRegistration} objects for client
	 * @throws CloudCoderAuthenticationException if the client is not authenticated
	 */
	public CourseAndCourseRegistration[] getCourseAndCourseRegistrations() throws CloudCoderAuthenticationException;
	
	/**
	 * Get {@link Problem}s available in given {@link Course}.
	 * The client must be registered in the Course.
	 * 
	 * @param course the Course
	 * @return the Problems available in the course
	 * @throws CloudCoderAuthenticationException if the client is not authenticated,
	 *         or is not regsitered in the course 
	 */
	public Problem[] getProblems(Course course) throws CloudCoderAuthenticationException;
	
	/**
	 * Get {@link ProblemAndSubmissionReceipt}s for given {@link Course}.
	 * This allows the client to get not only the Problems in the Course,
	 * but also the most recent {@link SubmissionReceipt} for each Problem.
	 * The client must be registered in the Course.
	 * 
	 * @param course the Course
	 * @return the ProblemAndSubmissionReceipts for the client's work in the Course
	 * @throws CloudCoderAuthenticationException if the client is not authenticated,
	 *         or is not regsitered in the course 
	 */
	public ProblemAndSubmissionReceipt[] getProblemAndSubscriptionReceipts(Course course) throws CloudCoderAuthenticationException;
	
	/**
	 * @param course
	 * @param user
	 * @return
	 * @throws NetCoderAuthenticationException
	 */
	public ProblemAndSubmissionReceipt[] getProblemAndSubscriptionReceipts(Course course, User user) throws CloudCoderAuthenticationException;
	
	/**
	 * Get the list of {@link TestCase}s for a {@link Problem}.
	 * This will only succeed if the authenticated user is an instructor in the course
	 * the problem is assigned in.
	 * 
	 * @param problemId the id of the Problem
	 * @return the list of TestCases for the Problem
	 */
	public TestCase[] getTestCasesForProblem(int problemId) throws CloudCoderAuthenticationException;
	
	/**
	 * Store given {@link ProblemAndTestCaseList} in the database.
	 * The authenticated user must be registered as an instructor for the course
	 * in which the problem is assigned.
	 * If the problem already has a valid problem id, it will be updated.
	 * If the problem does not yet have a valid problem id, it will be inserted.
	 * 
	 * @param problemAndTestCaseList the ProblemAndTestCaseList to store
	 * @return the updated ProblemAndTestCaseList (which may have had ids assigned, etc.)
	 */
	public ProblemAndTestCaseList storeProblemAndTestCaseList(
			ProblemAndTestCaseList problemAndTestCaseList,
			Course course) throws CloudCoderAuthenticationException;
	
	/**
	 * Submit an exercise (problem and testcases) to the exercise repository.
	 * 
	 * @param exercise the exercise to submit
	 * @param repoUsername the repository username
	 * @param repoPassword the repository password
	 */
	public OperationResult submitExercise(ProblemAndTestCaseList exercise, String repoUsername, String repoPassword)
		throws CloudCoderAuthenticationException;
	
	/**
	 * Import an exercise (problem and testcases) from the exercise repository.
	 * The currently-authenticated user must be an instructor in the course.
	 * 
	 * @param course the {@link Course} to import the exercise into
	 * @param exerciseHash the hash of the execise to import
	 * @return the exercise, or null if no such exercise could be found in the repository
	 */
	public ProblemAndTestCaseList importExercise(Course course, String exerciseHash) throws CloudCoderAuthenticationException;
	
	/**
	 * Delete a problem (and its test cases) from the local database.
	 * The currently-authenticated user must be an instructor in the course.
	 */
	public OperationResult deleteProblem(Course course, Problem problem) throws CloudCoderAuthenticationException;
	
	/**
	 * Start a quiz.
	 * 
	 * @param problem  the {@link Problem} to administer as a quiz
	 * @param section  the section in which to administer the quiz
	 * @return the {@link Quiz}
	 * @throws CloudCoderAuthenticationException if the current user is not authorized
	 *         to give a quiz for the {@link Problem}/section
	 */
	public Quiz startQuiz(Problem problem, int section) throws CloudCoderAuthenticationException;
	
	/**
	 * Find out if there is a current (ongoing) {@link Quiz} for the
	 * given {@link Problem} in a section for which the currently-authenticated
	 * user is an instructor.  Note that there is an implicit assumption
	 * that instructors of multiple sections will give a quiz in at most
	 * one section at a time.
	 * 
	 * Note that the if a non-null value is returned, the end time field
	 * of the returned quiz will contain the current server-side time: this
	 * can be used to compute how long the quiz has been ongoing. 
	 * 
	 * @param problem the {@link Problem}
	 * @return the current (ongoing) {@link Quiz}, or null if there is no
	 *         quiz, or if the current user is not authorized to administer
	 *         quizzes in the course
	 * @throws CloudCoderAuthenticationException if there is no authenticated user in
	 *         the server-side session
	 */
	public Quiz findCurrentQuiz(Problem problem) throws CloudCoderAuthenticationException;
	
	/**
	 * End given {@link Quiz}.
	 * 
	 * @param quiz the {@link Quiz} to end
	 * @return true if successful, false if not (for example, if there is no such quiz)
	 * @throws CloudCoderAuthenticationException if there is no authenticated user in
	 *         the server-side session 
	 */
	public Boolean endQuiz(Quiz quiz) throws CloudCoderAuthenticationException;
}
