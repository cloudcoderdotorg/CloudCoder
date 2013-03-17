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

import java.util.Arrays;

import org.cloudcoder.app.client.PageStack;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page to display server-side initialization errors.
 * This allows the cloudcoder admin to diagnose and resolve fatal init errors.
 * 
 * @author David Hovemeyer
 */
public class InitErrorPage extends CloudCoderPage {
	private class UI extends Composite {
		private CellList<String> errorList;
		private StatusMessageView statusMessageView;

		public UI() {
			LayoutPanel panel = new LayoutPanel();
			
			HTML label = new HTML("<span style='font-size: 32px; font-weight: bold;'>CloudCoder Failed to Initialize Correctly</span>");
			panel.add(label);
			panel.setWidgetLeftRight(label, 0.0, Unit.PX, 00., Unit.PX);
			panel.setWidgetTopHeight(label, 0.0, Unit.PX, 48.0, Unit.PX);

			TextCell cell = new TextCell();
			this.errorList = new CellList<String>(cell);
			panel.add(errorList);
			panel.setWidgetLeftRight(errorList, 40.0, Unit.PX, 40.0, Unit.PX);
			panel.setWidgetTopBottom(errorList, 48.0 + 40.0, Unit.PX, 40.0 + StatusMessageView.HEIGHT_PX, Unit.PX);
			
			this.statusMessageView = new StatusMessageView();
			panel.add(statusMessageView);
			panel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
			panel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
			
			initWidget(panel);
		}

		public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
			statusMessageView.activate(session, subscriptionRegistrar);
			
			RPC.loginService.getInitErrorList(new AsyncCallback<String[]>() {
				@Override
				public void onSuccess(String[] result) {
					onLoadInitErrorList(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					session.add(StatusMessage.error(
							"Could not load init error list (check webapp log): " +
							caught.getMessage()));
				}
			});
		}

		protected void onLoadInitErrorList(String[] result) {
			errorList.setRowCount(result.length);
			errorList.setRowData(0, Arrays.asList(result));
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
		return false;
	}
	
	@Override
	public PageId getPageId() {
		return PageId.INIT_ERROR;
	}

	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		throw new IllegalStateException("Not an activity");
	}
}
