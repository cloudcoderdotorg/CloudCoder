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
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.TestResult;

/**
 * Transaction to store a {@link SubmissionReceipt} and {@link TestResult}s
 * for a user's submission.
 */
public class StoreSubmissionReceipt extends
		AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final TestResult[] testResultList_;
	private final SubmissionReceipt receipt;

	/**
	 * Constructor.
	 * 
	 * @param testResultList_  the {@link TestResult}s
	 * @param receipt          the {@link SubmissionReceipt}
	 */
	public StoreSubmissionReceipt(TestResult[] testResultList_,
			SubmissionReceipt receipt) {
		this.testResultList_ = testResultList_;
		this.receipt = receipt;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		Queries.doInsertSubmissionReceipt(receipt, testResultList_, conn, this);
		return true;
	}

	@Override
	public String getDescription() {
		return "storing submission receipt";
	}
}