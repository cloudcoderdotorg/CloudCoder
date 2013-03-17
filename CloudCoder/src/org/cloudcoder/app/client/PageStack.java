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

package org.cloudcoder.app.client;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.shared.util.Publisher;

/**
 * Stack of {@link PageId}s representing the user's navigation
 * history.  A PAGE_CHANGE event is published when a page id is
 * pushed or popped (unless notifications are disabled).
 * 
 * @author David Hovemeyer
 */
public class PageStack extends Publisher {
	/** Events published by the page stack. */
	public enum Event {
		PAGE_CHANGE,
	}
	
	private List<PageId> stack;
	private boolean notifications;
	
	/**
	 * Constructor.
	 */
	public PageStack() {
		this.stack = new ArrayList<PageId>();
		this.notifications = true;
	}
	
	/**
	 * Push a new page.
	 * 
	 * @param id the {@link PageId} of the page to push
	 */
	public void push(PageId id) {
		stack.add(id);
		if (notifications) {
			notifySubscribers(Event.PAGE_CHANGE, id);
		}
	}
	
	/**
	 * Pop a page.
	 * 
	 * @return the {@link PageId} of the popped page
	 */
	public PageId pop() {
		int last = stack.size() - 1;
		PageId id = stack.remove(last);
		if (notifications) {
			notifySubscribers(Event.PAGE_CHANGE, id);
		}
		return id;
	}
	
	/**
	 * @return  the {@link PageId} on top of the stack
	 */
	public PageId getTop() {
		return stack.get(stack.size() - 1);
	}
	
	/**
	 * @return true if the page stack is empty, false otherwise
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * Clear the page stack.
	 */
	public void clear() {
		stack.clear();
	}

	/**
	 * Enable or disable notifications to subscribers.
	 * disabling notifications is useful when reconstructing
	 * navigation history: for example, when restoring the
	 * current activity.
	 * 
	 * @param b true to enable notifications, false to disable
	 */
	public void setNotifications(boolean b) {
		this.notifications = b;
	}
}
