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

import java.io.InputStream;
import java.util.List;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.IModelObject;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.ProblemSummary;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.RepoProblemSearchCriteria;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.StartedQuiz;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.UserRegistrationRequest;

/**
 * Thin abstraction layer for interactions with database.
 * 
 * @author David Hovemeyer
 */
public interface IDatabase {
	/**
	 * Get a configuration setting.
	 * 
	 * @param name the {@link ConfigurationSettingName}
	 * @return the {@link ConfigurationSetting}, or null if there is no such setting
	 */
	public ConfigurationSetting getConfigurationSetting(ConfigurationSettingName name);
	
	/**
	 * Authenticate a user.
	 * 
	 * @param userName  the username
	 * @param password  the password
	 * @return the authenticated User, or null if the username/password doesn't correspond to a known user
	 */
	public User authenticateUser(String userName, String password);
	
	/**
	 * Look up a user by user name, <em>without authentication</em>.
	 * 
	 * @param userName  the username
	 * @return the User corresponding to the username, or null if there is no such user
	 */
	public User getUserWithoutAuthentication(String userName);
	
	/**
	 * Get the {@link Problem} with given problem id.
	 * Checks that the {@link User} has permission to see the problem.
	 * Returns a {@link Pair} containing the problem, and if appropriate
	 * a {@link Quiz}.  The quiz is returned only if the user has
	 * permission to see the problem because of an ongoing quiz.
	 * 
	 * @param user      the {@link User}
	 * @param problemId the problem id
	 * @return the {@link Pair} containing the problem and (maybe) quiz;
	 *         null if there is no such problem, or if the user is not
	 *         permitted to see the problem
	 */
	public Pair<Problem, Quiz> getProblem(User user, int problemId);

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
	
	/**
	 * Return a {@link ProblemList} containing all of the {@link Problem}s in a particular
	 * {@link Course} that the given {@link User} has permission to see.
	 * 
	 * @param user    the User
	 * @param course  the Course
	 * @return the ProblemList containing the Problems that the user has permission to see
	 */
	public ProblemList getProblemsInCourse(User user, Course course);
	
	/**
	 * Get list of {@link ProblemAndSubmissionReceipt}s for the problems the
	 * given {@link User} is allowed to see in the given {@link Course} and {@link Module}.
	 *   
	 * @param user    the authenticated User
	 * @param course  the Course
	 * @param forUser the user to get the {@link ProblemAndSubmissionReceipt}s for
	 * @param module  the Module (if null, then all problems are returned)
	 * @return list of {@link ProblemAndSubmissionReceipt}s
	 */
	public List<ProblemAndSubmissionReceipt> getProblemAndSubscriptionReceiptsInCourse(User user, Course course, User forUser, Module module);
	
	public void storeChanges(Change[] changeList);
	
	/**
	 * Get List of {@link TestCase}s for {@link Problem} with given id.
	 * Note that no authentication is done to ensure that the caller
	 * should be able to access the test cases.
	 * 
	 * @param problemId the Problem id
	 * @return list of TestCases for the Problem
	 */
	public List<TestCase> getTestCasesForProblem(int problemId);

	/**
	 * Get the list of {@link TestCase}s for {@link Problem} with given id,
	 * checking that the given authenticated {@link User} is allowed to access
	 * the test cases for the problem.
	 * 
	 * @param authenticatedUser the authenticated User 
	 * @param problemId         the Problem id
	 * @return list of test cases, or null if the user is not authorized to access the test cases
	 *         (i.e., is not an instructor for the {@link Course} in which the problem is assigned)
	 */
	public TestCase[] getTestCasesForProblem(User authenticatedUser, int problemId);
	
	public void insertSubmissionReceipt(SubmissionReceipt receipt, TestResult[] testResultList);
	public void getOrAddLatestSubmissionReceipt(User user, Problem problem);
	public void addProblem(Problem problem);
	public void addTestCases(Problem problem, List<TestCase> testCaseList);

