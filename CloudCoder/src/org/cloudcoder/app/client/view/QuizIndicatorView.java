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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.client.QuizInProgress;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * View to indicate whether the problem the user is currently
 * working on is a quiz.  A pencil icon is displayed when a quiz
 * is in progress, and it is grayed out when the quiz has ended.
 * 
 * @author David Hovemeyer
 */
public class QuizIndicatorView extends Composite implements Subscriber {
	public static final double HEIGHT_PX = StatusMessageView.HEIGHT_PX;
	public static final double WIDTH_PX = 22;
	
	public static final String QUIZ_ICON_URL = "cloudcoder/images/pencil-sm.png";
	public static final String ENDED_QUIZ_ICON_URL = "cloudcoder/images/pencil-sm-gray.png";
	
	private Image image;
	private Session session;
	private SubscriptionRegistrar subscriptionRegistrar;
	
	/**
	 * Constructor.
	 */
	public QuizIndicatorView() {
		LayoutPanel panel = new LayoutPanel();
		
		this.image = new Image();
		panel.add(image);
		panel.setWidgetLeftWidth(image, 1, Unit.PX, WIDTH_PX, Unit.PX);
		panel.setWidgetTopHeight(image, 2, Unit.PX, HEIGHT_PX, Unit.PX);
		
		initWidget(panel);
	}
	
	/**
	 * Activate the view.
	 * 
	 * @param session                the {@link Session}
	 * @param subscriptionRegistrar  the {@link SubscriptionRegistrar} which which this
	 *                               view should register its subscriptions
	 */
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		this.session = session;
		this.subscriptionRegistrar = subscriptionRegistrar;
		
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		QuizInProgress quizInProgress = session.get(QuizInProgress.class);
		if (quizInProgress != null) {
			attach(quizInProgress);
		}
	}
	
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof QuizInProgress) {
			attach((QuizInProgress) hint);
		} else if (key == QuizInProgress.Event.STATE_CHANGE) {
			updateImage(session.get(QuizInProgress.class));
		}
	}

	private void attach(QuizInProgress quizInProgress) {
		quizInProgress.subscribe(QuizInProgress.Event.STATE_CHANGE, this, this.subscriptionRegistrar);
		updateImage(quizInProgress);
	}

	private void updateImage(QuizInProgress quizInProgress) {
		image.setUrl(quizInProgress.isEnded() ? ENDED_QUIZ_ICON_URL : QUIZ_ICON_URL);
	}
}
