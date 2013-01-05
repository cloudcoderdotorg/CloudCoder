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
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Page allowing an instructor to administer a quiz for a {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class QuizPage extends CloudCoderPage {
	
	private class UI extends Composite {
		private ListBox sectionListBox;
		private StatusMessageView statusMessageView;
		private Button startQuizButton;
		private Button endQuizButton;
		private Label timeLabel;
		private int duration;
		private Timer timer;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			LayoutPanel northPanel = new LayoutPanel();
			PageNavPanel navPanel = new PageNavPanel();
			navPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					GWT.log("Going back to course admin page");
					getSession().notifySubscribers(Session.Event.COURSE_ADMIN, null);
				}
			});
			navPanel.setLogoutHandler(new LogoutHandler(getSession()));
			northPanel.add(navPanel);
			northPanel.setWidgetTopHeight(navPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			northPanel.setWidgetRightWidth(navPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			
			Label pageLabel = new Label("Quiz: " + getSession().get(Problem.class).toNiceString());
			pageLabel.setStyleName("cc-pageTitle", true);
			northPanel.add(pageLabel);
			northPanel.setWidgetTopHeight(pageLabel, 0.0, Unit.PX, 28, Unit.PX);
			northPanel.setWidgetLeftRight(pageLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX);
			
			LayoutPanel southPanel = new LayoutPanel();
			this.statusMessageView = new StatusMessageView();
			southPanel.add(statusMessageView);
			southPanel.setWidgetTopBottom(statusMessageView, 0, Unit.PX, 0, Unit.PX);
			southPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			dockLayoutPanel.addSouth(southPanel, StatusMessageView.HEIGHT_PX);
			
			LayoutPanel centerPanel = new LayoutPanel();

			Label sectionLabel = new Label("Choose section:");
			centerPanel.add(sectionLabel);
			centerPanel.setWidgetLeftWidth(sectionLabel, 40, Unit.PX, 180, Unit.PX);
			centerPanel.setWidgetTopHeight(sectionLabel, 20, Unit.PX, 20, Unit.PX);
			this.sectionListBox = new ListBox(false);
			centerPanel.add(sectionListBox);
			centerPanel.setWidgetLeftWidth(sectionListBox, 40, Unit.PX, 140, Unit.PX);
			centerPanel.setWidgetTopHeight(sectionListBox, 44, Unit.PX, 28, Unit.PX);
			this.startQuizButton = new Button("Start Quiz");
			centerPanel.add(startQuizButton);
			centerPanel.setWidgetLeftWidth(startQuizButton, 40, Unit.PX, 140, Unit.PX);
			centerPanel.setWidgetTopHeight(startQuizButton, 96, Unit.PX, 32, Unit.PX);
			startQuizButton.setEnabled(false);
			startQuizButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleStartQuiz();
				}
			});
			this.endQuizButton = new Button("End Quiz");
			centerPanel.add(endQuizButton);
			centerPanel.setWidgetLeftWidth(endQuizButton, 40, Unit.PX, 140, Unit.PX);
			centerPanel.setWidgetTopHeight(endQuizButton, 156, Unit.PX, 32, Unit.PX);
			endQuizButton.setEnabled(false);
			endQuizButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleEndQuiz();
				}
			});
			this.timeLabel = new Label("");
			timeLabel.setStyleName("cc-quizTimer", true);
			centerPanel.add(timeLabel);
			centerPanel.setWidgetTopHeight(timeLabel, 96, Unit.PX, 100, Unit.PX);
			centerPanel.setWidgetLeftWidth(timeLabel, 200, Unit.PX, 450, Unit.PX);
			
			dockLayoutPanel.add(centerPanel);
			
			initWidget(dockLayoutPanel);
		}

		protected void handleStartQuiz() {
			int index = sectionListBox.getSelectedIndex();
			if (index < 0) {
				getSession().add(StatusMessage.error("Please select a section"));
			}
			int section = Integer.parseInt(sectionListBox.getItemText(index));
			
			Problem problem = getSession().get(Problem.class);
			
			RPC.getCoursesAndProblemsService.startQuiz(problem, section, new AsyncCallback<Quiz>() {
				@Override
				public void onFailure(Throwable caught) {
					getSession().add(StatusMessage.error("Could not start quiz", caught));
				}

				@Override
				public void onSuccess(Quiz result) {
					getSession().add(result);
					getSession().add(StatusMessage.goodNews("Quiz started"));
					duration = 0;
					doStartQuiz();
				}
			});
		}
		
		private void handleEndQuiz() {
			GWT.log("End quiz...");
			
			Quiz quiz = getSession().get(Quiz.class);
			
			RPC.getCoursesAndProblemsService.endQuiz(quiz, new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					getSession().add(StatusMessage.error("Error trying to end quiz", caught));
				}
				
				@Override
				public void onSuccess(Boolean result) {
					if (result) {
						getSession().add(StatusMessage.goodNews("Quiz ended successfully"));
						endQuizButton.setEnabled(false);
						timer.cancel();
					} else {
						getSession().add(StatusMessage.error("Quiz could not be ended (doesn't exist?"));
					}
				}
			});
		}

		public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar) {
			statusMessageView.activate(session, subscriptionRegistrar);
			
			final Problem problem = session.get(Problem.class);

			// Find out if there is a quiz active already
			RPC.getCoursesAndProblemsService.findCurrentQuiz(problem, new AsyncCallback<Quiz>() {
				@Override
				public void onFailure(Throwable caught) {
					getSession().add(StatusMessage.error("Error checking for current quiz", caught));
				}
				
				@Override
				public void onSuccess(Quiz result) {
					if (result == null) {
						// No current quiz, so load course registrations
						// to see which section(s) the instructor is authorized
						// to administer a quiz in
						loadCourseRegistrations();
					} else {
						// Resume current quiz
						getSession().add(result);
						sectionListBox.addItem(String.valueOf(result.getSection()));
						duration = (int) ((result.getEndTime() - result.getStartTime()) / 1000);
						doStartQuiz();
					}
				}
			});
		}

		// Load user's course registrations so we know which sections he/she
		// is an instructor for.
		private void loadCourseRegistrations() {
			final Problem problem = getSession().get(Problem.class);
			
			RPC.getCoursesAndProblemsService.getCourseAndCourseRegistrations(new AsyncCallback<CourseAndCourseRegistration[]>() {
				@Override
				public void onSuccess(CourseAndCourseRegistration[] result) {
					int numSections = 0;
					for (CourseAndCourseRegistration ccr : result) {
						if (ccr.getCourse().getId() == problem.getCourseId()
								&& ccr.getCourseRegistration().getRegistrationType().isInstructor()) {
							Integer section = (Integer)ccr.getCourseRegistration().getSection();
							sectionListBox.addItem(section.toString());
							numSections++;
						}
					}
					if (numSections > 0) {
						startQuizButton.setEnabled(true);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
		
		private String formatDuration(int seconds) {
			int s = seconds % 60;
			seconds /= 60;
			int m = seconds % 60;
			seconds /= 60;
			int h = seconds;
			
			StringBuilder buf = new StringBuilder();
			if (h > 0) {
				buf.append(String.valueOf(h));
				buf.append(":");
			}
			buf.append(toS(m));
			buf.append(":");
			buf.append(toS(s));
			
			return buf.toString();
		}
		
		private String toS(int t) {
			String s = String.valueOf(t);
			return t < 10 ? "0" + s : s;
		}

		private void doStartQuiz() {
			sectionListBox.setEnabled(false);
			startQuizButton.setEnabled(false);
			endQuizButton.setEnabled(true);
			timeLabel.setText(formatDuration(duration));
			
			timer = new Timer() {
				@Override
				public void run() {
					duration++;
					timeLabel.setText(formatDuration(duration));
				}
			};
			timer.scheduleRepeating(1000);
		}

		public void deactivate() {
			if (timer != null) {
				timer.cancel();
			}
		}
	}

	private UI ui;

	@Override
	public void createWidget() {
		this.ui = new UI();
	}

	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
		getSubscriptionRegistrar().cancelAllSubscriptions();
		ui.deactivate();
	}

	@Override
	public IsWidget getWidget() {
		return ui;
	}

	@Override
	public boolean isActivity() {
		return true;
	}

}