	public void insertUsersFromInputStream(InputStream in, Course course);
	
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
	 * Return a list of all users in the given course.
	 * @param sectionNumber 
	 * 
	 * @param course The course for which we want all users.
	 * @param sectionNumber the section of the course (0 for all sections)
	 * @return A lot of all users inthe given course.
	 */
	public List<User> getUsersInCourse(int courseId, int sectionNumber);
	
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

	/**
	 * Store given {@link ProblemAndTestCaseList} in the database.
	 * If the problem exists, the existing problem data and test cases will be updated.
	 * If the problem doesn't exist yet, it (and its test cases) will be created.
	 * The {@link User} must be registered as an instructor for the {@link Course}
	 * in which the problem is (or will be) assigned.
	 * 
	 * @param problemAndTestCaseList the problem and test cases to be stored (updated or inserted)
	 * @param course the course in which the problem is (or will be) assigned
	 * @param user the authenticated user
	 * @return updated ProblemAndTestCaseList
	 * @throws CloudCoderAuthenticationException if the user is not an instructor in the course)
	 */
	public ProblemAndTestCaseList storeProblemAndTestCaseList(ProblemAndTestCaseList problemAndTestCaseList, Course course, User user)
		throws CloudCoderAuthenticationException;

	/**
	 * Get a {@link RepoProblemAndTestCaseList} from the database.
	 * 
	 * @param hash the hash of the problem and its associated test cases
	 * @return the {@link RepoProblemAndTestCaseList}, or null if no such object exists in the database
	 */
	public RepoProblemAndTestCaseList getRepoProblemAndTestCaseList(String hash);

	/**
	 * Store a {@link RepoProblemAndTestCaseList} in the database.
	 * 
	 * @param exercise the {@link RepoProblemAndTestCaseList} to store
	 * @param user     the {@link User} who is importing the problem into the database
	 */
	public void storeRepoProblemAndTestCaseList(RepoProblemAndTestCaseList exercise, User user);

	/**
	 * Search the repository database for {@link RepoProblem}s matching given criteria.
	 * 
	 * @param searchCriteria the search criteria
	 * @return the problems that matched the search criteria
	 */
	public List<RepoProblemSearchResult> searchRepositoryExercises(RepoProblemSearchCriteria searchCriteria);

	/**
	 * Find all {@link CourseRegistration}s for given user in given course.
	 * There can be more than one: for example, if the user is an instructor
	 * for multiple sections of the same course.
	 * 
	 * @param user    the user
	 * @param course  the course
	 * @return list of {@link CourseRegistration}s, which will be empty if
	 *         the user is not registered for the course
	 */
	public CourseRegistrationList findCourseRegistrations(User user, Course course);

    /**
     * Add a new user record to the database, and register that person
     * for the course indicated by the given courseId.  The registration
     * will have the given course registration type and will be for the
     * section indicated.
     * 
     * @param authenticatedUser the currently-authenticated {@link User}, who must be
     *                          an instructor in the course
     * @param courseId          the course id
     * @param editedUser        the {@link EditedUser} containing the information about the
     *                          new user to add
     * @throws CloudCoderAuthenticationException if the authenticated user is not an instructor in the course
     */
    public void addUserToCourse(User authenticatedUser, int courseId, EditedUser editedUser) throws CloudCoderAuthenticationException;

    /**
     * Edit a user record in the database.  Any blank fields will
     * remain unchanged.
     * 
     * @param id
     * @param username
     * @param firstname
     * @param lastname
     * @param email
     * @param passwd
     */
    public void editUser(int id, String username, String firstname, String lastname,
        String email, String passwd);
    
    /**
     * 
     * @param user
     */
    public void editUser(User user);

    /**
     * Edit the registration type for the user record indicated by the
     * userId and the course indicated by the given courseId.
     * 
     * @param userId
     * @param courseId
     * @param type
     */
    public void editRegistrationType(int userId, int courseId,
        CourseRegistrationType type);
	
