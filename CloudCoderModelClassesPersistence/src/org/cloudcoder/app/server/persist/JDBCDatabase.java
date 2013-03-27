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

package org.cloudcoder.app.server.persist;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.cloudcoder.app.server.persist.txn.AddRepoProblemTag;
import org.cloudcoder.app.server.persist.txn.AddTestCasesToProblem;
import org.cloudcoder.app.server.persist.txn.AddUserRegistrationRequest;
import org.cloudcoder.app.server.persist.txn.AddUserToCourse;
import org.cloudcoder.app.server.persist.txn.AuthenticateUser;
import org.cloudcoder.app.server.persist.txn.CompleteRegistration;
import org.cloudcoder.app.server.persist.txn.CreateProblemSummary;
import org.cloudcoder.app.server.persist.txn.DeleteProblem;
import org.cloudcoder.app.server.persist.txn.EditUser;
import org.cloudcoder.app.server.persist.txn.EditUserGivenUserData;
import org.cloudcoder.app.server.persist.txn.EndQuiz;
import org.cloudcoder.app.server.persist.txn.FindCourseRegistrationsGivenUserAndCourse;
import org.cloudcoder.app.server.persist.txn.FindCourseRegistrationsGivenUserAndCourseId;
import org.cloudcoder.app.server.persist.txn.FindCurrentQuiz;
import org.cloudcoder.app.server.persist.txn.FindUnfinishedQuizForStudent;
import org.cloudcoder.app.server.persist.txn.FindUserRegistrationRequestGivenSecret;
import org.cloudcoder.app.server.persist.txn.GetAllChangesNewerThan;
import org.cloudcoder.app.server.persist.txn.GetAllSubmissionReceiptsForUserAndProblem;
import org.cloudcoder.app.server.persist.txn.GetBestSubmissionReceiptsForProblem;
import org.cloudcoder.app.server.persist.txn.GetBestSubmissionReceiptsForProblemForAuthenticatedUser;
import org.cloudcoder.app.server.persist.txn.GetChangeGivenChangeEventId;
import org.cloudcoder.app.server.persist.txn.GetConfigurationSetting;
import org.cloudcoder.app.server.persist.txn.GetCoursesForUser;
import org.cloudcoder.app.server.persist.txn.GetModulesForCourse;
import org.cloudcoder.app.server.persist.txn.GetMostRecentChangeForUserAndProblem;
import org.cloudcoder.app.server.persist.txn.GetMostRecentFullTextChange;
import org.cloudcoder.app.server.persist.txn.GetOrAddLatestSubmissionReceipt;
import org.cloudcoder.app.server.persist.txn.GetProblemAndSubscriptionReceiptsForUserInCourse;
import org.cloudcoder.app.server.persist.txn.GetProblemForProblemId;
import org.cloudcoder.app.server.persist.txn.GetProblemForUser;
import org.cloudcoder.app.server.persist.txn.GetRepoProblemTags;
import org.cloudcoder.app.server.persist.txn.GetProblemsInCourse;
import org.cloudcoder.app.server.persist.txn.GetRepoProblemAndTestCaseListGivenHash;
import org.cloudcoder.app.server.persist.txn.GetSectionsForCourse;
import org.cloudcoder.app.server.persist.txn.GetSubmissionReceipt;
import org.cloudcoder.app.server.persist.txn.GetSubmissionText;
import org.cloudcoder.app.server.persist.txn.GetTestCasesForProblem;
import org.cloudcoder.app.server.persist.txn.GetTestCasesForProblemCheckAuth;
import org.cloudcoder.app.server.persist.txn.GetTestResultsForSubmission;
import org.cloudcoder.app.server.persist.txn.GetUserGivenId;
import org.cloudcoder.app.server.persist.txn.GetUserWithoutAuthentication;
import org.cloudcoder.app.server.persist.txn.GetUsersInCourse;
import org.cloudcoder.app.server.persist.txn.InsertProblem;
import org.cloudcoder.app.server.persist.txn.InsertUsersFromInputStream;
import org.cloudcoder.app.server.persist.txn.ReloadModelObject;
import org.cloudcoder.app.server.persist.txn.ReplaceSubmissionReceipt;
import org.cloudcoder.app.server.persist.txn.ReplaceTestResults;
import org.cloudcoder.app.server.persist.txn.SearchRepositoryExercises;
import org.cloudcoder.app.server.persist.txn.SetModuleForProblem;
import org.cloudcoder.app.server.persist.txn.InstructorStartQuiz;
import org.cloudcoder.app.server.persist.txn.StudentStartOrContinueQuiz;
import org.cloudcoder.app.server.persist.txn.StoreChanges;
import org.cloudcoder.app.server.persist.txn.StoreProblemAndTestCaseList;
import org.cloudcoder.app.server.persist.txn.StoreRepoProblemAndTestCaseList;
import org.cloudcoder.app.server.persist.txn.StoreSubmissionReceipt;
import org.cloudcoder.app.server.persist.txn.SuggestTagNames;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DatabaseRunnable;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.IModelObject;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.ProblemSummary;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.RepoProblemSearchCriteria;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.StartedQuiz;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.UserRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of IDatabase using JDBC.
 * 
 * @author David Hovemeyer
 */
