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

/**
 * A module is a category containing related {@link Problem}s
 * in a {@link Course}. 
 * 
 * @author David Hovemeyer
 */
public class Module implements IModelObject<Module>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String name;
	
	public static ModelObjectField<Module, Integer> ID = new ModelObjectField<Module, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(Module obj, Integer value) { obj.setId(value); }
		public Integer get(Module obj) { return obj.getId(); }
	};
	public static ModelObjectField<Module, String> NAME = new ModelObjectField<Module, String>("name", String.class, 60, ModelObjectIndexType.UNIQUE) {
		public void set(Module obj, String value) { obj.setName(value); }
		public String get(Module obj) { return obj.getName(); }
	};
	
	/**
	 * The default "Uncategorized" module, which has id=1.
	 */
	public static final Module DEFAULT_MODULE = new Module();
	static {
		DEFAULT_MODULE.setId(1);
		DEFAULT_MODULE.setName("Uncategorized");
	}
	
	/**
	 * Model object schema (version 0).
	 */
	public static ModelObjectSchema<Module> SCHEMA_V0 = new ModelObjectSchema<Module>("module")
			.add(ID)
			.add(NAME)
			.addPersistedModelObject(DEFAULT_MODULE);
	
	/**
	 * Model object schema (current version).
	 */
	public static ModelObjectSchema<Module> SCHEMA = SCHEMA_V0;
	
	/**
	 * Constructor.
	 */
	public Module() {
		
	}
	
	@Override
	public ModelObjectSchema<? super Module> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Set the unique id.
	 * @param id the unique id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the module's unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the module's name.
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the module's name
	 */
	public String getName() {
		return name;
	}
}
