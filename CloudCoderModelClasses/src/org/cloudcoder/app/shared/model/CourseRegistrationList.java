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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * List of {@link CourseRegistration}s.
 * 
 * @author David Hovemeyer
 */
public class CourseRegistrationList implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<CourseRegistration> list;

	/**
	 * Constructor.
	 */
	public CourseRegistrationList() {
		list = new ArrayList<CourseRegistration>();
	}
	
	/**
	 * @return the list of {@link CourseRegistration}s
	 */
	public List<CourseRegistration> getList() {
		return list;
	}
	
	/**
	 * Return true if any of the {@link CourseRegistration}s are
	 * instructor registrations.
	 * 
	 * @return true if any of the {@link CourseRegistration}s are
	 *         instructor registrations, false otherwise
	 */
	public boolean isInstructor() {
		for (CourseRegistration reg : list) {
			if (reg.getRegistrationType().ordinal() >= CourseRegistrationType.INSTRUCTOR.ordinal()) {
				return true;
			}
		}
		return false;
	}
}
