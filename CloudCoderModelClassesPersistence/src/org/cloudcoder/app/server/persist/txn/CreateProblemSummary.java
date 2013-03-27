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
import java.util.HashMap;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemSummary;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;

/**
 * Transaction to create a {@link ProblemSummary} summarizing all student
 * work on a @{link {@link Problem}.
 */
public class CreateProblemSummary extends
		AbstractDatabaseRunnableNoAuthException<ProblemSummary> {
	private final Problem problem;

	/**
	 * Constructor.
	 * 
	 * @param problem the {@link Problem}
	 */
	public CreateProblemSummary(Problem problem) {
		this.problem = problem;
	}

	@Override
	public ProblemSummary run(Connection conn) throws SQLException {
		// Determine how many students (non-instructor users) are in this course
		int numStudentsInCourse = Queries.doCountStudentsInCourse(problem, conn, this);
		
		// Get all SubmissionReceipts
		PreparedStatement stmt = prepareStatement(
				conn,
				"select sr.*, e.* " +
				"  from " + SubmissionReceipt.SCHEMA.getDbTableName() + " as sr, " + Event.SCHEMA.getDbTableName() + " as e " +
				" where sr.event_id = e.id " +
				"   and e.problem_id = ?");
		stmt.setInt(1, problem.getProblemId());
		
		// Keep track of "best" submissions from each student.
		HashMap<Integer, SubmissionReceipt> bestSubmissions = new HashMap<Integer, SubmissionReceipt>();
		
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			SubmissionReceipt receipt = Queries.loadSubmissionReceiptAndEvent(resultSet);
			
			SubmissionReceipt prevBest = bestSubmissions.get(receipt.getEvent().getUserId());
			SubmissionStatus curStatus = receipt.getStatus();
			//SubmissionStatus prevStatus = prevBest.getStatus();
			if (prevBest == null
					|| curStatus == SubmissionStatus.TESTS_PASSED && prevBest.getStatus() != SubmissionStatus.TESTS_PASSED
					|| receipt.getNumTestsPassed() > prevBest.getNumTestsPassed()) {
				// New receipt is better than the previous receipt
				bestSubmissions.put(receipt.getEvent().getUserId(), receipt);
			}
		}
		
		// Aggregate the data
		int started = 0;
		int anyPassed = 0;
		int allPassed = 0;
		for (SubmissionReceipt r : bestSubmissions.values()) {
			if (r.getStatus() == SubmissionStatus.TESTS_PASSED) {
				started++;
				allPassed++;
				anyPassed++;
			} else if (r.getNumTestsPassed() > 0) {
				started++;
				anyPassed++;
			} else {
				started++;
			}
		}
		
		// Create the ProblemSummary
		ProblemSummary problemSummary = new ProblemSummary();
		problemSummary.setProblem(problem);
		problemSummary.setNumStudents(numStudentsInCourse);
		problemSummary.setNumStarted(started);
		problemSummary.setNumPassedAtLeastOneTest(anyPassed);
		problemSummary.setNumCompleted(allPassed);
		
		return problemSummary;
	}

	@Override
	public String getDescription() {
		return "get problem summary for problem";
	}
}