	/**
	 * Get best submission receipts for given {@link Problem} in given {@link Course}.
	 * Should not be called unless the currently-authenticated user is an
	 * instructor in the course.
	 * 
	 * @param course   the {@link Course}
	 * @param section  the section number of the course (0 for all sections)
	 * @param problem  the {@link Problem}
	 * @return list of {@link UserAndSubmissionReceipt} objects
	 */
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(Course course, int section, Problem problem);

	/**
	 * Get best submission receipts for given {@link Problem}.
	 * Returns empty list if the authenticated user is not an instructor
	 * in the course in which the problem is assigned.
	 * 
	 * @param problem           the {@link Problem}
	 * @param section           the section number (0 for all sections)
	 * @param authenticatedUser the authenticated {@link User}
	 * @return list of best submission receipts for each user in course
	 */
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(Problem problem, int section, User authenticatedUser);

	/**
	 * Delete a problem (and its test cases).
	 * The user must be an instructor in the course the problem belongs to.
	 * 
	 * @param user       the authenticated {@link User}
	 * @param course     the course
	 * @param problem    the problem
	 * @return true if the problem was deleted successfully, false otherwise
	 */
	public boolean deleteProblem(User user, Course course, Problem problem) throws CloudCoderAuthenticationException;
	
	/**
	 * Run a database transaction.
	 * 
	 * @param databaseRunnable the database transaction to run
	 * @return the result of the database transaction
	 * @throws PersistenceException if an error occurs
	 */
	public<E> E databaseRun(AbstractDatabaseRunnableNoAuthException<E> databaseRunnable);
	
	/**
	 * Run a database transaction that can throw an authorization exception.
	 * 
	 * @param databaseRunnable the database transaction to run
	 * @return the result of the database transaction
	 * @throws PersistenceException if an error occurs
	 * @throws CloudCoderAuthenticationException if an authorization exception occurs
	 */
	public<E> E databaseRunAuth(AbstractDatabaseRunnable<E> databaseRunnable) throws CloudCoderAuthenticationException;

	/**
	 * Add a {@link UserRegistrationRequest} to the database.
	 * 
	 * @param request the {@link UserRegistrationRequest} to add
	 * @return an {@link OperationResult} indicating whether adding the request succeeded or failed
	 */
	public OperationResult addUserRegistrationRequest(UserRegistrationRequest request);

	/**
	 * Find the {@link UserRegistrationRequest} corresponding to given secret.
	 * 
	 * @param secret the secret
	 * @return the {@link UserRegistrationRequest} corresponding to the secret, or null if there
	 *         is no such request
	 */
	public UserRegistrationRequest findUserRegistrationRequest(String secret);

	/**
	 * Complete a {@link UserRegistrationRequest}.
	 * 
	 * @param request the {@link UserRegistrationRequest} to complete
	 * @return an {@link OperationResult} describing the success or failure: if successful,
	 *         it means that a new user account has been created
	 */
	public OperationResult completeRegistration(UserRegistrationRequest request);

	public User getUserGivenId(int userId);

	/**
	 * Get the most popular tags for given {@link RepoProblem}.
	 * Note that the tags returned are "aggregate" tags, meaning that
	 * they represent all of the users who added a tag to a particular
	 * problem.  As such, they contain a valid user count that can
	 * be retrieved by calling {@link RepoProblemTag#getCount()}.
	 * 
	 * @param repoProblemId the unique id of the {@link RepoProblem}
	 * @return the most popular tags
	 */
	public List<RepoProblemTag> getProblemTags(int repoProblemId);

	/**
	 * Add a {@link RepoProblemTag} which records a {@link User}'s tagging
	 * of a repository exercise.
	 * 
	 * @param repoProblemTag the {@link RepoProblemTag} to add
	 * @return true if adding the tag succeeded, false
	 *         if the user has already added an identical tag
	 */
	public boolean addRepoProblemTag(RepoProblemTag repoProblemTag);

