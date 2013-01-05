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

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.util.Publisher;

/**
 * Object added to {@link Session} when the {@link Problem}
 * that the user is working on is a {@link Quiz}.
 * 
 * @author David Hovemeyer
 */
public class QuizInProgress extends Publisher {
	/** Events. */
	public enum Event {
		/** The object's state changed. */
		STATE_CHANGE,
	}
	
	private boolean ended;
	
	/**
	 * Constructor.
	 */
	public QuizInProgress() {
		
	}
	
	/**
	 * Set whether or not the quiz has ended.
	 * 
	 * @param ended true if the quiz has ended, false otherwise
	 */
	public void setEnded(boolean ended) {
		this.ended = ended;
		notifySubscribers(Event.STATE_CHANGE, ended);
	}
	
	/**
	 * @return true if the quiz has ended
	 */
	public boolean isEnded() {
		return ended;
	}
}
