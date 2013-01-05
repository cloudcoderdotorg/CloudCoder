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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.rpc.RPC;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Page logout handler.
 * 
 * @author David Hovemeyer
 */
public class LogoutHandler implements Runnable {
	private Session session;
	
	public LogoutHandler(Session session) {
		this.session = session;
	}

	@Override
	public void run() {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Could not log out?", caught);

				// well, at least we tried
				clearSessionData();
			}

			@Override
			public void onSuccess(Void result) {
				// server has purged the session
				clearSessionData();
			}

			protected void clearSessionData() {
				// Clear all session objects
				session.clear();

				// Publish the LOGOUT event.
				session.notifySubscribers(Session.Event.LOGOUT, null);
			}
		};

		RPC.loginService.logout(callback);
	}
}
