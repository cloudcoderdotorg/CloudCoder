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

import org.cloudcoder.app.client.model.CourseSelection;
import org.cloudcoder.app.client.model.Section;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.ProblemProgressView;
import org.cloudcoder.app.client.view.SectionSelectionView;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page for displaying statistics for a given {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class StatisticsPage extends CloudCoderPage {
	
	private class UI extends Composite implements Subscriber {
		private static final double STATS_OPTION_PANEL_HEIGHT_PX = 28.0;

		private PageNavPanel pageNavPanel;
		private Label problemLabel;
		private StatusMessageView statusMessageView;
		private ProblemProgressView studentProgressView;
		private Button downloadCsvButton;
		private SectionSelectionView sectionSelectionView;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);

			// North panel: problem label, page nav panel, stats options
			LayoutPanel northPanel = new LayoutPanel();
			
			// page nav panel
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			
			// label to display the problem name
			this.problemLabel = new Label("");
			problemLabel.setStyleName("cc-problemName", true);
			northPanel.add(problemLabel);
			northPanel.setWidgetLeftRight(problemLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			northPanel.setWidgetTopHeight(problemLabel, 0.0, Unit.PX, 22.0, Unit.PX);
			
			// stats options (choose section, sorting, download CSV)
			FlowPanel statsOptionPanel = new FlowPanel();
			this.sectionSelectionView = new SectionSelectionView();
			statsOptionPanel.add(sectionSelectionView);
			statsOptionPanel.add(new InlineHTML(" "));
			this.downloadCsvButton = new Button("Download");
			downloadCsvButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleDownloadCsv();
				}
			});
			statsOptionPanel.add(downloadCsvButton);
			
			// Create a hidden iframe element for the CSV download
			InlineHTML iframe = new InlineHTML("<iframe id=\"ccCsvDownload\" style=\"height: 1px; width: 1px; display: none;\"></iframe>");
			statsOptionPanel.add(iframe);
			
			northPanel.add(statsOptionPanel);
			northPanel.setWidgetLeftRight(statsOptionPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			northPanel.setWidgetBottomHeight(statsOptionPanel, 0.0, Unit.PX, STATS_OPTION_PANEL_HEIGHT_PX, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX + STATS_OPTION_PANEL_HEIGHT_PX);
			
			// South panel: just a status message view
			statusMessageView = new StatusMessageView();
			dockLayoutPanel.addSouth(statusMessageView, StatusMessageView.HEIGHT_PX);
			
			// Center panel: stats view
			LayoutPanel centerPanel = new LayoutPanel();
			this.studentProgressView = new ProblemProgressView();
			centerPanel.add(studentProgressView);
			centerPanel.setWidgetLeftRight(studentProgressView, 0.0, Unit.PX, 0.0, Unit.PX);
			centerPanel.setWidgetTopBottom(studentProgressView, 10.0, Unit.PX, 10.0, Unit.PX);
			dockLayoutPanel.add(centerPanel);
			
			initWidget(dockLayoutPanel);
		}

		protected void handleDownloadCsv() {
			// Redirect to the /admin/problems servlet with the chosen
			// course/problem/section

			Problem problem = getSession().get(Problem.class);
			
			// Build the path info string for the request, which specifies
			// course id and problem id, and (optionally) section number
			StringBuilder pathInfo = new StringBuilder();
			pathInfo.append(problem.getCourseId());
			Section sectionChoice = sectionSelectionView.getSelectedSection();
			if (sectionChoice != null && sectionChoice.getNumber() != 0) {
				pathInfo.append("-");
				pathInfo.append(sectionChoice.getNumber());
			}
			pathInfo.append("/");
			pathInfo.append(problem.getProblemId());
			
			// Build the full URL
			String url = GWT.getHostPageBaseURL() + "admin/problems/" + pathInfo.toString();
			
			// Set the src attribute in the hidden download iframe
			GWT.log("Redirecting iframe to " + url);
			doDownloadCsv(url);
		}
		
		private native void doDownloadCsv(String url) /*-{
			var elt = $doc.getElementById("ccCsvDownload");
			elt.src = url;
		}-*/;

		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			// Make sure a Section has been selected (adding one if not)
			Section section = session.get(Section.class);
			if (section != null) {
				session.add(new Section()); // add a Section matching all section
			}
			
			// Activate views
			statusMessageView.activate(session, subscriptionRegistrar);
			studentProgressView.activate(session, subscriptionRegistrar);
			sectionSelectionView.activate(session, subscriptionRegistrar);
			
			// Set title
			Problem problem = session.get(Problem.class);
			CourseSelection courseSelection = session.get(CourseSelection.class);
			problemLabel.setText("Statistics for " + problem.toNiceString() + " in " + courseSelection.getCourse().getName());
			
			// Set back/logout handlers
			pageNavPanel.setBackHandler(new Runnable() {
				@Override
				public void run() {
					// Go back to course admin page
					session.notifySubscribers(Session.Event.COURSE_ADMIN, session.get(Course.class));
				}
			});
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
		}
		
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			// So far, no need to handle events here
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
