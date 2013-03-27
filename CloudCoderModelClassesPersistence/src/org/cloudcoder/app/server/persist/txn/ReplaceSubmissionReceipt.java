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
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.SubmissionReceipt;

/**
 * Transaction to replace a {@link SubmissionReceipt}.
 * (Useful if a submission needs to be retested.)
 */
public class ReplaceSubmissionReceipt extends
		AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final SubmissionReceipt receipt;

	/**
	 * Constructor.
	 *  
	 * @param receipt the updated {@link SubmissionReceipt}
	 */
	public ReplaceSubmissionReceipt(SubmissionReceipt receipt) {
		this.receipt = receipt;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
	 */
	@Override
	public Boolean run(Connection conn) throws SQLException {
		// Only update the fields that we expect might have changed
		// following a retest.
		PreparedStatement stmt = prepareStatement(
				conn,
				"update " + SubmissionReceipt.SCHEMA.getDbTableName() + 
				"  set status = ?, num_tests_attempted = ?, num_tests_passed = ?");
		stmt.setInt(1, receipt.getStatus().ordinal());
		stmt.setInt(2, receipt.getNumTestsAttempted());
		stmt.setInt(3, receipt.getNumTestsPassed());
		
		stmt.executeUpdate();
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "update submission receipt";
	}
}