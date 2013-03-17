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

package org.cloudcoder.app.client.model;

import org.cloudcoder.app.client.page.CloudCoderPage;
import org.cloudcoder.app.shared.model.Activity;

/**
 * Abstract identifier for a {@link CloudCoderPage}.
 * Used for things like opaque page identifier to place in
 * an {@link Activity}. 
 * 
 * @author David Hovemeyer
 */
public enum PageId {
	/** CloudCoder initialization error. */
	INIT_ERROR,
	
	/** Login page. */
	LOGIN,

	/** Default home page showing courses and problems. */
	COURSES_AND_PROBLEMS,
	
	/** The development page (edit code, submit, etc.) */
	DEVELOPMENT,
	
	/** The problem admin page. */
	PROBLEM_ADMIN,
	
	/** The edit problem page. */
	EDIT_PROBLEM,
	
	/** The user admin page. */
	USER_ADMIN,
	
	/** User account page. */
	USER_ACCOUNT,
	
	/** Statistics (all users progress) page. */
	STATISTICS,
	
	/** The (individual) user progress page. */
	USER_PROGRESS,
	
	/** Quiz administration page. */
	QUIZ,
}
