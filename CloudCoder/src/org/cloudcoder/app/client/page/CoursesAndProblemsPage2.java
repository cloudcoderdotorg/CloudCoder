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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.CourseSelection;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemDescriptionView;
import org.cloudcoder.app.client.view.ProblemListView2;
import org.cloudcoder.app.client.view.SectionLabel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TermAndCourseTreeView;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * A {@link CloudCoderPage} to browse courses and problems.
 * 
 * @author David Hovemeyer
 */
public class CoursesAndProblemsPage2 extends CloudCoderPage {
	/**
	 * UI class for CoursesAndProblemsPage2.
	 */
	private class UI extends Composite implements SessionObserver, Subscriber {
		/**
		 * Width of the west panel (which has the term/course tree view).
		 */
		private static final double WEST_PANEL_WIDTH_PX = 240.0;
		
		/**
		 * Width of "Load Problem!" button.
		 */
		private static final double LOAD_PROBLEM_BUTTON_WIDTH_PX = 160.0;

		/**
		 * Width of the course and user admin buttons.
		 */
		public static final double COURSE_AND_USER_ADMIN_BUTTON_WIDTH_PX = 70.0;

		/**
		 * Height of the course and user admin buttons.
		 */
		private static final double COURSE_AND_USER_ADMIN_BUTTON_HEIGHT_PX = 27.0;
		
		/**
		 * Width of "Refresh" button (to refresh problem list).
		 */
		private static final double REFRESH_PROBLEM_LIST_BUTTON_WIDTH_PX = 100.0;
		
		/**
		 * Separation between east/west panels and center panel.
		 */
		private static final double SEP_PX = 10.0;
		
		/**
		 * Space above/below splitter.
		 */
		private static final double VSEP_PX = 5.0;

		private LayoutPanel westPanel;

		private PageNavPanel pageNavPanel;
		private TermAndCourseTreeView termAndCourseTreeView;
		private ProblemDescriptionView problemDescriptionView;
		private StatusMessageView statusMessageView;
		private ProblemListView2 problemListView2;
		private Button courseAdminButton;
		private Button userAdminButton;
		private Button loadProblemButton;
		private Button accountButton;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);

			//
			// West panel: has the terms and courses tree view.
			// Also, displays the Course admin and User admin buttons when
			// needed (for instructors). 
			//
			westPanel = new LayoutPanel();
			IsWidget coursesLabel = createSectionLabel("Courses");
			westPanel.add(coursesLabel);
			westPanel.setWidgetTopHeight(coursesLabel, 0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			westPanel.setWidgetLeftRight(coursesLabel, 0.0, Unit.PX, SEP_PX, Unit.PX);

			dockLayoutPanel.addWest(westPanel, WEST_PANEL_WIDTH_PX);

			//
			// East panel: has the PageNavPanel and the "My account" button.
			// This will be a good location for other user-centric widgets
			// (e.g., a list of todo items such as problems due soon?)
			//
			LayoutPanel eastPanel = new LayoutPanel();
			pageNavPanel = new PageNavPanel();
			pageNavPanel.setShowBackButton(false);
			eastPanel.add(pageNavPanel);
			eastPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);
			eastPanel.setWidgetLeftRight(pageNavPanel, SEP_PX, Unit.PX, 0.0, Unit.PX);
			
