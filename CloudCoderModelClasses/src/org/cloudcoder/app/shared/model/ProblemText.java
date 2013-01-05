// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * Problem text representing user's work on a problem.
 * 
 * @author David Hovemeyer
 */
public class ProblemText implements Serializable {
	private static final long serialVersionUID = 1L;

	private String text;
	private boolean New;
	private boolean quiz;
	
	public ProblemText() {
		
	}
	
	public ProblemText(String text, boolean New) {
		this.text = text;
		this.New = New;
	}
	
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @param New the new to set
	 */
	public void setNew(boolean New) {
		this.New = New;
	}
	
	/**
	 * @return the new
	 */
	public boolean isNew() {
		return New;
	}
	
	/**
	 * Set whether this problem is a quiz.
	 * 
	 * @param quiz true if the problem is a quiz, false if not
	 */
	public void setQuiz(boolean quiz) {
		this.quiz = quiz;
	}
	
	/**
	 * @return true if the problem is a quiz, false if not
	 */
	public boolean isQuiz() {
		return quiz;
	}
}
