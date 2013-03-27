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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.IFactory;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.User;

public class CreateCourse {
	public static class Instructor {
		Instructor(User user, int section) {
			this.user = user;
			this.section = section;
		}
		User user;
		int section;
	}
	
	public static void main(String[] args) throws Exception {
		Scanner keyboard = new Scanner(System.in);
		
		Class.forName("com.mysql.jdbc.Driver");
		Properties config = DBUtil.getConfigProperties();
		Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
		
		System.out.println("Create a CloudCoder course");

		// Get list of terms
		List<Term> terms = DBUtil.getAllModelObjects(conn, Term.SCHEMA, new IFactory<Term>() {
			@Override
			public Term create() {
				return new Term();
			}
		});
		
		// Get course details
		Term term = ConfigurationUtil.choose(keyboard, "What term?", terms);
		int year = ConfigurationUtil.askInt(keyboard, "What year? ");
		String name = ConfigurationUtil.askString(keyboard, "Course name (e.g., \"CS 101\")? ");
		String title = ConfigurationUtil.askString(keyboard, "Course title (e.g., \"Introduction to Computer Science\")? ");
		String url = ConfigurationUtil.askString(keyboard, "Course URL? ");

		// Get instructor details
		List<Instructor> instructors = new ArrayList<CreateCourse.Instructor>();
		boolean moreInstructors;
		do {
			String username = ConfigurationUtil.askString(keyboard, "Instructor username? ");
			User user = ConfigurationUtil.findUser(conn, username);
			if (user==null) {
			    throw new IllegalArgumentException("Cannot find instructor with username "+username);
			}
			int section = ConfigurationUtil.askInt(keyboard, "What section is this instructor teaching? ");
			
			instructors.add(new Instructor(user, section));
			
			String more = ConfigurationUtil.askString(keyboard, "Add another instructor? (y/n)");
			moreInstructors = more.trim().toLowerCase().startsWith("y");
		} while (moreInstructors);
		
		// Create the course
		Course course = new Course();
		course.setName(name);
		course.setTitle(title);
		course.setUrl(url);
		course.setTermId(term.getId());
		course.setYear(year);
		DBUtil.storeModelObject(conn, course);
		
		// Register the instructor(s)
		for (Instructor instructor : instructors) {
			CourseRegistration reg = new CourseRegistration();
			reg.setCourseId(course.getId());
			reg.setUserId(instructor.user.getId());
			reg.setRegistrationType(CourseRegistrationType.INSTRUCTOR);
			reg.setSection(instructor.section);
			DBUtil.storeModelObject(conn, reg);
		}
		
		System.out.println("Success!");
	}

}
