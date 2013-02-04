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

package org.cloudcoder.app.client;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.client.page.ProblemAdminPage;
import org.cloudcoder.app.client.page.CoursesAndProblemsPage2;
import org.cloudcoder.app.client.page.DevelopmentPage;
import org.cloudcoder.app.client.page.EditProblemPage;
import org.cloudcoder.app.client.page.InitErrorPage;
import org.cloudcoder.app.client.page.LoginPage;
import org.cloudcoder.app.client.page.QuizPage;
import org.cloudcoder.app.client.page.StatisticsPage;
import org.cloudcoder.app.client.page.UserAccountPage;
import org.cloudcoder.app.client.page.UserAdminPage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Activity;
import org.cloudcoder.app.shared.model.ActivityObject;
import org.cloudcoder.app.shared.model.InitErrorException;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.DefaultSubscriptionRegistrar;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * CloudCoder entry point class.
 */
public class CloudCoder implements EntryPoint, Subscriber {
	private Session session;
	private SubscriptionRegistrar subscriptionRegistrar;
	private CloudCoderPage currentPage;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		session = new Session();
		subscriptionRegistrar = new DefaultSubscriptionRegistrar();

		// Subscribe to all Session events
		session.subscribeToAll(Session.Event.values(), this, subscriptionRegistrar);

		// Go to whatever initial page is appropriate.
		createInitialPage();
	}

	private void createInitialPage() {
		// Check to see if the user is already logged in, and if so,
		// if there is an Activity set.  A bit complicated because it
		// involves two RPC calls that are chained.
		RPC.loginService.getUser(new AsyncCallback<User>() {
			@Override
			public void onFailure(Throwable caught) {
				// Special case: if this RPC call (which is the first one)
				// throws an InitErrorException, switch to the InitErrorPage
				// so that the cloudcoder admin can diagnose and resolve
				// the issue.
				if (caught instanceof InitErrorException) {
					changePage(new InitErrorPage());
				} else {
					session.add(StatusMessage.error("Could not check for current login status: " + caught.getMessage()));
					changePage(new LoginPage());
				}
			}

			@Override
			public void onSuccess(User result) {
				if (result == null) {
					// Not logged in, so show LoginPage
					changePage(new LoginPage());
				} else {
					final User user = result;
					
					// User is logged in: get Activity
					RPC.loginService.getActivity(new AsyncCallback<Activity>() {
						@Override
						public void onFailure(Throwable caught) {
							session.add(StatusMessage.error("Could not check for current login status: " + caught.getMessage()));
							changePage(new LoginPage());
						}

						@Override
						public void onSuccess(Activity result) {
							// Add user to session
							session.add(user);
							
							// Did we find the user's Activity?
							if (result == null) {
								// Don't know what the user's activity was, so take
								// them to the courses/problems page
								changePage(new CoursesAndProblemsPage2());
							} else {
								// We have an activity.  Find the page.
								CloudCoderPage page = getPageForActivity(result);
								
								// Restore the session objects.
								for (Object obj : result.getSessionObjects()) {
									GWT.log("Restoring activity object: " + obj.getClass().getName());
									session.add(obj);
								}
								
								changePage(page);
							}
						}
					});
				}
			}
		});
	}

	protected CloudCoderPage getPageForActivity(Activity result) {
		String name = result.getName();
		if (name.equals(CoursesAndProblemsPage2.class.getName())) {
			return new CoursesAndProblemsPage2();
		} else if (name.equals(DevelopmentPage.class.getName())) {
			return new DevelopmentPage();
		} else if (name.equals(ProblemAdminPage.class.getName())) {
			return new ProblemAdminPage();
		} else if (name.equals(EditProblemPage.class.getName())) {
			return new EditProblemPage();
		} else if (name.equals(QuizPage.class.getName())) {
			return new QuizPage();
		} else if (name.equals(StatisticsPage.class.getName())) {
			return new StatisticsPage();
		}
		
		// This shouldn't happen (can't find page for Activity),
		// but if it does, go to the courses and problems page.
		GWT.log("Can't find activity " + result.getName());
		return new CoursesAndProblemsPage2();
	}
	
	protected Activity getActivityForPage(CloudCoderPage page) {
		return getActivityForSessionAndPage(page, session);
	}

	/**
	 * Create an {@link Activity} for current page and session.
	 *  
	 * @param page     current page
	 * @param session  current session
	 * @return the {@link Activity}
	 */
	public static Activity getActivityForSessionAndPage(CloudCoderPage page, Session session) {
		Activity activity = new Activity(page.getClass().getName());
		
		// Record the Session objects (the ones that are ActivityObjects)
		for (Object obj : session.getObjects()) {
			if (obj instanceof ActivityObject) {
				GWT.log("Adding " + obj.getClass().getName() + " to Activity");
				activity.addSessionObject((ActivityObject) obj);
			}
		}
		
		return activity;
	}
	
	private void changePage(CloudCoderPage page) {
		if (currentPage != null) {
			currentPage.deactivate();
			RootLayoutPanel.get().remove(currentPage.getWidget());
			
			// make sure there is no StatusMessage from the previous page
			session.remove(StatusMessage.class);
		}
		page.setSession(session);

		// Create the page's Widget and add it to the DOM tree.
		// Leave a 10 pixel border around the page widget.
		page.createWidget();
		IsWidget w = page.getWidget();
		RootLayoutPanel.get().add(w);
		RootLayoutPanel.get().setWidgetLeftRight(w, 10.0, Unit.PX, 10.0, Unit.PX);
		RootLayoutPanel.get().setWidgetTopBottom(w, 10.0, Unit.PX, 10.0, Unit.PX);

		// Now it is safe to activate the page
		page.activate();
		currentPage = page;
		
		// Inform the server of the Activity (page) that the user is now working on,
		// if the page requests it.  Otherwise set the activity to null.
		Activity activity = page.isActivity() ? getActivityForPage(page) : null;
		RPC.loginService.setActivity(activity, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				// There's not really anything useful we can do here.
				GWT.log("Couldn't set activity on server?", caught);
			}

			@Override
			public void onSuccess(Void result) {
				// Nothing to do
			}
		});
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		// This is where we monitor for events that indicate page changes.
		if (key == Session.Event.LOGIN || key == Session.Event.BACK_HOME) {
			changePage(new CoursesAndProblemsPage2());
		} else if (key == Session.Event.PROBLEM_CHOSEN) {
			changePage(new DevelopmentPage());
		} else if (key == Session.Event.COURSE_ADMIN) {
		    changePage(new ProblemAdminPage());
		} else if (key == Session.Event.USER_ADMIN) {
		    changePage(new UserAdminPage());
		} else if (key == Session.Event.LOGOUT) {
			changePage(new LoginPage());
		} else if (key == Session.Event.EDIT_PROBLEM) {
			changePage(new EditProblemPage());
		} else if (key == Session.Event.USER_ACCOUNT) {
		    changePage(new UserAccountPage());
		} else if (key == Session.Event.START_QUIZ) {
			changePage(new QuizPage());
		} else if (key == Session.Event.STATISTICS) {
			changePage(new StatisticsPage());
		}
	}
}
