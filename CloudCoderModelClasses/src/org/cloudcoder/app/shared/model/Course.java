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

/**
 * Model object representing a course.
 * 
 * @author David Hovemeyer
 */
public class Course implements ActivityObject, IModelObject<Course> {
	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private String title;
	private String url;
	private int termId;
	private int year;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema<Course> SCHEMA = new ModelObjectSchema<Course>("course")
		.add(new ModelObjectField<Course, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
			public void set(Course obj, Integer value) { obj.setId(value); }
			public Integer get(Course obj) { return obj.getId(); }
		})
		.add(new ModelObjectField<Course, String>("name", String.class, 20) {
			public void set(Course obj, String value) { obj.setName(value); }
			public String get(Course obj) { return obj.getName(); }
		})
		.add(new ModelObjectField<Course, String>("title", String.class, 100) {
			public void set(Course obj, String value) { obj.setTitle(value); }
			public String get(Course obj) { return obj.getTitle(); }
		})
		.add(new ModelObjectField<Course, String>("url", String.class, 120) {
			public void set(Course obj, String value) { obj.setUrl(value); }
			public String get(Course obj) { return obj.getUrl(); }
		})
		.add(new ModelObjectField<Course, Integer>("term_id", Integer.class, 0) {
			public void set(Course obj, Integer value) { obj.setTermId(value); }
			public Integer get(Course obj) { return obj.getTermId(); }
		})
		.add(new ModelObjectField<Course, Integer>("year", Integer.class, 0) {
			public void set(Course obj, Integer value) { obj.setYear(value); }
			public Integer get(Course obj) { return obj.getYear(); }
		});

	/**
	 * Number of fields in the database.
	 */
	public static final int NUM_FIELDS = SCHEMA.getNumFields();
	
	// Note: this field is not stored in the database directly,
	// but is set by GetCoursesAndProblemsServiceImpl when a user's courses
	// are returned.
	private Term term;
	
	/**
	 * Constructor.
	 */
	public Course() {
		
	}
	
	@Override
	public ModelObjectSchema<Course> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Set the course id.
	 * @param id the course id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get the course id.
	 * @return the course id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the course name, e.g. "CS 101".
	 * @param name the course name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the course name.
	 * @return the course name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the course title, e.g. "Introduction to Computer Science".
	 * @param title the course title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Get the course title.
	 * @return the course title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Get the course URL.
	 * @return the course URL
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Set the course URL
	 * @param url the course URL
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Set the term id (the unique id of the term in which the course is offered.)
	 * @param termId the term id
	 */
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	/**
	 * Get the term id.
	 * @return the term id
	 */
	public int getTermId() {
		return termId;
	}
	
	/**
	 * Set the year the course is offered.
	 * @param year the year the course is offered
	 */
	public void setYear(int year) {
		this.year = year;
	}
	
	/**
	 * Get the year the course is offered.
	 * @return the year the course is offered
	 */
	public int getYear() {
		return year;
	}
	
	/**
	 * Set the {@link Term}.  Its id should be the same as
	 * the term id.
	 * @param term the Term
	 */
	public void setTerm(Term term) {
		this.term = term;
	}
	
	/**
	 * Get the {@link Term} set previously with setTerm.
	 * @return the Term
	 */
	public Term getTerm() {
		return term;
	}
	
	@Override
	public String toString() {
		return name + " - " + title;
	}

	/**
	 * Get a {@link TermAndYear} object with this course's Term and year.
	 * @return the TermAndYear object
	 */
	public TermAndYear getTermAndYear() {
		return new TermAndYear(term, year);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		Course other = (Course) obj;
		return this.id == other.id
				&& this.name.equals(other.name)
				&& this.title.equals(other.title)
				&& this.url.equals(other.url)
				&& this.termId == other.termId
				&& this.year == other.year;
	}
	
	@Override
	public int hashCode() {
		int code = 0;
		code += id;
		code *= 37;
		code += title.hashCode();
		code *= 37;
		code += url.hashCode();
		code *= 37;
		code += termId;
		code *= 37;
		code += year;
		return code;
	}
}
