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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ButtonPanel;
import org.cloudcoder.app.client.view.ChoiceDialogBox;
import org.cloudcoder.app.client.view.CourseAdminProblemListView;
import org.cloudcoder.app.client.view.IButtonPanelAction;
import org.cloudcoder.app.client.view.ImportProblemDialog;
import org.cloudcoder.app.client.view.OkDialogBox;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ShareProblemDialog;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndModule;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page for performing course admin actions related to {@link Problem}s.
 * 
 * @author David Hovemeyer
 */
public class ProblemAdminPage extends CloudCoderPage {
	private enum ProblemAction implements IButtonPanelAction {
		NEW("New", "Create a new exercise"),
		EDIT("Edit", "Edit the selected exercise"),
		DELETE("Delete", "Delete the selected exercise"),
		STATISTICS("Statistics", "See statistics on selected exercise"),
		IMPORT("Import", "Import an exercise from the CloudCoder exercise repository"),
		SHARE("Share", "Shared selected exercise by publishing it to the CloudCoder exercise repository"),
		MAKE_VISIBLE("Make visible", "Make selected exerise visible to students"),
		MAKE_INVISIBLE("Make invisible", "Make selected exercise invisible to students"),
		QUIZ("Quiz", "Give selected exercise as a quiz");
		
		private final String name;
		private final String tooltip;
		
		private ProblemAction(String name, String tooltip) {
			this.name = name;
			this.tooltip = tooltip;
		}
		
		public String getName() {
			return name;
		}
		
		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.view.IButtonPanelAction#getTooltip()
		 */
		@Override
		public String getTooltip() {
			return tooltip;
		}
		
		public boolean isEnabledByDefault() {
			return this == NEW || this == IMPORT;
		}
	}
	
	private enum DeleteChoice {
		CANCEL,
		DELETE,
	}
	
	private class UI extends Composite implements SessionObserver, Subscriber {
		private PageNavPanel pageNavPanel;
		private Label courseLabel;
		private ButtonPanel<ProblemAction> buttonPanel;
		private CourseAdminProblemListView courseAdminProblemListView;
		private StatusMessageView statusMessageView;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			// Create a north panel with course info, PageNavPanel, and button panel
			LayoutPanel northPanel = new LayoutPanel();
			
			this.courseLabel = new Label();
			northPanel.add(courseLabel);
			northPanel.setWidgetLeftRight(courseLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopHeight(courseLabel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);
			courseLabel.setStyleName("cc-courseLabel");
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);

			// Create a button panel with buttons for problem-related actions
			buttonPanel = new ButtonPanel<ProblemAction>(ProblemAction.values()) {
				@Override
				public void onButtonClick(ProblemAction action) {
					onProblemButtonClick(action);
				}
				
				/* (non-Javadoc)
				 * @see org.cloudcoder.app.client.view.ButtonPanel#isEnabled(org.cloudcoder.app.client.view.IButtonPanelAction)
				 */
				@Override
				public boolean isEnabled(ProblemAction action) {
					if (action == ProblemAction.MAKE_VISIBLE || action == ProblemAction.MAKE_INVISIBLE) {
						Problem problem = getSession().get(Problem.class);
						//    PV  MV  enable
						//    t   t   f
						//    t   f   t
						//    f   t   t
						//    f   f   f
						return (problem != null) && problem.isVisible() != (action == ProblemAction.MAKE_VISIBLE);
					} else {
						return true;
					}
				}
			};
			
			northPanel.add(buttonPanel);
			northPanel.setWidgetTopHeight(buttonPanel, PageNavPanel.HEIGHT_PX, Unit.PX, ButtonPanel.HEIGHT_PX, Unit.PX);
			northPanel.setWidgetLeftRight(buttonPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX + ButtonPanel.HEIGHT_PX + 10.0);
			
