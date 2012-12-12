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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ChoiceDialogBox;
import org.cloudcoder.app.client.view.EditBooleanField;
import org.cloudcoder.app.client.view.EditDateField;
import org.cloudcoder.app.client.view.EditDateTimeField;
import org.cloudcoder.app.client.view.EditEnumField;
import org.cloudcoder.app.client.view.EditModelObjectField;
import org.cloudcoder.app.client.view.EditStringField;
import org.cloudcoder.app.client.view.EditStringFieldWithAceEditor;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TestCaseEditor;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.EditProblemAdapter;
import org.cloudcoder.app.shared.model.IProblem;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemData;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Page for editing a {@link ProblemAndTestCaseList}.
 * 
 * @author David Hovemeyer
 */
public class EditProblemPage extends CloudCoderPage {
	
	private enum Confirm {
		OK, CANCEL,
	}
	
	private class UI extends ResizeComposite implements SessionObserver {
		private static final double CENTER_PANEL_V_SEP_PX = 10.0;
		private static final double SAVE_BUTTON_HEIGHT_PX = 32.0;

		private DockLayoutPanel dockLayoutPanel;
		private Label pageLabel;
		private PageNavPanel pageNavPanel;
		private StatusMessageView statusMessageView;
		private FlowPanel centerPanel;
		private List<EditModelObjectField<IProblem, ?>> problemFieldEditorList;
		private List<TestCaseEditor> testCaseEditorList;
		private Button addTestCaseButton;
		private FlowPanel addTestCaseButtonPanel;
		private ProblemAndTestCaseList problemAndTestCaseListOrig;
		
		public UI() {
			this.dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			// At top of page, show name of course, a PageNavPanel,
			// and a button for saving the edited problem/testcases.
			LayoutPanel northPanel = new LayoutPanel();
			this.pageLabel = new Label("");
			pageLabel.setStyleName("cc-courseLabel");
			northPanel.add(pageLabel);
			northPanel.setWidgetLeftRight(pageLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopBottom(pageLabel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopBottom(pageNavPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			Button saveButton = new Button("Save problem!");
			saveButton.setStyleName("cc-emphButton");
			saveButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleSaveProblem();
				}
			});
			northPanel.add(saveButton);
			northPanel.setWidgetLeftWidth(saveButton, 0.0, Unit.PX, 140.0, Unit.PX);
			northPanel.setWidgetBottomHeight(saveButton, CENTER_PANEL_V_SEP_PX, Unit.PX, SAVE_BUTTON_HEIGHT_PX, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX + SAVE_BUTTON_HEIGHT_PX + CENTER_PANEL_V_SEP_PX);
			
			// At bottom of page, show a StatusMessageView.
			// Put it in a LayoutPanel so we can add a bit of vertical space to
			// separate it from the center panel.
			this.statusMessageView = new StatusMessageView();
			LayoutPanel southPanel = new LayoutPanel();
			southPanel.add(statusMessageView);
			southPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			southPanel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			dockLayoutPanel.addSouth(southPanel, StatusMessageView.HEIGHT_PX + CENTER_PANEL_V_SEP_PX);
			
			// Create UI for editing problem and test cases
			problemFieldEditorList = new ArrayList<EditModelObjectField<IProblem, ?>>();
			createProblemFieldEditors();

			this.centerPanel = new FlowPanel();
			
			// Add editor widgets for Problem fields
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				centerPanel.add(editor.getUI());
			}
			
			ScrollPanel scrollPanel = new ScrollPanel(centerPanel);
			scrollPanel.setStyleName("cc-editProblemPanel", true);
			dockLayoutPanel.add(scrollPanel);
			
			initWidget(dockLayoutPanel);
		}

		protected void handleSaveProblem() {
			// Commit the contents of all editors
			if (!commitAll()) {
				getSession().add(StatusMessage.error("One or more field values is invalid"));
				return;
			}
			
			// Create a pending operation message
			getSession().add(StatusMessage.pending("Sending problem data to server..."));
			
			// Attempt to store the problem and its test cases in the database
			final ProblemAndTestCaseList problemAndTestCaseList = getSession().get(ProblemAndTestCaseList.class);
			final Course course = getSession().get(Course.class);
			saveProblem(problemAndTestCaseList, course);
		}

		protected void saveProblem(
				final ProblemAndTestCaseList problemAndTestCaseList,
				final Course course) {
			RPC.getCoursesAndProblemsService.storeProblemAndTestCaseList(problemAndTestCaseList, course, new AsyncCallback<ProblemAndTestCaseList>() {
				@Override
				public void onSuccess(ProblemAndTestCaseList result) {
					getSession().add(StatusMessage.goodNews("Problem saved successfully"));
					
					// Make the returned ProblemAndTestCaseList current
					problemAndTestCaseListOrig.copyFrom(result);
					problemAndTestCaseList.copyFrom(result);
					
					// The TestCaseEditors must be updated, because the TestCase objects
					// they are editing have changed.
					int count = 0;
					TestCase[] currentTestCases = problemAndTestCaseList.getTestCaseList();
					for (TestCaseEditor editor : testCaseEditorList) {
						editor.setTestCase(currentTestCases[count++]);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								saveProblem(problemAndTestCaseList, course);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Could not save problem: " + caught.getMessage()));
					}
				}
			});
		}