public class JDBCDatabase implements IDatabase {
	static final Logger logger=LoggerFactory.getLogger(JDBCDatabase.class);

	private String jdbcUrl;
	
	public JDBCDatabase() {
		JDBCDatabaseConfig.ConfigProperties config = JDBCDatabaseConfig.getInstance().getConfigProperties();
		jdbcUrl = "jdbc:mysql://" +
				config.getHost() + config.getPortStr() +
				"/" +
				config.getDatabaseName() +
				"?user=" +
				config.getUser() +
				"&password=" + config.getPasswd();
		logger.info("Database URL: "+jdbcUrl);
	}
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw new IllegalStateException("Could not load mysql jdbc driver", e);
		}
	}

	private static class InUseConnection {
		Connection conn;
		int refCount;
	}
	
	/*
	 * Need to consider how to do connection management better.
	 * For now, just use something simple that works.
	 */

	private ThreadLocal<InUseConnection> threadLocalConnection = new ThreadLocal<InUseConnection>();
	
	private Connection getConnection() throws SQLException {
		InUseConnection c = threadLocalConnection.get();
		if (c == null) {
			c = new InUseConnection();
			c.conn = DriverManager.getConnection(jdbcUrl);
			c.refCount = 0;
			threadLocalConnection.set(c);
		}
		c.refCount++;
		return c.conn;
	}
	
	private void releaseConnection() throws SQLException {
		InUseConnection c = threadLocalConnection.get();
		c.refCount--;
		if (c.refCount == 0) {
			c.conn.close();
			threadLocalConnection.set(null);
		}
	}
	
	@Override
	public ConfigurationSetting getConfigurationSetting(final ConfigurationSettingName name) {
		return databaseRun(new GetConfigurationSetting(name));
	}
	
	public User getUserGivenId(final int userId) {
		return databaseRun(new GetUserGivenId(userId));
	}
	
	@Override
	public User authenticateUser(final String userName, final String password) {
		return databaseRun(new AuthenticateUser(userName, password));
	}
	
	@Override
    public List<User> getUsersInCourse(final int courseId, final int sectionNumber) {
		return databaseRun(new GetUsersInCourse(sectionNumber, courseId));
	}

	@Override
	public User getUserWithoutAuthentication(final String userName) {
		return databaseRun(new GetUserWithoutAuthentication(userName));
	}
	
	@Override
	public Pair<Problem, Quiz> getProblem(final User user, final int problemId) {
		return databaseRun(new GetProblemForUser(problemId, user));
	}

	@Override
	public Problem getProblem(final int problemId) {
		return databaseRun(new GetProblemForProblemId(problemId));
	}
	
	@Override
	public Change getMostRecentChange(final User user, final int problemId) {
		return databaseRun(new GetMostRecentChangeForUserAndProblem(problemId, user));
	}
	
	@Override
	public Change getMostRecentFullTextChange(final User user, final int problemId) {
		return databaseRun(new GetMostRecentFullTextChange(problemId, user));
	}
	
	@Override
	public Change getChange(final int changeEventId) {
		return databaseRun(new GetChangeGivenChangeEventId(changeEventId));
	}
	
	@Override
	public List<Change> getAllChangesNewerThan(final User user, final int problemId, final int baseRev) {
		return databaseRun(new GetAllChangesNewerThan(problemId, user, baseRev));
	}
	
	@Override
	public List<? extends Object[]> getCoursesForUser(final User user) {
		return databaseRun(new GetCoursesForUser(user));
	}

	@Override
	public ProblemList getProblemsInCourse(final User user, final Course course) {
		return databaseRun(new GetProblemsInCourse(course, user));
	}

	@Override
	public List<ProblemAndSubmissionReceipt> getProblemAndSubscriptionReceiptsInCourse(
			final User requestingUser, final Course course, final User forUser, final Module module) {
		return databaseRun(new GetProblemAndSubscriptionReceiptsForUserInCourse(course, forUser,
				module, requestingUser));
	}
	
	@Override
	public void storeChanges(final Change[] changeList) {
		databaseRun(new StoreChanges(changeList));
	}
	
	@Override
	public List<TestCase> getTestCasesForProblem(final int problemId) {
		return databaseRun(new GetTestCasesForProblem(problemId));
	}

	@Override
	public TestCase[] getTestCasesForProblem(final User authenticatedUser, final boolean requireInstructor, final int problemId) {
		return databaseRun(new GetTestCasesForProblemCheckAuth(authenticatedUser,
				requireInstructor, problemId));
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#insertSubmissionReceipt(org.cloudcoder.app.shared.model.SubmissionReceipt)
	 */
	@Override
	public void insertSubmissionReceipt(final SubmissionReceipt receipt, final TestResult[] testResultList_) {
		databaseRun(new StoreSubmissionReceipt(testResultList_, receipt));
	}
	
	public void insertUsersFromInputStream(final InputStream in, final Course course) {
		try {
			databaseRunAuth(new InsertUsersFromInputStream(in, course));
		} catch (CloudCoderAuthenticationException e) {
			// TODO proper error handling
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#addSubmissionReceiptIfNecessary(org.cloudcoder.app.shared.model.User, org.cloudcoder.app.shared.model.Problem)
	 */
	@Override
	public void getOrAddLatestSubmissionReceipt(final User user, final Problem problem) {
		databaseRun(new GetOrAddLatestSubmissionReceipt(user, problem));
	}
	
	@Override
	public void editUser(final User user)
	{
	    databaseRun(new EditUser(user));
	}
	
	@Override
    public void editUser(final int userId, final String username, final String firstname, 
        final String lastname, final String email, final String passwd)
    {
	    databaseRun(new EditUserGivenUserData(username, lastname, email, passwd, userId,
				firstname));    
    }

    @Override
    public void editRegistrationType(int userId, int courseId,
        CourseRegistrationType type)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Implement this method");
    }

    @Override
	public void addUserToCourse(final User authenticatedUser, final int courseId, final EditedUser editedUser) throws CloudCoderAuthenticationException {
    	databaseRunAuth(new AddUserToCourse(courseId, authenticatedUser, editedUser));
    }

    @Override
	public void addProblem(final Problem problem) {
		databaseRun(new InsertProblem(problem));
	}
	
	@Override
	public void addTestCases(final Problem problem, final List<TestCase> testCaseList) {
		databaseRun(new AddTestCasesToProblem(problem, testCaseList));
	}
	
	@Override
	public ProblemSummary createProblemSummary(final Problem problem) {
		return databaseRun(new CreateProblemSummary(problem));
	}
	
	@Override
	public SubmissionReceipt getSubmissionReceipt(final int submissionReceiptId) {
		return databaseRun(new GetSubmissionReceipt(submissionReceiptId));
	}
	
	@Override
	public void replaceTestResults(final TestResult[] testResults, final int submissionReceiptId) {
		databaseRun(new ReplaceTestResults(submissionReceiptId, testResults));
	}
	
	@Override
	public void updateSubmissionReceipt(final SubmissionReceipt receipt) {
		databaseRun(new ReplaceSubmissionReceipt(receipt));
	}
	
	@Override
	public ProblemAndTestCaseList storeProblemAndTestCaseList(
			final ProblemAndTestCaseList problemAndTestCaseList, final Course course, final User user)
			throws CloudCoderAuthenticationException {
		return databaseRunAuth(new StoreProblemAndTestCaseList(problemAndTestCaseList, course, user));
	}
	
	@Override
	public RepoProblemAndTestCaseList getRepoProblemAndTestCaseList(final String hash) {
		return databaseRun(new GetRepoProblemAndTestCaseListGivenHash(hash));
	}

	@Override
	public void storeRepoProblemAndTestCaseList(final RepoProblemAndTestCaseList exercise, final User user) {
		databaseRun(new StoreRepoProblemAndTestCaseList(user, exercise));
	}
	
	@Override
	public List<RepoProblemSearchResult> searchRepositoryExercises(final RepoProblemSearchCriteria searchCriteria) {
		return databaseRun(new SearchRepositoryExercises(searchCriteria));
	}
	
	@Override
	public CourseRegistrationList findCourseRegistrations(final User user, final Course course) {
		return databaseRun(new FindCourseRegistrationsGivenUserAndCourse(course, user));
	}
	
	@Override
	public CourseRegistrationList findCourseRegistrations(final User user, final int courseId) {
		return databaseRun(new FindCourseRegistrationsGivenUserAndCourseId(user, courseId));
	}
	
	@Override
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(
			final Course unused, final int section, final Problem problem) {
		return databaseRun(new GetBestSubmissionReceiptsForProblem(section, problem));
	}

	@Override
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(final Problem problem, final int section, final User authenticatedUser) {
		return databaseRun(new GetBestSubmissionReceiptsForProblemForAuthenticatedUser(section,
				problem, authenticatedUser));
	}
	
	@Override
	public boolean deleteProblem(final User user, final Course course, final Problem problem)
			throws CloudCoderAuthenticationException {
		return databaseRunAuth(new DeleteProblem(user, problem, course));
	}

	@Override
	public OperationResult addUserRegistrationRequest(final UserRegistrationRequest request) {
		return databaseRun(new AddUserRegistrationRequest(request));
	}
	
	@Override
	public UserRegistrationRequest findUserRegistrationRequest(final String secret) {
		return databaseRun(new FindUserRegistrationRequestGivenSecret(secret));
	}
	
	@Override
	public OperationResult completeRegistration(final UserRegistrationRequest request) {
		return databaseRun(new CompleteRegistration(request));
	}
	
	@Override
	public List<RepoProblemTag> getProblemTags(final int repoProblemId) {
		return databaseRun(new GetRepoProblemTags(repoProblemId));
	}
	
	@Override
	public boolean addRepoProblemTag(final RepoProblemTag repoProblemTag) {
		return databaseRun(new AddRepoProblemTag(repoProblemTag));
	}
	
	@Override
	public List<String> suggestTagNames(final String term) {
		return databaseRun(new SuggestTagNames(term));
	}
	
	@Override
	public Quiz startQuiz(final User user, final Problem problem, final int section) throws CloudCoderAuthenticationException {
		return databaseRunAuth(new InstructorStartQuiz(section, user, problem));
	}
	
	@Override
	public Quiz findCurrentQuiz(final User user, final Problem problem) {
		return databaseRun(new FindCurrentQuiz(problem, user));
	}
	
	@Override
	public Boolean endQuiz(final User user, final Quiz quiz) {
		return databaseRun(new EndQuiz(quiz, user));
	}
	
	@Override
	public <E extends IModelObject<E>> boolean reloadModelObject(final E obj) {
		return databaseRun(new ReloadModelObject<E>(obj));
	}
	
	@Override
	public Module[] getModulesForCourse(final User user, final Course course) {
		return databaseRun(new GetModulesForCourse(user, course));
	}
	
	@Override
	public Module setModule(final User user, final Problem problem, final String moduleName) throws CloudCoderAuthenticationException {
		return databaseRunAuth(new SetModuleForProblem(moduleName, user, problem));
	}
	
	@Override
	public StartedQuiz startOrContinueQuiz(final User user, final Quiz quiz) {
		return databaseRun(new StudentStartOrContinueQuiz(quiz, user));
	}
	
	@Override
	public StartedQuiz findUnfinishedQuiz(final User user) {
		return databaseRun(new FindUnfinishedQuizForStudent(user));
	}
	
	@Override
	public Integer[] getSectionsForCourse(final Course course, final User authenticatedUser) {
		return databaseRun(new GetSectionsForCourse(course, authenticatedUser));
	}
	
	@Override
	public SubmissionReceipt[] getAllSubmissionReceiptsForUser(final Problem problem, final User user) {
		return databaseRun(new GetAllSubmissionReceiptsForUserAndProblem(problem, user));
	}
	
	@Override
	public ProblemText getSubmissionText(final User authenticatedUser, final User submitter, final Problem problem, final SubmissionReceipt receipt) {
		return databaseRun(new GetSubmissionText(receipt, authenticatedUser, problem, submitter));
	}
	
	@Override
	public NamedTestResult[] getTestResultsForSubmission(final User authenticatedUser, final Problem problem, final SubmissionReceipt receipt) {
		return databaseRun(new GetTestResultsForSubmission(receipt, authenticatedUser, problem));
	}

	/**
	 * Run a database transaction and return the result.
	 * This method is for transactions that extend {@link AbstractDatabaseRunnableNoAuthException}
	 * and thus are guaranteed not to throw {@link CloudCoderAuthenticationException}.
	 * 
	 * @param databaseRunnable the transaction to run
	 * @return the result
	 */
	public<E> E databaseRun(AbstractDatabaseRunnableNoAuthException<E> databaseRunnable) {
		try {
			return doDatabaseRun(databaseRunnable);
		} catch (CloudCoderAuthenticationException e) {
			// The fact that the method takes an
			// AbstractDatabaseRunnableNoAuthException guarantees that the transaction
			// won't throw NetcoderAuthenticationException.
			throw new IllegalStateException("Can't happen", e);
		}
	}

	/**
	 * Run a database transaction and return the result.
	 * This method is for transactions that check the authenticity of provided
	 * user credentials and may throw {@link CloudCoderAuthenticationException}.
	 * 
	 * @param databaseRunnable the transaction to run
	 * @return the result
	 */
	public<E> E databaseRunAuth(AbstractDatabaseRunnable<E> databaseRunnable) throws CloudCoderAuthenticationException {
		return doDatabaseRun(databaseRunnable);
	}

	private<E> E doDatabaseRun(DatabaseRunnable<E> databaseRunnable) throws CloudCoderAuthenticationException {
		// Give the DatabaseRunnable access to the logger
		databaseRunnable.setLogger(logger);
		
		int attempts = 0;
		
		boolean successfulCommit = false;
		E result = null;

		// Attempt the transaction until a maximum number of attempts is reached.
		while (!successfulCommit && attempts < 20) {
			attempts++;
			
			Connection conn;
			boolean origAutocommit;
			
			// Attempt to get a connection
			try {
				conn = getConnection();
				origAutocommit = conn.getAutoCommit();
			} catch (SQLException e) {
				throw new PersistenceException("SQLException", e);
			}
			
			// Attempt the execute the transaction.
			// If the transaction is not successful (throws SQLException),
			// determine if it is because of a deadlock or other recoverable
			// error, and if so, retry.
			try {
				conn.setAutoCommit(false);
				result = databaseRunnable.run(conn);
				conn.commit();
				successfulCommit = true; // Hooray!
			} catch (SQLException e) {
				String sqlState = e.getSQLState();
				if (sqlState != null && (sqlState.equals("40001") || sqlState.equals("41000") || sqlState.equals("23000"))) {
					// Deadlock detected: retry transaction
					// NOTE: I have also included duplicate key errors (23000).
					// There is evidence that a MySQL bug can cause spurious
					// duplicate key errors on auto increment fields, which I *think*
					// I have actually seen in CloudCoder error logs.
					//   See: http://www.softwareprojects.com/resources/programming/t-mysql-innodb-deadlocks-and-duplicate-key-errors-12-1970.html
					// Workaround is to retry the transaction.
					logger.info("MySQL deadlock detected (sqlState=" + sqlState + ")", e);
				} else {
					// Some other kind of transaction failure.
					logger.error("Transaction failed with SQLException", e);
					throw new PersistenceException("SQLException", e);
				}
			} finally {
				// If the transaction didn't succeed, roll back
				if (!successfulCommit) {
					try {
						conn.rollback();
					} catch (SQLException ex) {
						throw new PersistenceException("SQLException (on rollback)", ex);
					}
				}
				
				// Restore the original autocommit value and release the connection.
				try {
					conn.setAutoCommit(origAutocommit);
					releaseConnection();
				} catch (SQLException e) {
					throw new PersistenceException("SQLException (releasing connection)", e);
				}
			}
		}
		
		// If the transaction was never executed successfully, throw a PersistenceException
		if (!successfulCommit) {
			throw new PersistenceException("Could not complete transaction (gave up after " + attempts + " attempts)");
		}
		
		// Success!
		return result;
	}
	
}
