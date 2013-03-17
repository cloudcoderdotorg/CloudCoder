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
import org.cloudcoder.app.client.model.UserSelection;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.UserProgressView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page to show the progress of a {@link User} in a {@link Course}.
 * 
 * @author David Hovemeyer
 */
public class UserProgressPage extends CloudCoderPage {
	
	private class UI extends Composite implements SessionObserver {
		
		private Label topLabel;
		private PageNavPanel pageNavPanel;
		private StatusMessageView statusMessageView;
		private UserProgressView userProgressView;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			LayoutPanel northPanel = new LayoutPanel();
			
			this.topLabel = new Label();
			topLabel.setStyleName("cc-problemName", true);
			northPanel.add(topLabel);
			northPanel.setWidgetTopHeight(topLabel, 0.0, Unit.PX, 22.0, Unit.PX);
			northPanel.setWidgetLeftRight(topLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Unit.PX);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX);
			
			LayoutPanel southPanel = new LayoutPanel();
			
			this.statusMessageView = new StatusMessageView();
			southPanel.add(statusMessageView);
			southPanel.setWidgetTopBottom(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			southPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			
			dockLayoutPanel.addSouth(southPanel, StatusMessageView.HEIGHT_PX);
			
			this.userProgressView = new UserProgressView();
			dockLayoutPanel.add(userProgressView);
			
			initWidget(dockLayoutPanel);
		}
		
		@Override
		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			
			UserSelection selectedUser = session.get(UserSelection.class);
			CourseSelection courseSelection = session.get(CourseSelection.class);
			
			// activate views
			statusMessageView.activate(session, subscriptionRegistrar);
			userProgressView.activate(session, subscriptionRegistrar);
			
			// Display top label (username and course)
			StringBuilder buf = new StringBuilder();
			buf.append("Progress for ");
			buf.append(selectedUser.getUser().getUsername());
			buf.append(" in ");
			buf.append(courseSelection.getCourse().getName());
			topLabel.setText(buf.toString());
			
			// Add back/logout handlers
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			pageNavPanel.setBackHandler(new PageBackHandler(session));
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
		// This could be an activity, but we would need to have a way
		// to keep the SelectedUser in the server-side session.
		return false;
	}

	@Override
	public PageId getPageId() {
		return PageId.USER_PROGRESS;
	}
	
	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
		pageStack.push(PageId.USER_ADMIN);
	}
}
