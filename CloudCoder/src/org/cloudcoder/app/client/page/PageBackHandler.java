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
import org.cloudcoder.app.client.model.Session;

/**
 * Handler for navigating to previous page.
 * 
 * @author David Hovemeyer
 */
public class PageBackHandler implements Runnable {

	private Session session;
	
	public PageBackHandler(Session session) {
		this.session = session;
	}
	
	@Override
	public void run() {
		// Just pop the current page from the PageStack
		session.get(PageStack.class).pop();
	}

}
