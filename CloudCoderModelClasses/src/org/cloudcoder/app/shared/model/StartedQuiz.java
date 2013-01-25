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
 * A record indicating that a {@link User} (student) started a
 * {@link Quiz} and may currently be working on one.
 * 
 * @author David Hovemeyer
 */
public class StartedQuiz implements Serializable, IModelObject<StartedQuiz> {
	private static final long serialVersionUID = 1L;
	
	public static final ModelObjectField<StartedQuiz, Integer> ID = new ModelObjectField<StartedQuiz, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(StartedQuiz obj, Integer value) { obj.setId(value); }
		public Integer get(StartedQuiz obj) { return obj.getId(); }
	};

	public static final ModelObjectField<StartedQuiz, Integer> USER_ID = new ModelObjectField<StartedQuiz, Integer>("user_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(StartedQuiz obj, Integer value) { obj.setUserId(value); }
		public Integer get(StartedQuiz obj) { return obj.getUserId(); }
	};

	public static final ModelObjectField<StartedQuiz, Integer> QUIZ_ID = new ModelObjectField<StartedQuiz, Integer>("quiz_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(StartedQuiz obj, Integer value) { obj.setQuizId(value); }
		public Integer get(StartedQuiz obj) { return obj.getQuizId(); }
	};

	public static final ModelObjectField<StartedQuiz, Long> START_TIME = new ModelObjectField<StartedQuiz, Long>("start_time", Long.class, 0) {
		public void set(StartedQuiz obj, Long value) { obj.setStartTime(value); }
		public Long get(StartedQuiz obj) { return obj.getStartTime(); }
	};
	
	public static final ModelObjectSchema<StartedQuiz> SCHEMA_V0 = new ModelObjectSchema<StartedQuiz>("started_quiz")
			.add(ID)
			.add(USER_ID)
			.add(QUIZ_ID)
			.add(START_TIME);
	
	public static final ModelObjectSchema<StartedQuiz> SCHEMA = SCHEMA_V0;

	private int id;
	private int userId;
	private int quizId;
	private long startTime;
	
	public StartedQuiz() {
		
	}
	
	@Override
	public ModelObjectSchema<? super StartedQuiz> getSchema() {
		return SCHEMA;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public void setQuizId(int quizId) {
		this.quizId = quizId;
	}
	
	public int getQuizId() {
		return quizId;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
}
