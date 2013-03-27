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

import org.cloudcoder.app.client.PageStack;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.Slider;
import org.cloudcoder.app.client.view.SliderEvent;
import org.cloudcoder.app.client.view.SliderListener;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.TestOutcomeSummaryView;
import org.cloudcoder.app.client.view.TestResultListView;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.UserSelection;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Page for viewing a user's submissions on a particular {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class UserProblemSubmissionsPage extends CloudCoderPage {
	private class UI extends Composite implements SessionObserver {
		private static final double SLIDER_HEIGHT_PX = 28.0;
		
		private Label usernameAndProblemLabel;
		private PageNavPanel pageNavPanel;
		private Slider submissionSlider;
		private Button prevSubmissionButton;
		private Button nextSubmissionButton;
		private StatusMessageView statusMessageView;
		private TestOutcomeSummaryView testOutcomeSummaryView;
		private TestResultListView testResultListView;
		private AceEditor editor;

		public UI() {
			SplitLayoutPanel panel = new SplitLayoutPanel();
			
			// North panel has username/problem name, page nav panel,
			// and slider to select the user's submissions.
			LayoutPanel northPanel = new LayoutPanel();
			
			this.usernameAndProblemLabel = new Label("");
			usernameAndProblemLabel.setStyleName("cc-problemName", true);
			northPanel.add(usernameAndProblemLabel);
			northPanel.setWidgetLeftRight(usernameAndProblemLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(usernameAndProblemLabel, 0.0, Unit.PX, 22.0, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			
			this.submissionSlider = new Slider("ccSubSlider");
			submissionSlider.setMinimum(1);
			submissionSlider.setMaximum(10);
			northPanel.add(submissionSlider);
			northPanel.setWidgetLeftRight(submissionSlider, (38.0 * 2), Unit.PX, 0, Unit.PX);
			northPanel.setWidgetTopHeight(submissionSlider, 36.0, Unit.PX, SLIDER_HEIGHT_PX, Unit.PX);
			
			this.prevSubmissionButton = new Button("<");
			northPanel.add(prevSubmissionButton);
			northPanel.setWidgetLeftWidth(prevSubmissionButton, 0.0, Unit.PX, 30, Unit.PX);
			northPanel.setWidgetTopHeight(prevSubmissionButton, 36.0, Unit.PX, SLIDER_HEIGHT_PX, Unit.PX);
			prevSubmissionButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onChangeSubmission(-1);
				}
			});

			this.nextSubmissionButton = new Button(">");
			northPanel.add(nextSubmissionButton);
			northPanel.setWidgetLeftWidth(nextSubmissionButton, 38.0, Unit.PX, 30, Unit.PX);
			northPanel.setWidgetTopHeight(nextSubmissionButton, 36.0, Unit.PX, SLIDER_HEIGHT_PX, Unit.PX);
			nextSubmissionButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onChangeSubmission(1);
				}
			});

			panel.addNorth(northPanel, PageNavPanel.HEIGHT_PX + 8.0 + SLIDER_HEIGHT_PX);
			
			// South panel has status message view, test outcome summary view,
			// and test result view.  TODO: indicate whether or not the sumission compiled.
			LayoutPanel southPanel = new LayoutPanel();
			
			this.statusMessageView = new StatusMessageView();
			southPanel.add(statusMessageView);
			southPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, TestOutcomeSummaryView.WIDTH_PX, Unit.PX);
			southPanel.setWidgetTopHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			
			this.testOutcomeSummaryView = new TestOutcomeSummaryView();
			southPanel.add(testOutcomeSummaryView);
			southPanel.setWidgetRightWidth(testOutcomeSummaryView, 0.0, Unit.PX, TestOutcomeSummaryView.WIDTH_PX, Unit.PX);
			southPanel.setWidgetTopHeight(testOutcomeSummaryView, 0.0, Unit.PX, TestOutcomeSummaryView.HEIGHT_PX, Unit.PX);
			
			this.testResultListView = new TestResultListView();
			southPanel.add(testResultListView);
			southPanel.setWidgetLeftRight(testResultListView, 0.0, Unit.PX, 0.0, Unit.PX);
			southPanel.setWidgetTopBottom(testResultListView, TestOutcomeSummaryView.HEIGHT_PX, Unit.PX, 0.0, Unit.PX);
			
			panel.addSouth(southPanel, 200.0);
			
			// Center panel has a read-only ACE editor showing the source code.
			this.editor = new AceEditor(true);
			panel.add(editor);
			
			initWidget(panel);
		}

		private void onChangeSubmission(int delta) {
			int value = submissionSlider.getValue();
			value += delta;
			if (value >= submissionSlider.getMinimum() && value <= submissionSlider.getMaximum()) {
				submissionSlider.setValue(value);
			}
		}

		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// Show username, problem name and description
			Problem problem = session.get(Problem.class);
			UserSelection userSelection = session.get(UserSelection.class);
			usernameAndProblemLabel.setText(userSelection.getUser().getUsername() + ", " + problem.toNiceString());
			
			// Activate editor
			editor.startEditor();
			editor.setFontSize("14px");
			editor.setReadOnly(true);
			editor.setTheme(AceEditorTheme.VIBRANT_INK);
			AceEditorMode mode = ViewUtil.getModeForLanguage(problem.getProblemType().getLanguage());
			if (mode != null) {
				editor.setMode(mode);
			}
			
			// Activate views
			pageNavPanel.setBackHandler(new PageBackHandler(session));
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			statusMessageView.activate(session, subscriptionRegistrar);
			testOutcomeSummaryView.activate(session, subscriptionRegistrar);
			testResultListView.activate(session, subscriptionRegistrar);
			
			// Get all SubmissionReceipts for this user on this problem
			RPC.getCoursesAndProblemsService.getAllSubmissionReceiptsForUser(problem, userSelection.getUser(), new AsyncCallback<SubmissionReceipt[]>() {
				@Override
				public void onSuccess(SubmissionReceipt[] result) {
					onLoadSubmissionReceipts(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					session.add(StatusMessage.error("Could not get submission receipts", caught));
				}
			});
		}

		private void onLoadSubmissionReceipts(SubmissionReceipt[] result) {
			getSession().add(result);
			
			submissionSlider.setMinimum(0);
			submissionSlider.setMaximum(result.length - 1);
			
			submissionSlider.addListener(new SliderListener() {
				@Override
				public void onStop(SliderEvent e) {
				}
				
				@Override
				public void onStart(SliderEvent e) {
				}
				
				@Override
				public boolean onSlide(SliderEvent e) {
					return true;
				}
				
				@Override
				public void onChange(SliderEvent e) {
					int selected = submissionSlider.getValue();
					GWT.log("Slider value is " + selected);
					SubmissionReceipt[] submissionReceipts = getSession().get(SubmissionReceipt[].class);
					onSelectSubmission(submissionReceipts[selected]);
				}
			});
			
			// Find the best submission
			int best = 0;
			for (int i = 1; i < result.length; i++) {
				if (result[i].getNumTestsPassed() > result[best].getNumTestsPassed()) {
					best = i;
				}
			}
			GWT.log("Best submission is " + best);
			
			submissionSlider.setValue(best);
		}

		private void onSelectSubmission(SubmissionReceipt receipt) {
			// Load text
			Problem problem = getSession().get(Problem.class);
			UserSelection userSelection = getSession().get(UserSelection.class);
			RPC.editCodeService.getSubmissionText(userSelection.getUser(), problem, receipt, new AsyncCallback<ProblemText>() {
				@Override
				public void onFailure(Throwable caught) {
					getSession().add(StatusMessage.error("Couldn't get text for submission", caught));
				}
				
				@Override
				public void onSuccess(ProblemText result) {
					editor.setText(result.getText());
				}
			});
			
			// Get test results
			RPC.getCoursesAndProblemsService.getTestResultsForSubmission(problem, receipt, new AsyncCallback<NamedTestResult[]>() {
				@Override
				public void onFailure(Throwable caught) {
					getSession().add(StatusMessage.error("Couldn't load test results for submission", caught));
				}
				
				public void onSuccess(NamedTestResult[] result) {
					getSession().add(result);
				}
			});
		}
	}
	
	private UI ui;

	@Override
	public void createWidget() {
		ui = new UI();
	}

	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
	}

	@Override
	public IsWidget getWidget() {
		return ui;
	}

	@Override
	public boolean isActivity() {
		return true;
	}

	@Override
	public PageId getPageId() {
		return PageId.USER_PROBLEM_SUBMISSIONS;
	}

	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
		pageStack.push(PageId.PROBLEM_ADMIN);
		pageStack.push(PageId.STATISTICS);
	}

}
