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
 * RepoProblem represents a problem in the exercise repository.
 * As such, it contains just a unique id, the unique id of the user who
 * posted it, and the information in {@link ProblemData}.
 * 
 * @author David Hovemeyer
 */
public class RepoProblem extends ProblemData implements IModelObject<RepoProblem> {
	private static final long serialVersionUID = 1L;

	private int id;
	private int userId;
	private String hash;
	
	/** {@link ModelObjectField} for repo problem id. */
	public static final ModelObjectField<RepoProblem, Integer> ID =
			new ModelObjectField<RepoProblem, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(RepoProblem obj, Integer value) { obj.setId(value); }
		public Integer get(RepoProblem obj) { return obj.getId(); }
	};
	/** {@link ModelObjectField} for user id. */
	public static final ModelObjectField<RepoProblem, Integer> USER_ID =
			new ModelObjectField<RepoProblem, Integer>("user_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(RepoProblem obj, Integer value) { obj.setUserId(value); }
		public Integer get(RepoProblem obj) { return obj.getUserId(); }
	};
	/** {@link ModelObjectField} for problem/test case hash code. */
	public static final ModelObjectField<RepoProblem, String> HASH =
			new ModelObjectField<RepoProblem, String>("hash", String.class, 40, ModelObjectIndexType.UNIQUE) {
		public void set(RepoProblem obj, String value) { obj.setHash(value); }
		public String get(RepoProblem obj) { return obj.getHash(); }
	};
	
	/**
	 * Description of fields (schema version 0).
	 */
	public static final ModelObjectSchema<RepoProblem> SCHEMA_V0 = new ModelObjectSchema<RepoProblem>("repo_problem")
		.add(ID)
		.add(USER_ID)
		.add(HASH)
		.addAll(ProblemData.SCHEMA_V0.getFieldList());
	
	/**
	 * Description of fields (schema version 1).
	 */
	public static final ModelObjectSchema<RepoProblem> SCHEMA_V1 =
			// Based on Problem schema version 0...
			ModelObjectSchema.basedOn(SCHEMA_V0)
			// With the v1 deltas from ProblemData
			.addDeltasFrom(ProblemData.SCHEMA_V1)
			.finishDelta();
	
	/**
	 * Description of fields (schema version 2).
	 * This version incorporates schema changes from version 2
	 * of {@link ProblemData}'s schema.
	 */
	public static final ModelObjectSchema<RepoProblem> SCHEMA_V2 =
			ModelObjectSchema.basedOn(RepoProblem.SCHEMA_V1)
			.addDeltasFrom(ProblemData.SCHEMA_V2)
			.finishDelta();
	
	/**
	 * Description of fields (current schema version).
	 */
	public static final ModelObjectSchema<RepoProblem> SCHEMA = SCHEMA_V2;
	
	/** Number of fields. */
	public static final int NUM_FIELDS = SCHEMA.getNumFields();
	
	/**
	 * Constructor.
	 */
	public RepoProblem() {
		
	}
	
	@Override
	public ModelObjectSchema<RepoProblem> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Set the unique id.
	 * @param id the unique id
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
	 * Set the user id of the user who published the problem.
	 * @param userId the user id of the user who published the problem
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * @return the user id of the user who published the problem
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Set the SHA1 hash of the problem and its test cases.
	 * @param hash the hash
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	/**
	 * Get the SHA1 hash of the problem and its test cases
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}
}
