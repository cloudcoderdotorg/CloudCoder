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

import org.cloudcoder.app.shared.model.Course;

/**
 * Model object indicating a section of a {@link Course}.
 * Used when a page/view allows selection of sections of
 * a course.
 * 
 * @author David Hovemeyer
 */
public class Section {
	private final int number;
	
	/**
	 * Constructor.  Section number is set to 0, which means
	 * "all sections".
	 */
	public Section() {
		this.number = 0;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param number the section number
	 */
	public Section(int number) {
		this.number = number;
	}
	
	/**
	 * @return the section number
	 */
	public int getNumber() {
		return number;
	}
}
