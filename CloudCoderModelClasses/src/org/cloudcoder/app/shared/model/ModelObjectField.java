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

/**
 * Descriptor for a field of a model class.  Describes the
 * name, Java type, size (such as the max length for a String field),
 * and whether the field value is a unique object id.
 * This can be used by a persistence layer to map model object
 * properties into a persistent store (such as a database).
 * It also defines {@link #set(Object, Object)} and {@link #get(Object)}
 * methods for getting and setting the field value, which
 * allows reflection-like dynamic getting and setting of field
 * values without reflection. 
 * 
 * @author David Hovemeyer
 * 
 * @param <ModelObjectType> the type of model object containing the field
 * @param <E> the field type
 */
public abstract class ModelObjectField<ModelObjectType, E> {
	/**
	 * Flag to indicate that the field should allow null values.
	 */
	public static final int ALLOW_NULL = (1 << 0);
	
	/**
	 * Flag to indicate that the field should be serialized in a way
	 * that preserves its exact contents.  Significant only for string fields:
	 * indicates that they should be serialized as CDATA in XML.
	 */
	public static final int LITERAL = (1 << 1);
	
	private final String name;
	private final Class<?> type;
	private final int size;
	private final ModelObjectIndexType indexType;
	private final int flags;
	private final String defaultValue;
	
	/**
	 * Constructor for fields which are not a unique object id.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 */
	public ModelObjectField(String name, Class<E> type, int size) {
		this(name, type, size, ModelObjectIndexType.NONE, 0);
	}

	/**
	 * Constructor for fields which might require an index.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 * @param indexType type of index that should be used for this field
	 */
	public ModelObjectField(String name, Class<E> type, int size, ModelObjectIndexType indexType) {
		this(name, type, size, indexType, 0);
	}

	/**
	 * Constructor for fields which might require an index.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 * @param indexType type of index that should be used for this field
	 * @param flags flags to set for this field
	 */
	public ModelObjectField(String name, Class<E> type, int size, ModelObjectIndexType indexType, int flags) {
		this(name, type, size, indexType, flags, null);
	}
	
	/**
	 * Constructor for fields which might require an index and might have
	 * a default value for the database column.
	 * 
	 * @param name the field name (can be used as a database column name)
	 * @param type the Java type of the field
	 * @param size the size (e.g., max string length for a string column)
	 * @param indexType type of index that should be used for this field
	 * @param flags flags to set for this field
	 * @param defaultValue the default value as a string
	 */
	public ModelObjectField(String name, Class<E> type, int size, ModelObjectIndexType indexType, int flags, String defaultValue) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.indexType = indexType;
		this.flags = flags;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * @return the field name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the Java type of the field
	 */
	public Class<?> getType() {
		return type;
	}
	
	/**
	 * @return the size of the field (e.g., max string length for a string field)
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * @return true if the field is a unique object id
	 */
	public boolean isUniqueId() {
		return indexType == ModelObjectIndexType.IDENTITY;
	}
	
	/**
	 * Get the field's index type.
	 * @return the field's index type
	 */
	ModelObjectIndexType getIndexType() {
		return indexType;
	}

	/**
	 * Check whether or not the field is allowed to have NULL values.
	 * @return true if the field is allowed to have NULL values, false otherwise
	 */
	public boolean isAllowNull() {
		return (flags & ALLOW_NULL) != 0;
	}
	
	/**
	 * Check whether or not the field requires serialization in a way that preserves
	 * its exact value (e.g., need to use CDATA to convey string data in XML).
	 * 
	 * @return true if the field must be serialized in a way that preserves its
	 *         exact value
	 */
	public boolean isLiteral() {
		return (flags & LITERAL) != 0;
	}
	
	/**
	 * @return the default value as a string; null if the database column does not have a default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Set the value of the field in given model object.
	 * 
	 * @param obj   the model object
	 * @param value the value to set
	 */
	public abstract void set(ModelObjectType obj, E value);
	
	/**
	 * Get the value of the field in given model object.
	 * 
	 * @param obj  the model object
	 * @return the fields value
	 */
	public abstract E get(ModelObjectType obj);

	/**
	 * Set the field value via a plain Object reference.
	 * Throws IllegalArgumentException if the value's type does not
	 * match the field's type.
	 * 
	 * @param obj    the model object
	 * @param value  the field value to set
	 */
	@SuppressWarnings("unchecked")
	public void setUntyped(ModelObjectType obj, Object value) {
		if (value.getClass() != type) {
			throw new IllegalArgumentException(
					"Value type " + value.getClass().getName() +
					" does not match declared type " + type.getName() +
					" for setting field " + name);
		}
		set(obj, (E) value);
	}
}
