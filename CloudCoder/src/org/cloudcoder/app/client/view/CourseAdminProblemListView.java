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

package org.cloudcoder.app.client.view;

import java.util.Arrays;

import org.cloudcoder.app.client.model.CourseSelection;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.CourseAdminPage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndModule;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View to show problems in the {@link CourseAdminPage}.
 * 
 * @author David Hovemeyer
 */
public class CourseAdminProblemListView extends ResizeComposite implements Subscriber, SessionObserver {
	private CloudCoderPage page;
	private DataGrid<ProblemAndModule> grid;
	private Session session;
	private SingleSelectionModel<ProblemAndModule> selectionModel;
	private ICallback<ProblemAndModule> editModuleNameCallback;
	
	/**
	 * Constructor.
	 */
	public CourseAdminProblemListView(CloudCoderPage page) {
		this.page = page;
		grid = new DataGrid<ProblemAndModule>();
		grid.addColumn(new ProblemNameColumn(), "Name");
		grid.addColumn(new ProblemBriefDescriptionColumn(), "Description");
		grid.addColumn(new ProblemTypeColumn(), "Type");
		grid.addColumn(new ProblemWhenAssignedColumn(), "Assigned");
		grid.addColumn(new ProblemWhenDueColumn(), "Due");
		grid.addColumn(new ProblemVisibleColumn(), "Visible");
		
		// The column displaying the module name allows editing, and invokes
		// a callback when the module name changes.
		ProblemModuleNameColumn moduleNameColumn = new ProblemModuleNameColumn();
		grid.addColumn(moduleNameColumn, "Module (click to edit)");
		moduleNameColumn.setFieldUpdater(new FieldUpdater<ProblemAndModule, String>() {
			@Override
			public void update(int index, ProblemAndModule object, String value) {
				object.getModule().setName(value);
				if (editModuleNameCallback != null) {
					editModuleNameCallback.call(object);
				}
			}
		});
		initWidget(grid);
	}
	
	/**
	 * Set a callback to be invoked when the module name of a problem is changed.
	 * 
	 * @param callback callback invoked when the module name of a problem is changed
	 */
	public void setEditModuleNameCallback(ICallback<ProblemAndModule> callback) {
		this.editModuleNameCallback = callback;
	}
	
	private static class ProblemNameColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().getTestname();
		}
	}
	
	private static class ProblemBriefDescriptionColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().getBriefDescription();
		}
	}
	
	private static class ProblemTypeColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().getProblemType().toString();
		}
	}
	
	private static class ProblemWhenAssignedColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return ViewUtil.formatDate(object.getProblem().getWhenAssignedAsDate());
		}
	}
	
	private static class ProblemWhenDueColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return ViewUtil.formatDate(object.getProblem().getWhenDueAsDate());
		}
	}
	
	private static class ProblemVisibleColumn extends TextColumn<ProblemAndModule> {
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getProblem().isVisible() ? "true" : "false";
		}
	}
	
//	private static class ProblemModuleNameColumn extends TextColumn<ProblemAndModule> {
//		@Override
//		public String getValue(ProblemAndModule object) {
//			return object.getModule().getName();
//		}
//	}
	
	private static class ProblemModuleNameColumn extends Column<ProblemAndModule, String> {
		public ProblemModuleNameColumn() {
			super(new EditTextCell());
		}
		
		/* (non-Javadoc)
		 * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(ProblemAndModule object) {
			return object.getModule().getName();
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		// Set selection model.
		// When a Problem is selected, it will be added to the Session.
		this.selectionModel = new SingleSelectionModel<ProblemAndModule>();
		selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ProblemAndModule selected = selectionModel.getSelectedObject();
				if (selected != null) {
					session.add(selected.getProblem());
				}
			}
		});
		grid.setSelectionModel(selectionModel);

		// Force loading of problems in course.
		// This avoids the problem that if a module in a course was selected
		// in the courses/problems page, some of the problems may not be
		// in the session (because they weren't in the selected module).
		CourseSelection courseSelection = session.get(CourseSelection.class);
		Course course = courseSelection.getCourse();
		loadProblems(session, course);
	}
	
	/**
	 * Get the currently-selected {@link Problem}.
	 * 
	 * @return the currently-selected {@link Problem}
	 */
	public Problem getSelected() {
		return selectionModel.getSelectedObject().getProblem();
	}

	private void loadProblems(final Session session, final Course course) {
		RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, (Module)null, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
			/* (non-Javadoc)
			 * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
			 */
			@Override
			public void onSuccess(ProblemAndSubmissionReceipt[] result) {
				displayProblems(result);
			}
			/* (non-Javadoc)
			 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
			 */
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof CloudCoderAuthenticationException) {
					page.recoverFromServerSessionTimeout(new Runnable() {
						public void run() {
							// Try again!
							loadProblems(session, course);
						}
					});
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
	 */
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && (hint instanceof CourseSelection)) {
			// Course selected, load its problems.
			// Note that this isn't really needed by CourseAdminPage (because there
			// is only one Course which is pre-selected), but if this view is
			// reused in another page at some point, this might be useful.
			CourseSelection courseSelection = (CourseSelection) hint;
			Course course = courseSelection.getCourse();
			loadProblems(session, course);
		} else if (key == Session.Event.ADDED_OBJECT && (hint instanceof ProblemAndSubmissionReceipt[])) {
			// This can happen when these is an explicit reload of problems
			displayProblems((ProblemAndSubmissionReceipt[]) hint);
		}
	}

	protected void displayProblems(ProblemAndSubmissionReceipt[] problemAndSubmissionReceiptList) {
		ProblemAndModule[] problems = new ProblemAndModule[problemAndSubmissionReceiptList.length];
		int count = 0;
		for (ProblemAndSubmissionReceipt p : problemAndSubmissionReceiptList) {
			problems[count++] = new ProblemAndModule(p.getProblem(), p.getModule());
		}
		displayProblems(problems);
	}

	protected void displayProblems(ProblemAndModule[] result) {
		grid.setRowCount(result.length);
		grid.setRowData(Arrays.asList(result));
	}
}
