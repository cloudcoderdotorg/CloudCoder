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

/**
 * An assigned quiz that allows students in a section of a {@link Course}
 * to work on a {@link Problem} for a limited amount of time.
 * 
 * @author David Hovemeyer
 */
public class Quiz implements Serializable, IModelObject<Quiz> {
	private static final long serialVersionUID = 1L;

	/** {@link ModelObjectField} for unique id. */
	public static final ModelObjectField<Quiz, Integer> ID = new ModelObjectField<Quiz, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(Quiz obj, Integer value) { obj.setId(value); }
		public Integer get(Quiz obj) { return obj.getId(); }
	};

	/** {@link ModelObjectField} for course id. */
	public static final ModelObjectField<Quiz, Integer> COURSE_ID = new ModelObjectField<Quiz, Integer>("course_id", Integer.class, 0) {
		public void set(Quiz obj, Integer value) { obj.setCourseId(value); }
		public Integer get(Quiz obj) { return obj.getCourseId(); }
	};

	/** {@link ModelObjectField} for problem id. */
	public static final ModelObjectField<Quiz, Integer> PROBLEM_ID = new ModelObjectField<Quiz, Integer>("problem_id", Integer.class, 0) {
		public void set(Quiz obj, Integer value) { obj.setProblemId(value); }
		public Integer get(Quiz obj) { return obj.getProblemId(); }
	};

	/** {@link ModelObjectField} for section. */
	public static final ModelObjectField<Quiz, Integer> SECTION = new ModelObjectField<Quiz, Integer>("section", Integer.class, 0) {
		public void set(Quiz obj, Integer value) { obj.setSection(value); }
		public Integer get(Quiz obj) { return obj.getSection(); }
	};

	/** {@link ModelObjectField} for start time. */
	public static final ModelObjectField<Quiz, Long> START_TIME = new ModelObjectField<Quiz, Long>("start_time", Long.class, 0) {
		public void set(Quiz obj, Long value) { obj.setStartTime(value); }
		public Long get(Quiz obj) { return obj.getStartTime(); }
	};

	/** {@link ModelObjectField} for end time. */
	public static final ModelObjectField<Quiz, Long> END_TIME = new ModelObjectField<Quiz, Long>("end_time", Long.class, 0) {
		public void set(Quiz obj, Long value) { obj.setEndTime(value); }
		public Long get(Quiz obj) { return obj.getEndTime(); }
	};
	
	/**
	 * Index for quick lookup by course id, problem id, and section.
	 */
	private static final ModelObjectIndex<Quiz> INDEX = new ModelObjectIndex<Quiz>(ModelObjectIndexType.NON_UNIQUE)
			.addField(COURSE_ID)
			.addField(PROBLEM_ID)
			.addField(SECTION);
	
	/**
	 * Descriptor for model object fields (schema version 0).
	 */
	public static final ModelObjectSchema<Quiz> SCHEMA_V0 = new ModelObjectSchema<Quiz>("quiz")
			.add(ID)
			.add(COURSE_ID)
			.add(PROBLEM_ID)
			.add(SECTION)
			.add(START_TIME)
			.add(END_TIME)
			.addIndex(INDEX);
	
	/**
	 * Descriptor for model object fields (current schema version).
	 */
	public static final ModelObjectSchema<Quiz> SCHEMA = SCHEMA_V0;
	
	private int id;
	private int courseId;
	private int problemId;
	private int section;
	private long startTime;
	private long endTime;
	
	/**
	 * Constructor.
	 */
	public Quiz() {
		
	}
	
	@Override
	public ModelObjectSchema<? super Quiz> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Set unique id.
	 * @param id unique id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set course id.
	 * @param courseId the course id to set
	 */
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	/**
	 * @return the course id
	 */
	public int getCourseId() {
		return courseId;
	}
	
	/**
	 * Set problem id.
	 * @param problemId the problem id to set
	 */
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	/**
	 * @return the problem id
	 */
	public int getProblemId() {
		return problemId;
	}
	
	/**
	 * Set section.
	 * @param section the section to set
	 */
	public void setSection(int section) {
		this.section = section;
	}
	
	/**
	 * @return the section
	 */
	public int getSection() {
		return section;
	}
	
	/**
	 * Set the start time.
	 * @param startTime the start time to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Set the end time.
	 * @param endTime the end time to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * @return the end time
	 */
	public long getEndTime() {
		return endTime;
	}
}
