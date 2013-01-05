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

package org.cloudcoder.app.server.rpc;

import org.cloudcoder.app.shared.model.Activity;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;

/**
 * Keys for session attributes.
 * 
 * @author David Hovemeyer
 */
public interface SessionAttributeKeys {
	/**
	 * Key to get the {@link User} object of the current
	 * authenticated user.
	 */
	public static final String USER_KEY = "user";
	
	/**
	 * Key to get the most-recently-set {@link Activity} object.
	 */
	public static final String ACTIVITY_KEY = "activity";
	
	/**
	 * Key to get the current {@link Problem} object.
	 */
	public static final String PROBLEM_KEY = "problem";

	public static final String FUTURE_SUBMISSION_RESULT_KEY = "future";

	public static final String FULL_TEXT_CHANGE_KEY = "fullText";

	/**
	 * Key to get the current {@link Quiz}, if any.
	 */
	public static final String QUIZ_KEY = "quiz";

}