			// Create a south panel with a StatusMessageView
			this.statusMessageView = new StatusMessageView();
			dockLayoutPanel.addSouth(statusMessageView, StatusMessageView.HEIGHT_PX);
			
			// Create a center panel with problems list.
			this.courseAdminProblemListView = new CourseAdminProblemListView(ProblemAdminPage.this);
			dockLayoutPanel.add(courseAdminProblemListView);
			// Handle edits to the module name.
			courseAdminProblemListView.setEditModuleNameCallback(new ICallback<ProblemAndModule>() {
				public void call(ProblemAndModule value) {
					setProblemModuleName(value);
				}
			});
			
			initWidget(dockLayoutPanel);
		}

		private void setProblemModuleName(final ProblemAndModule value) {
			RPC.getCoursesAndProblemsService.setModule(value.getProblem(), value.getModule().getName(), new AsyncCallback<Module>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							@Override
							public void run() {
								setProblemModuleName(value);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Could not set module for exercise", caught));
					}
				}

				@Override
				public void onSuccess(Module result) {
					getSession().add(StatusMessage.goodNews("Successfully changed module for exercise"));
				}
			});
		}

		/**
		 * Called when a problem button is clicked.
		 * 
		 * @param action the ProblemButtonAction
		 */
		protected void onProblemButtonClick(ProblemAction action) {
			switch (action) {
			case NEW:
				handleNewProblem();
				break;

			case EDIT:
				handleEditProblem();
				break;
				
			case DELETE:
				handleDeleteProblem();
				break;

			case STATISTICS:
				handleStatistics();
				break;
				
			case SHARE:
				doShareProblem();
				break;
				
			case IMPORT:
				doImportProblem();
				break;
				
			case MAKE_VISIBLE:
			case MAKE_INVISIBLE:
				doChangeVisibility(action == ProblemAction.MAKE_VISIBLE);
				break;
				
			case QUIZ:
				handleQuiz();
				break;
			}
		}

		private void doChangeVisibility(final boolean visible) {
			Problem chosen = getSession().get(Problem.class);
			final Course course = getCurrentCourse();
			
			getSession().add(StatusMessage.pending("Changing visibility of problem..."));
			
			loadProblemAndTestCaseList(chosen, new ICallback<ProblemAndTestCaseList>() {
				/* (non-Javadoc)
				 * @see org.cloudcoder.app.shared.model.ICallback#call(java.lang.Object)
				 */
				@Override
				public void call(ProblemAndTestCaseList value) {
					value.getProblem().setVisible(visible);
					updateProblem(value, course);
				}
			});
		}

		private void doShareProblem() {
			final Problem chosen = getSession().get(Problem.class);
			
			if (!chosen.getLicense().isPermissive()) {
				OkDialogBox licenseDialog = new OkDialogBox(
						"Sharing requires a permissive license",
						"Sharing a problem requires a permissive license. Please edit the problem " +
						"and choose a permissive license such as Creative Commons or GNU FDL.");
				licenseDialog.center();
				return;
			}
			
			if (chosen.getProblemAuthorship() == ProblemAuthorship.IMPORTED) {
				OkDialogBox problemAuthorshipDialog = new OkDialogBox(
						"Sharing not allowed for unmodified problems",
						"This problem was imported from the exercise repository, but not modified. " +
						"You can share it if you make some changes first.");
				problemAuthorshipDialog.center();
				return;
			}

			loadProblemAndTestCaseList(chosen, new ICallback<ProblemAndTestCaseList>() {
				@Override
				public void call(ProblemAndTestCaseList value) {
					ShareProblemDialog shareProblemDialog = new ShareProblemDialog();
					shareProblemDialog.setExercise(value);
					shareProblemDialog.setResultCallback(new ICallback<OperationResult>() {
						public void call(OperationResult value) {
							// Add a StatusMessage with the result of the operation
							GWT.log("share problem result: " + value.isSuccess() + ":" + value.getMessage());

							if (value.isSuccess()) {
								getSession().add(StatusMessage.goodNews(value.getMessage()));
							} else {
								getSession().add(StatusMessage.error(value.getMessage()));
							}
						}
					});
					
					shareProblemDialog.center();
				}
			});
		}

		private void doImportProblem() {
			ImportProblemDialog dialog = new ImportProblemDialog();
			final Course course = getCurrentCourse();
			dialog.setCourse(course);
			dialog.setResultCallback(new ICallback<ProblemAndTestCaseList>() {
				@Override
				public void call(ProblemAndTestCaseList value) {
					if (value != null) {
						getSession().add(StatusMessage.goodNews("Exercise imported successfully!"));
						
						reloadProblems(course);
					} else {
						getSession().add(StatusMessage.error("Exercise was not found"));
					}
				}
			});
			
			dialog.center();
		}

		private void handleEditProblem() {
			// Get the full ProblemAndTestCaseList for the chosen Problem
			final Problem chosen = getSession().get(Problem.class);
			
			loadProblemAndTestCaseList(chosen, new ICallback<ProblemAndTestCaseList>() {
				@Override
				public void call(ProblemAndTestCaseList value) {
					getSession().add(value);
					getSession().notifySubscribers(Session.Event.EDIT_PROBLEM, value);
				}
			});
		}
		
		private void handleDeleteProblem() {
			final Problem chosen = getSession().get(Problem.class);
			final Course course = getCurrentCourse();
			
			// Only invisible problems may be deleted
			if (chosen.isVisible()) {
				OkDialogBox visibleDialog = new OkDialogBox(
						"Problem is visible",
						"You can't delete a problem which is visible to students. Make it invisible first.");
				visibleDialog.center();
				return;
			}
			
			// Confirm using a dialog
			ChoiceDialogBox<DeleteChoice> confirmDeleteDialog = new ChoiceDialogBox<DeleteChoice>(
					"Really delete problem?",
					"Do you really want to delete the selected problem (" + chosen.getTestname() + ")? " +
					"If you click 'Delete problem' there will be no way to undo the deletion.",
					new ChoiceDialogBox.ChoiceHandler<DeleteChoice>() {
						@Override
						public void handleChoice(DeleteChoice choice) {
							if (choice == DeleteChoice.DELETE) {
								getSession().add(StatusMessage.pending("Deleting problem..."));
								deleteProblem(chosen, course);
							}
						}
					}
			);
			confirmDeleteDialog.addChoice("Cancel", DeleteChoice.CANCEL);
			confirmDeleteDialog.addChoice("Delete problem", DeleteChoice.DELETE);
			confirmDeleteDialog.center();
		}

		/**
		 * @author Andrei Papancea
		 * 
		 * Updated: 9/17/2012 at 3:44 PM
		 * 
		 */
		private void handleStatistics() {
			// Get the selected problem
			final Problem chosen = getSession().get(Problem.class);
			
			// Switch to the StatisticsPage
			getSession().notifySubscribers(Session.Event.STATISTICS, chosen);
		}

		/**
		 * Load a complete {@link ProblemAndTestCaseList} for given {@link Problem}.
		 * An RPC call is made to fetch the {@link TestCase}s for the problem,
		 * and the result is delivered asynchronously to a callback.
		 *
		 * @param problem    the problem
		 * @param callback   the callback to receive the full {@link ProblemAndTestCaseList}
		 */
		private void loadProblemAndTestCaseList(
				final Problem problem,
				final ICallback<ProblemAndTestCaseList> callback) {
			RPC.getCoursesAndProblemsService.getTestCasesForProblem(problem.getProblemId(), new AsyncCallback<TestCase[]>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								loadProblemAndTestCaseList(problem, callback);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Could not load test cases for problem: " + caught.getMessage()));
					}
				}

				@Override
				public void onSuccess(TestCase[] result) {
					// Success!
					ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
					problemAndTestCaseList.setProblem(problem);
					problemAndTestCaseList.setTestCaseList(result);
					callback.call(problemAndTestCaseList);
				}
			});
		}
		
		private void handleNewProblem() {
			Problem problem = new Problem();
			Problem.initEmpty(problem);
			
			// Set default when assigned and when due dates/times
			// (assigned now, due in 48 hours)
			problem.setWhenAssigned(System.currentTimeMillis());
			problem.setWhenDue(problem.getWhenAssigned() + (48L*60L*60L*1000L));
			
			// Set author name, email, and website based on User information
			User user = getSession().get(User.class);
			problem.setAuthorName(user.getFirstname() + " " + user.getLastname());
			problem.setAuthorEmail(user.getEmail());
			problem.setAuthorWebsite(user.getWebsite());
			
			// Set course id
			problem.setCourseId(getCurrentCourse().getId());
			
			// Initially there are no test cases
			TestCase[] testCaseList= new TestCase[0];
			
			// Edit it!
			ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
			problemAndTestCaseList.setProblem(problem);
			problemAndTestCaseList.setTestCaseList(testCaseList);
			getSession().add(problemAndTestCaseList);
			getSession().notifySubscribers(Session.Event.EDIT_PROBLEM, problemAndTestCaseList);
		}
		
		private void handleQuiz() {
			Problem selected = getSession().get(Problem.class);
			if (selected != null) {
				if (selected.isVisible()) {
					getSession().add(StatusMessage.error("Quiz problems must not be visible to students!"));
					return;
				}
				
				getSession().notifySubscribers(Session.Event.START_QUIZ, selected);
			}
		}

		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
			
			// Activate views
			pageNavPanel.setBackHandler(new BackHomeHandler(session));
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			courseAdminProblemListView.activate(session, subscriptionRegistrar);
			statusMessageView.activate(session, subscriptionRegistrar);
			
			// The session should contain a course
			Course course = getCurrentCourse();
			courseLabel.setText("Problems in " + course.getName() + " - " + course.getTitle());
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
		 */
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == Session.Event.ADDED_OBJECT && (hint instanceof Problem)) {
				// Problem selected: enable/disable buttons appropriately
				buttonPanel.updateButtonEnablement();
			}
		}

		public void deleteProblem(final Problem chosen, final Course course) {
			RPC.getCoursesAndProblemsService.deleteProblem(course, chosen, new AsyncCallback<OperationResult>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								deleteProblem(chosen, course);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Error deleting problem: " + caught.getMessage()));
					}
				}
				@Override
				public void onSuccess(OperationResult result) {
					if (result.isSuccess()) {
						getSession().add(StatusMessage.goodNews(result.getMessage()));
						reloadProblems(course);
					} else {
						getSession().add(StatusMessage.error(result.getMessage()));
					}
				}
			});
		}

		public void updateProblem(final ProblemAndTestCaseList value, final Course course) {
			RPC.getCoursesAndProblemsService.storeProblemAndTestCaseList(value, course, new AsyncCallback<ProblemAndTestCaseList>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								updateProblem(value, course);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Could not update problem visibility: " + caught.getMessage()));
					}
				}
				
				/* (non-Javadoc)
				 * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
				 */
				@Override
				public void onSuccess(ProblemAndTestCaseList result) {
					getSession().add(StatusMessage.goodNews("Problem visibility updated successfully"));
					reloadProblems(course);
				}
			});
		}

		public void reloadProblems(final Course course) {
			// Reload problems
			SessionUtil.loadProblemAndSubmissionReceiptsInCourse(ProblemAdminPage.this, course, getSession());
			
			// If a problem is selected, add it to the session
			// (so the buttons are enabled/disable appropriately).
			Problem currentProblem = courseAdminProblemListView.getSelected();
			if (currentProblem != null) {
				getSession().add(currentProblem);
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