			accountButton = new Button("My account");
			accountButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleAccountButtonClicked();
				}
			});
			eastPanel.add(accountButton);
			eastPanel.setWidgetRightWidth(accountButton, 0.0, Unit.PX, 120.0, Unit.PX);
			eastPanel.setWidgetTopHeight(accountButton, PageNavPanel.HEIGHT_PX, Style.Unit.PX, COURSE_AND_USER_ADMIN_BUTTON_HEIGHT_PX, Unit.PX);

			dockLayoutPanel.addEast(eastPanel, 200);

			//
			// Center panel: has the problem list view, problem description view,
			// and status message view.
			//
			LayoutPanel centerPanel = new LayoutPanel();
			
			SplitLayoutPanel centerSplit = new SplitLayoutPanel();

			LayoutPanel problemDescriptionPanel = new LayoutPanel();
			IsWidget problemDescriptionLabel = createSectionLabel("Exercise Description");
			problemDescriptionPanel.add(problemDescriptionLabel);
			problemDescriptionPanel.setWidgetTopHeight(problemDescriptionLabel, VSEP_PX, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			problemDescriptionPanel.setWidgetLeftRight(problemDescriptionLabel, 0.0, Unit.PX, LOAD_PROBLEM_BUTTON_WIDTH_PX, Unit.PX);
			this.loadProblemButton = new Button("Load exercise!");
			loadProblemButton.setStylePrimaryName("cc-emphButton");
			loadProblemButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadProblemButtonClicked();
				}
			});
			problemDescriptionPanel.add(loadProblemButton);
			problemDescriptionPanel.setWidgetTopHeight(loadProblemButton, VSEP_PX, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			problemDescriptionPanel.setWidgetRightWidth(loadProblemButton, 0.0, Unit.PX, LOAD_PROBLEM_BUTTON_WIDTH_PX, Unit.PX);
			problemDescriptionView = new ProblemDescriptionView();
			problemDescriptionPanel.add(problemDescriptionView);
			problemDescriptionPanel.setWidgetTopBottom(problemDescriptionView, VSEP_PX+SectionLabel.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			problemDescriptionPanel.setWidgetLeftRight(problemDescriptionView, 0.0, Unit.PX, 0.0, Unit.PX);
			centerSplit.addSouth(problemDescriptionPanel, 300.0);

			LayoutPanel problemListPanel = new LayoutPanel();
			IsWidget problemLabel = createSectionLabel("Exercises");
			problemListPanel.add(problemLabel);
			problemListPanel.setWidgetTopHeight(problemLabel, 0.0, Unit.PX, SectionLabel.HEIGHT_PX, Unit.PX);
			problemListPanel.setWidgetLeftRight(problemLabel, 0.0, Unit.PX, 0.0, Unit.PX);
			problemListView2 = new ProblemListView2(CoursesAndProblemsPage2.this);
			problemListPanel.add(problemListView2);
			problemListPanel.setWidgetTopBottom(problemListView2, SectionLabel.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			problemListPanel.setWidgetLeftRight(problemListView2, 0.0, Unit.PX, 0.0, Unit.PX);
			Button refreshProblemListButton = new Button("Refresh");
			refreshProblemListButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleRefreshProblemListButtonClicked();
				}
			});
			problemListPanel.add(refreshProblemListButton);
			problemListPanel.setWidgetTopHeight(refreshProblemListButton, 0, Unit.PX, COURSE_AND_USER_ADMIN_BUTTON_HEIGHT_PX, Unit.PX);
			problemListPanel.setWidgetRightWidth(refreshProblemListButton, 0, Unit.PX, REFRESH_PROBLEM_LIST_BUTTON_WIDTH_PX, Unit.PX);
			centerSplit.add(problemListPanel);

			centerPanel.add(centerSplit);
			centerPanel.setWidgetTopBottom(centerSplit, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			
			statusMessageView = new StatusMessageView();
			centerPanel.add(statusMessageView);
			centerPanel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			
			dockLayoutPanel.add(centerPanel);

			initWidget(dockLayoutPanel);
		}

		private IsWidget createSectionLabel(String text) {
			return new SectionLabel(text);
		}

		private void loadProblemButtonClicked() {
			Problem problem = getSession().get(Problem.class);
			if (problem != null) {
				getSession().notifySubscribers(Session.Event.PROBLEM_CHOSEN, problem);
			}
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);

			// activate views
			problemListView2.activate(session, subscriptionRegistrar);
			problemDescriptionView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);

			// register a logout handler
			this.pageNavPanel.setLogoutHandler(new LogoutHandler(session));

			// Load courses
			getCourseAndCourseRegistrationsRPC(session);
		}

		protected void getCourseAndCourseRegistrationsRPC(final Session session) {
			RPC.getCoursesAndProblemsService.getCourseAndCourseRegistrations(new AsyncCallback<CourseAndCourseRegistration[]>() {
				@Override
				public void onSuccess(CourseAndCourseRegistration[] result) {
					GWT.log(result.length + " course(s) loaded");
					addSessionObject(result);
				}

				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							@Override
							public void run() {
								// Try again!
								getCourseAndCourseRegistrationsRPC(session);
							}
						});
					} else {
						GWT.log("Error loading courses", caught);
						session.add(StatusMessage.error("Error loading courses", caught));
					}
				}
			});
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
		 */
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseAndCourseRegistration[]) {
				CourseAndCourseRegistration[] courseAndRegList = (CourseAndCourseRegistration[]) hint;

				boolean isInstructor = false;

				// Determine if the user is an instructor for any of the courses
				for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
					if (courseAndReg.getCourseRegistration().getRegistrationType() == CourseRegistrationType.INSTRUCTOR) {
						GWT.log("Instructor for course " +  courseAndReg.getCourse().getName());
						isInstructor = true;
					}
				}

				// Courses are loaded - populate TermAndCourseTreeView.
				// If the user is an instructor for at least one course, leave some room for
				// the "Course admin" button.
				termAndCourseTreeView = new TermAndCourseTreeView(courseAndRegList);
				westPanel.add(termAndCourseTreeView);
				westPanel.setWidgetTopBottom(
						termAndCourseTreeView,
						SectionLabel.HEIGHT_PX + (isInstructor ? COURSE_AND_USER_ADMIN_BUTTON_HEIGHT_PX : 0.0),
						Unit.PX,
						0.0,
						Unit.PX);
				westPanel.setWidgetLeftRight(termAndCourseTreeView, 0.0, Unit.PX, SEP_PX, Unit.PX);

				// Create the "Problems" and "User" admin buttons if appropriate.
				if (isInstructor) {
					FlowPanel adminButtonPanel = new FlowPanel();
					
					InlineLabel manageLabel = new InlineLabel("Manage: ");
					adminButtonPanel.add(manageLabel);
					
					courseAdminButton = new Button("Problems");
					courseAdminButton.addClickHandler(new ClickHandler(){
						@Override
						public void onClick(ClickEvent event) {
							handleCourseAdminButtonClicked();
						}
					});
					adminButtonPanel.add(courseAdminButton);
					
					adminButtonPanel.add(new InlineHTML(" "));
					
					userAdminButton = new Button("Users");
					userAdminButton.addClickHandler(new ClickHandler(){
						/* (non-Javadoc)
						 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
						 */
						@Override
						public void onClick(ClickEvent event) {
							handleUserAdminButtonClicked();
						}
					});
					adminButtonPanel.add(userAdminButton);
					
					westPanel.add(adminButtonPanel);
					westPanel.setWidgetTopHeight(adminButtonPanel, SectionLabel.HEIGHT_PX, Unit.PX, COURSE_AND_USER_ADMIN_BUTTON_HEIGHT_PX + 4.0, Unit.PX);
					westPanel.setWidgetLeftRight(adminButtonPanel, 0.0, Unit.PX, 0.0, Unit.PX);

					// Disable buttons initially.  They will be enabled/disabled
					// appropriately as courses are selected.
					courseAdminButton.setEnabled(false);
					userAdminButton.setEnabled(false);
				}

				// add selection event handler
				termAndCourseTreeView.addSelectionHandler(new SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						CourseSelection courseSelection = termAndCourseTreeView.getSelectedCourseAndModule();
						if (courseSelection != null) {
							getSession().add(courseSelection);
						}
					}
				});
			} else if (key == Session.Event.ADDED_OBJECT && hint instanceof CourseSelection) {
				CourseSelection courseSelection = (CourseSelection) hint;
				Course course = courseSelection.getCourse();

				if (courseAdminButton != null || userAdminButton != null) {
					// Find the CourseRegistration for this Course
					CourseAndCourseRegistration[] courseAndRegList = getSession().get(CourseAndCourseRegistration[].class);
					for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
						if (courseAndReg.getCourse() == course) {
							// Enable or disable the courseAdminButton (and userAdminButton) depending on whether or not
							// user is an instructor.
							boolean isInstructor = courseAndReg.getCourseRegistration().getRegistrationType() == CourseRegistrationType.INSTRUCTOR;
							courseAdminButton.setEnabled(isInstructor);
							userAdminButton.setEnabled(isInstructor);
							GWT.log((isInstructor ? "enable" : "disable") + " courseAdminButton");
						}
					}
				}
			}
		}

		protected void handleCourseAdminButtonClicked() {
			GWT.log("Course admin button clicked");
			Course course = getCurrentCourse();
			if (course != null) {
				getSession().notifySubscribers(Session.Event.COURSE_ADMIN, course);
			}
		}

		protected void handleUserAdminButtonClicked() {
			GWT.log("User admin button clicked");
			Course course = getCurrentCourse();
			if (course != null) {
				getSession().notifySubscribers(Session.Event.USER_ADMIN, course);
			}
		}

		protected void handleAccountButtonClicked() {
			GWT.log("My account button clicked");
			Course course = getCurrentCourse();
			if(course != null) {
				getSession().notifySubscribers(Session.Event.USER_ACCOUNT, course);
			}
		}
		
		protected void handleRefreshProblemListButtonClicked() {
			CourseSelection courseSelection = getSession().get(CourseSelection.class);
			if (courseSelection != null) {
				// Force a reload of the course
				getSession().add(courseSelection);
			}
		}
	}

	private UI ui;

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
	 */
	@Override
	public void createWidget() {
		ui = new UI();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
	 */
	@Override
	public void activate() {
		getSession().add(new ProblemAndSubmissionReceipt[0]);
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#deactivate()
	 */
	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#getWidget()
	 */
	@Override
	public IsWidget getWidget() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
	 */
	@Override
	public boolean isActivity() {
		return true;
	}

}
