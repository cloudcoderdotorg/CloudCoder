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

package org.cloudcoder.app.client.view;

import java.util.Arrays;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View for ProblemAndSubmissionReceipts.
 * It serves as a summary of which problems are available in a particular
 * Course. It also displays a status with each problem (whether or not the
 * problem has been started, completed, etc.)
 * 
 * @author David Hovemeyer
 */
public class ProblemListView2 extends ResizeComposite implements SessionObserver, Subscriber {
	private CloudCoderPage page;
	private Session session;
	private DataGrid<ProblemAndSubmissionReceipt> cellTable;

	/**
	 * Constructor.
	 * 
	 * @param page the {@link CloudCoderPage} that will contain this view
	 */
	public ProblemListView2(CloudCoderPage page) {
		this.page = page;
		
		cellTable = new DataGrid<ProblemAndSubmissionReceipt>();
		
		// Configure the DataGrid that will show the problems
		cellTable.addColumn(new TestNameColumn(), "Name");
		cellTable.addColumn(new BriefDescriptionColumn(), "Description");
		cellTable.addColumn(new WhenAssignedColumn(), "Assigned");
		cellTable.addColumn(new WhenDueColumn(), "Due");
		cellTable.addColumn(new SubmissionStatusColumn(), "Status");
		
		initWidget(cellTable);
	}

	private static class TestNameColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return object.getProblem().getTestname();
		}
	}

	private static class BriefDescriptionColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return object.getProblem().getBriefDescription();
		}
	}
	
	private static class WhenAssignedColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return ViewUtil.formatDate(object.getProblem().getWhenAssignedAsDate());
		}
	}
	
	private static class WhenDueColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return ViewUtil.formatDate(object.getProblem().getWhenDueAsDate());
		}
	}
	
	private static class SubmissionStatusColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			SubmissionStatus status = (object.getReceipt() != null) ? object.getReceipt().getStatus() : SubmissionStatus.NOT_STARTED;
			return status.toString();
		}
	}

	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		
		// Subscribe to session ADDED_OBJECT events (so we will see when a course is selected)
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);

		// When a problem is selected, add it to the session
		final SingleSelectionModel<ProblemAndSubmissionReceipt> selectionModel = new SingleSelectionModel<ProblemAndSubmissionReceipt>();
		cellTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ProblemAndSubmissionReceipt selected = selectionModel.getSelectedObject();
				if (selected != null) {
					// Add the problem to the Session
					session.add(selected.getProblem());
				}
			}
		});
		
		// If there is already a Course selected, load its problems.
		// Otherwise, if there are problems already in the session, display them.
		CourseSelection courseSelection = session.get(CourseSelection.class);
		ProblemAndSubmissionReceipt[] problemList = session.get(ProblemAndSubmissionReceipt[].class);
		if (courseSelection != null) {
//			Course course = courseSelection.getCourse();
			loadProblemsForCourse(courseSelection);
		} else if (problemList != null) {
			displayLoadedProblems(problemList);
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && (hint instanceof CourseSelection)) {
			// A Course has been selected: load its problems
			CourseSelection courseSelection = (CourseSelection) hint;
			loadProblemsForCourse(courseSelection);
		} else if (key == Session.Event.ADDED_OBJECT && (hint instanceof ProblemAndSubmissionReceipt[])) {
			// The list of ProblemAndSubmissionReceipts has been reloaded.
			// This can happen because the current page has done something to change
			// the list (e.g., adding or removing a problem) for the current course.
			displayLoadedProblems((ProblemAndSubmissionReceipt[])hint);
		}
	}

	public void loadProblemsForCourse(final CourseSelection courseSelection) {
		RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(courseSelection.getCourse(), session.get(User.class), courseSelection.getModule(), new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof CloudCoderAuthenticationException) {
					page.recoverFromServerSessionTimeout(new Runnable() {
						@Override
						public void run() {
							// Try again!
							loadProblemsForCourse(courseSelection);
						}
					});
				} else {
					GWT.log("Error loading problems for course", caught);
					session.add(StatusMessage.error("Error loading problems for course", caught));
				}
			}

			@Override
			public void onSuccess(ProblemAndSubmissionReceipt[] result) {
				//displayLoadedProblems(result);
				session.add(result);
			}
		});
	}

	private void displayLoadedProblems(ProblemAndSubmissionReceipt[] problemList) {
		cellTable.setRowCount(problemList.length);
		cellTable.setRowData(0, Arrays.asList(problemList));
	}
}