	/**
	 * Given a search term (partial tag name), suggest possible repository tag names.
	 *  
	 * @param term a search term (partial tag name)
	 * @return list of possible tag names matching the search term
	 */
	public List<String> suggestTagNames(String term);

	/**
	 * Start a {@link Quiz} for given {@link Problem} in given course section.
	 * 
	 * @param user    the authenticated {@link User}, who must be an instructor
	 *                in the course/section
	 * @param problem the {@link Problem} to give as a quiz
	 * @param section the course section
	 * @return the {@link Quiz}
	 * @throws CloudCoderAuthenticationException if the user is not authorized to give a quiz
	 *         in the course/section
	 */
	public Quiz startQuiz(User user, Problem problem, int section) throws CloudCoderAuthenticationException;

	/**
	 * Find a current (ongoing) quiz being administed for the given
	 * {@link Problem} in a course section in which the given {@link User}
	 * is an instructor.
	 * 
	 * @param user     the {@link User}
	 * @param problem  the {@link Problem}
	 * @return the {@link Quiz}, or null if there is no such quiz
	 */
	public Quiz findCurrentQuiz(User user, Problem problem);

	/**
	 * End given quiz.
	 * 
	 * @param user  the authenticated {@link User}, who must be an instructor
	 *              in the course/section in which the quiz is being administered
	 * @param quiz  the {@link Quiz}
	 * @return true if the quiz was successfully ended, false if not
	 */
	public Boolean endQuiz(User user, Quiz quiz);

	/**
	 * Reload a model object's fields from the database
	 * using its assigned unique id.
	 * 
	 * @param obj the model object to reload
	 * @return true if successful, false if object could not be located by its unique id
	 */
	public<E extends IModelObject<E>> boolean reloadModelObject(E obj);

	/**
	 * Get all of the {@link Module}s of the {@link Problem}s that are assigned
	 * in the given {@link Course}.  Only returns modules if the user is confirmed
	 * to be registered in the given course.  Note that modules of non-visible
	 * problems <em>will</em> be returned. 
	 * 
	 * @param user    the authenticated user
	 * @param course  the course
	 * @return the modules in the course
	 */
	public Module[] getModulesForCourse(User user, Course course);

	/**
	 * Set the {@link Module} in which the given {@link Problem} is categorized.
	 * The {@link User} must be an instructor in the course in which the
	 * problem is assigned.
	 * 
	 * @param user        the authenticated {@link User}
	 * @param problem     the {@link Problem}
	 * @param moduleName  the name of the {@link Module} in which the problem should
	 *                    be categorized
	 * @return
	 */
	public Module setModule(User user, Problem problem, String moduleName) throws CloudCoderAuthenticationException;

	/**
	 * Start or continue a user's work on a quiz.
	 * Ensures that a {@link StartedQuiz} object exists for the user/quiz.
	 * 
	 * @param user     the {@link User}
	 * @param quiz     the {@link Quiz}
	 * @return the {@link StartedQuiz} that indicates that the user has started
	 *         the quiz
	 */
	public StartedQuiz startOrContinueQuiz(User user, Quiz quiz);

	/**
	 * Find out whether the given {@link User} has started a {@link Quiz},
	 * but has not finished it.
	 * 
	 * @param user the {@link User}
	 * @return the {@link StartedQuiz} object specifying the unfinished quiz,
	 *         or null if there is no unfinished quiz for this user
	 */
	public StartedQuiz findUnfinishedQuiz(User user);

	/**
	 * Get all sections for given {@link Course}.
	 * 
	 * @param course            the course
	 * @param authenticatedUser the authenticated {@link User}, who must be
	 *                          an instructor in the course
	 * @return the sections, or an empty array if the user is not an instructor in the course
	 */
	public Integer[] getSectionsForCourse(Course course, User authenticatedUser);

}
