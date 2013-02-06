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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents the Activity the user
 * is working on.  Contains objects that should be restored to the
 * user's client-side session.
 * 
 * @author David Hovemeyer
 */
public class Activity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	//private ActivityObject[] sessionObjectList;
	
	// GWT serialization cannot deal with supertype references.
	// So, we need a separate field for each ActivityObject subclass
	// that we want to store in the Activity.  Not terribly elegant,
	// but effective.
	private Problem problem;
	private CourseSelection courseSelection;
	private ProblemAndTestCaseList problemAndTestCaseList;
	
	/**
	 * Default constructor.
	 * Needed only for serialization.
	 */
	public Activity() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name the name of the activity
	 */
	public Activity(String name) {
		this.name = name;
	}
	
	/**
	 * @return the name of the activity
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add an {@link ActivityObject} to the activity.
	 * It will be stored server-side, and will be restored to the
	 * user's session if they navigate away from cloudcoder but
	 * then come back.
	 * 
	 * @param obj the ActivityObject to add
	 */
	public void addSessionObject(ActivityObject obj) {
		if (obj instanceof Problem) {
			this.problem = (Problem) obj;
		} else if (obj instanceof CourseSelection) {
			this.courseSelection = (CourseSelection) obj;
		} else if (obj instanceof ProblemAndTestCaseList) {
			this.problemAndTestCaseList = (ProblemAndTestCaseList) obj;
		} else {
			throw new IllegalArgumentException("Unknown ActivityObject subclass: " + obj.getClass().getName());
		}
	}

	/**
	 * Get all objects that should be added to the client-side session
	 * when the activity is resumed.
	 * 
	 * @return list of ActivityObjects to be restored to the client-side session
	 */
	public List<ActivityObject> getSessionObjects() {
		ArrayList<ActivityObject> result = new ArrayList<ActivityObject>();
		
		if (problem != null) {
			result.add(problem);
		}
		if (courseSelection != null) {
			result.add(courseSelection);
		}
		if (problemAndTestCaseList != null) {
			result.add(problemAndTestCaseList);
		}
		
		return result;
	}
}
