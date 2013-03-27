package org.cloudcoder.app.server.persist.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

import org.slf4j.Logger;


/**
 * Abstract base class for database transactions.
 * Provides methods for creating resources (PreparedStatements, ResultSets)
 * that will be automatically cleaned up by the cleanup() method.
 * 
 * @author David Hovemeyer
 *
 * @param <E>
 */
public abstract class AbstractDatabaseRunnable<E> implements DatabaseRunnable<E> {

	private Stack<Object> cleanupStack = new Stack<Object>();
	private Logger logger;

	/**
	 * Constructor.
	 */
	public AbstractDatabaseRunnable() {
	}

	/**
	 * Create a PreparedStatement, adding it to the cleanup stack.
	 * 
	 * @param conn the database connection
	 * @param sql  the SQL to execute
	 * @return the PreparedStatement, which will be cleaned up automatically
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql);
		cleanupStack.push(stmt);
		return stmt;
	}

	/**
	 * Create a PreparedStatement, adding it to the cleanup stack.
	 * 
	 * @param conn the database connection
	 * @param sql  the SQL to execute
	 * @param options options for creating the PreparedStatement
	 * @return the PreparedStatement, which will be cleaned up automatically
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(Connection conn, String sql, int options) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql, options);
		cleanupStack.push(stmt);
		return stmt;
	}

	/**
	 * Execute a PreparedStatement to produce a ResultSet.
	 * The ResultSet will be added to the cleanup stack.
	 * 
	 * @param stmt the PreparedStatement to execute
	 * @return the ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery(PreparedStatement stmt) throws SQLException {
		ResultSet resultSet = stmt.executeQuery();
		cleanupStack.push(resultSet);
		return resultSet;
	}

	/**
	 * Get a ResultSet with generated keys from executing the given PreparedStatement.
	 * 
	 * @param stmt the PreparedStatement
	 * @return the ResultSet with the generated keys
	 * @throws SQLException
	 */
	public ResultSet getGeneratedKeys(PreparedStatement stmt) throws SQLException {
		ResultSet resultSet = stmt.getGeneratedKeys();
		cleanupStack.push(resultSet);
		return resultSet;
	}

	@Override
	public void cleanup() {
		while (!cleanupStack.isEmpty()) {
			Object o = cleanupStack.pop();
			if (o instanceof PreparedStatement) {
				DBUtil.closeQuietly((PreparedStatement) o);
			} else if (o instanceof ResultSet) {
				DBUtil.closeQuietly((ResultSet) o);
			}
		}
	}
	
	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}