		private void createProblemFieldEditors() {
			problemFieldEditorList.add(new EditEnumField<IProblem, ProblemType>("Problem type", ProblemType.class, ProblemData.PROBLEM_TYPE));
			problemFieldEditorList.add(new EditStringField<IProblem>("Problem name", ProblemData.TESTNAME));
			problemFieldEditorList.add(new EditStringField<IProblem>("Brief description", ProblemData.BRIEF_DESCRIPTION));
			
			EditStringFieldWithAceEditor<IProblem> descriptionEditor =
					new EditStringFieldWithAceEditor<IProblem>("Full description (HTML)", ProblemData.DESCRIPTION);
			descriptionEditor.setEditorMode(AceEditorMode.HTML);
			descriptionEditor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			problemFieldEditorList.add(descriptionEditor);
			
			// In the editor for the skeleton, we keep the editor mode in sync
			// with the problem type.  (I.e., for a Java problem we want Java
			// mode, for Python we want Python mode, etc.)
			EditStringFieldWithAceEditor<IProblem> skeletonEditor =
					new EditStringFieldWithAceEditor<IProblem>("Skeleton code", ProblemData.SKELETON) {
						@Override
						public void update() {
							setLanguage();
							super.update();
						}
						@Override
						public void onModelObjectChange() {
							setLanguage();
						}
						private void setLanguage() {
							AceEditorMode editorMode = ViewUtil.getModeForLanguage(getModelObject().getProblemType().getLanguage());
							setEditorMode(editorMode);
						}
					};
			skeletonEditor.setEditorTheme(AceEditorTheme.VIBRANT_INK);
			problemFieldEditorList.add(skeletonEditor);
			
			// We don't need an editor for schema version - problems/testcases are
			// automatically converted to the latest version when they are imported.
			
			problemFieldEditorList.add(new EditStringField<IProblem>("Author name", ProblemData.AUTHOR_NAME));
			problemFieldEditorList.add(new EditStringField<IProblem>("Author email", ProblemData.AUTHOR_EMAIL));
			problemFieldEditorList.add(new EditStringField<IProblem>("Author website", ProblemData.AUTHOR_WEBSITE));
			problemFieldEditorList.add(new EditDateField<IProblem>("Creation date", ProblemData.TIMESTAMP_UTC));
			problemFieldEditorList.add(new EditEnumField<IProblem, ProblemLicense>("License", ProblemLicense.class, ProblemData.LICENSE));
			problemFieldEditorList.add(new EditDateTimeField<IProblem>("When assigned", Problem.WHEN_ASSIGNED));
			problemFieldEditorList.add(new EditDateTimeField<IProblem>("When due", Problem.WHEN_DUE));
			problemFieldEditorList.add(new EditBooleanField<IProblem>(
					"Problem visible to students",
					"Check to make problem visible to students",
					Problem.VISIBLE));
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
		 */
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// The session should contain a ProblemAndTestCaseList.
			ProblemAndTestCaseList problemAndTestCaseList = session.get(ProblemAndTestCaseList.class);

			// Make a copy of the ProblemAndTestCaseList being edited.
			// This will allow us to detect whether or not it has been changed
			// by the user.
			this.problemAndTestCaseListOrig = new ProblemAndTestCaseList();
			problemAndTestCaseListOrig.copyFrom(problemAndTestCaseList);
			
			// Activate views
			final Course course = session.get(Course.class);
			pageLabel.setText(
					(problemAndTestCaseList.getProblem().getProblemId()== null ? "Create new" : "Edit") +
					" problem in " + course.toString());
			
			// The nested Runnable objects here are due to the strange way DialogBoxes
			// work in GWT - show() and center() return immediately rather than waiting
			// for the dialog to be dismissed.  Thus, it is necessary to use a callback
			// to capture a choice made in a dialog box.  Bleh.
			pageNavPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					leavePage(new Runnable() {
						public void run() {
							// Purge the list of ProblemAndSubmissionReceipts, in case a
							// problem was edited by this page.  That will force CourseAdminPage to
							// reload the problem list for the Course.
							getSession().remove(ProblemAndSubmissionReceipt[].class);
							
							session.notifySubscribers(Session.Event.COURSE_ADMIN, course);
						}
					});
				}
			});
			pageNavPanel.setLogoutHandler(new Runnable() {
				@Override
				public void run() {
					leavePage(new Runnable(){
						@Override
						public void run() {
							new LogoutHandler(session).run();
						}
					});
				}
			});
			statusMessageView.activate(session, subscriptionRegistrar);
			
			// Create a ProblemAdapter to serve as the IProblem edited by the problem editors.
			// Override the onChange() method to notify editors that the model object has changed
			// in some way.
			IProblem problemAdapter = new EditProblemAdapter(problemAndTestCaseList.getProblem()) {
				@Override
				protected void onChange() {
					for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
						editor.onModelObjectChange();
					}
				}
			};
			
			// Set the Problem in all problem field editors.
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				editor.setModelObject(problemAdapter);
			}
			
			// Add TestCaseEditors for test cases.
			testCaseEditorList = new ArrayList<TestCaseEditor>();
			for (TestCase testCase : problemAndTestCaseList.getTestCaseList()) {
				final TestCaseEditor testCaseEditor = new TestCaseEditor();
				testCaseEditor.setDeleteHandler(new Runnable() {
					@Override
					public void run() {
						handleDeleteTestCase(testCaseEditor);
					}
				});
				testCaseEditorList.add(testCaseEditor);
				testCaseEditor.setTestCase(testCase);
				centerPanel.add(testCaseEditor.getUI());
			}
			
			// Add a button to create a new TestCase and TestCaseEditor.
			// Put it in a FlowPanel to ensure that it's in its own div.
			// (Could also use this to style/position the button.)
			this.addTestCaseButtonPanel = new FlowPanel();
			addTestCaseButton = new Button("Add Test Case");
			addTestCaseButton.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					handleAddTestCase();
				}
			});
			addTestCaseButtonPanel.add(addTestCaseButton);
			centerPanel.add(addTestCaseButtonPanel);
		}
		
		private void leavePage(final Runnable action) {
			// Commit all changes made in the editors to the model objects.
			boolean successfulCommit = commitAll();
			boolean problemModified = isProblemModified();
			
			// If the Problem has not been modified, then it's fine to leave the page
			// without a prompt.
			if (successfulCommit && !problemModified) {
				action.run();
				return;
			}
			
			if (!successfulCommit) {
				getSession().add(StatusMessage.error("One or more values is invalid"));
			}
			GWT.log("Problem " + (problemModified ? "has" : "has not") + " been modified");
			
			// Prompt user to confirm leaving page (and abandoning changes to Problem)
			ChoiceDialogBox<Confirm> confirmDialog = new ChoiceDialogBox<Confirm>(
					"Save changes to problem?",
					"The problem has been modified: are you sure you want to abandon the changes?",
					new ChoiceDialogBox.ChoiceHandler<Confirm>() {
						public void handleChoice(Confirm choice) {
							if (choice == Confirm.OK) {
								action.run();
							}
						}
					});
			confirmDialog.addChoice("Abandon changes", Confirm.OK);
			confirmDialog.addChoice("Don't abandon changes", Confirm.CANCEL);
			confirmDialog.center();
		}
		
		/**
		 * Commit all changes in the UI to the underlying ProblemAndTestCaseList model object.
		 * 
		 * @return true if all committed values were valid, false if at least
		 *         one editor contains an invalid value
		 */
		private boolean commitAll() {
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				editor.commit();
			}
			for (TestCaseEditor editor : testCaseEditorList) {
				editor.commit();
			}
			boolean success = true;
			for (EditModelObjectField<IProblem, ?> editor : problemFieldEditorList) {
				if (editor.isCommitError()) {
					success = false;
				}
			}
			return success;
		}
		
		/**
		 * @return true if the ProblemAndTestCaseList has been modified, false otherwise
		 */
		private boolean isProblemModified() {
			return !getSession().get(ProblemAndTestCaseList.class).equals(problemAndTestCaseListOrig);
		}

		/**
		 * Called when the user clicks the "Delete" button in a TestCaseEditor.
		 * Removes the editor from the UI and removes the test case from
		 * the underlying ProblemAndTestCaseList.
		 * 
		 * @param testCaseEditor the TestCaseEditor
		 */
		protected void handleDeleteTestCase(TestCaseEditor testCaseEditor) {
			getSession().get(ProblemAndTestCaseList.class).removeTestCase(testCaseEditor.getTestCase());
			centerPanel.remove(testCaseEditor.getUI());
			testCaseEditorList.remove(testCaseEditor);
		}

		protected void handleAddTestCase() {
			// Add the TestCase to the ProblemAndTestCaseList
			TestCase testCase = TestCase.createEmpty();
			int numTests = testCaseEditorList.size();
			testCase.setTestCaseName("t"+numTests);
			getSession().get(ProblemAndTestCaseList.class).addTestCase(testCase);

			// Add a new TestCase editor and its UI widget
			TestCaseEditor testCaseEditor = new TestCaseEditor();
			testCaseEditorList.add(testCaseEditor);
			centerPanel.insert(testCaseEditor.getUI(), centerPanel.getWidgetIndex(addTestCaseButtonPanel));
			testCaseEditor.setTestCase(testCase);
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
