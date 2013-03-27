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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.RepoTestCase;
import org.cloudcoder.app.shared.model.User;

/**
 * Transaction to store a {@link RepoProblemAndTestCaseList} (a repository exercise)
 * in the database.
 */
public class StoreRepoProblemAndTestCaseList extends
		AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final User user;
	private final RepoProblemAndTestCaseList exercise;

	/**
	 * Constructor.
	 * 
	 * @param user     the user who is adding the exercise to the database
	 * @param exercise the exercise
	 */
	public StoreRepoProblemAndTestCaseList(User user,
			RepoProblemAndTestCaseList exercise) {
		this.user = user;
		this.exercise = exercise;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		// Compute hash
		exercise.computeHash();
		
		// Set user id
		exercise.getProblem().setUserId(user.getId());

		// Store the RepoProblem
		DBUtil.storeModelObject(conn, exercise.getProblem());
		
		// Insert RepoTestCases (setting repo problem id of each)
		String insertRepoTestCaseSql = DBUtil.createInsertStatement(RepoTestCase.SCHEMA);
		PreparedStatement stmt = prepareStatement(conn, insertRepoTestCaseSql, PreparedStatement.RETURN_GENERATED_KEYS);
		for (RepoTestCase repoTestCase : exercise.getTestCaseData()) {
			repoTestCase.setRepoProblemId(exercise.getProblem().getId());
			DBUtil.bindModelObjectValuesForInsert(repoTestCase, RepoTestCase.SCHEMA, stmt);
			stmt.addBatch();
		}
		stmt.executeBatch();

		// Get generated unique ids of RepoTestCase objects
		ResultSet genKeys = getGeneratedKeys(stmt);
		DBUtil.getModelObjectUniqueIds(exercise.getTestCaseData(), RepoTestCase.SCHEMA, genKeys);
		
		// Add a tag indicating the programming language
		Language language = exercise.getProblem().getProblemType().getLanguage();
		RepoProblemTag tag = new RepoProblemTag();
		tag.setName(language.getTagName());
		tag.setRepoProblemId(exercise.getProblem().getId());
		tag.setUserId(user.getId());
		Queries.doAddRepoProblemTag(conn, tag, this);
		
		return true;
	}

	@Override
	public String getDescription() {
		return " storing exercise in repository database";
	}
}