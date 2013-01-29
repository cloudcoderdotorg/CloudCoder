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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.IFactory;
import org.cloudcoder.app.shared.model.IModelObject;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectIndex;
import org.cloudcoder.app.shared.model.ModelObjectIndexType;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.ModelObjectSchema.Delta;
import org.cloudcoder.app.shared.model.ModelObjectSchema.PersistModelObjectDelta;
import org.cloudcoder.app.shared.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC utility methods.
 * 
 * @author David Hovemeyer
 */
public class DBUtil {
    private static final Logger logger=LoggerFactory.getLogger(DBUtil.class);
    
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Connection getConnection() throws IOException, SQLException 
    {
        Properties config = getConfigProperties();
        return DBUtil.connectToDatabase(config, "cloudcoder.db");
    }
    
    /**
     * Quietly close a {@link Statement}.
     * 
     * @param stmt the Statement to close
     */
	public static void closeQuietly(Statement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
		    logger.error("Unable to close prepared statement",e);
		}
	}
	
	public static void closeQuietly(Connection conn) {
	    try {
	        if (conn!=null) {
	            conn.close();
	        }
	    } catch (SQLException e) {
	        logger.error("Unable to close database connection",e);
	    }
	}

	/**
	 * Quietly close a {@link ResultSet}.
	 * 
	 * @param resultSet the ResultSet to close
	 */
	public static void closeQuietly(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException e) {
		    logger.error("Unable to close result set",e);
		}
	}
	
	/**
	 * Get placeholders for an insert statement.
	 * 
	 * @param schema the schema for the object being inserted
	 * @return placeholders for an insert statement
	 */
	public static String getInsertPlaceholders(ModelObjectSchema<?> schema) {
		StringBuilder buf = new StringBuilder();
		
		for (int i = 0; i < schema.getNumFields(); i++) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append("?");
		}
		
		return buf.toString();
	}

	/**
	 * Get insert placeholders for all fields except the unique id.
	 * For the unique id field, a literal NULL value will be specified.
	 * This is useful for generating an insert statement where the unique
	 * id is an autoincrement column.
	 * 
	 * @param schema the schema of a model object
	 * @return insert placeholders for all fields except the unique id
	 */
	public static<E> String getInsertPlaceholdersNoId(ModelObjectSchema<E> schema) {
		StringBuilder buf = new StringBuilder();
		
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append(field.isUniqueId() ? "NULL" : "?");
		}
		
		return buf.toString();
	}
	
	
	
	/**
	 * Get placeholders for an update statement where all fields
	 * will be updated.
	 * 
	 * @return placeholders for an update statement where all fields will be updated
	 */
	public static<E> String getUpdatePlaceholders(ModelObjectSchema<E> schema) {
		return doGetUpdatePlaceholders(schema, true);
	}
	
	/**
	 * Get placeholders for an update statement where all fields except for
	 * the unique id field will be updated.
	 * 
	 * @return placeholders for an update statement where all fields except the
	 *         unique id field will be update
	 */
	public static<E> String getUpdatePlaceholdersNoId(ModelObjectSchema<E> schema) {
		return doGetUpdatePlaceholders(schema, false);
	}

	private static<E> String doGetUpdatePlaceholders(ModelObjectSchema<E> schema, boolean includeUniqueId) {
		StringBuilder buf = new StringBuilder();
		
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			if (!field.isUniqueId() || includeUniqueId) {
				if (buf.length() > 0) {
					buf.append(", ");
				}
				buf.append(field.getName());
				buf.append(" = ?");
			}
		}
		
		return buf.toString();
	}
	
	/**
	 * Get a CREATE TABLE statement for creating a table with the given schema.
	 * 
	 * @param schema     the table's schema
	 * @return the text of the CREATE TABLE statement
	 */
	public static<E> String getCreateTableStatement(ModelObjectSchema<E> schema) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("CREATE TABLE `");
		sql.append(schema.getDbTableName());
		sql.append("` (");
		
		int createDefinitionCount = 0;
		
		// Columns
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			if (createDefinitionCount > 0) {
				sql.append(",");
			}
			sql.append("\n  `");
			sql.append(field.getName());
			sql.append("` ");
			
			sql.append(getSQLDatatypeWithModifiers(schema, field));
			
			createDefinitionCount++;
		}
		
		// Keys
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			ModelObjectIndexType indexType = schema.getIndexType(field);
			
			if (indexType == ModelObjectIndexType.NONE) {
				continue;
			}
			String keyType = getKeyType(indexType);
			
			if (createDefinitionCount > 0) {
				sql.append(",");
			}
			sql.append("\n  ");

			switch (indexType) {
			case IDENTITY:
				sql.append(keyType + " KEY (`");
				sql.append(field.getName());
				sql.append("`)");
				break;
				
			case UNIQUE:
			case NON_UNIQUE:
				sql.append(keyType + " KEY `");
				sql.append(field.getName());
				sql.append("` (`");
				sql.append(field.getName());
				sql.append("`)");
				break;
				
			case NONE:
				break;
			}
			
			createDefinitionCount++;
		}
		
		// Indices
		int indexCount = 0;
		for (ModelObjectIndex<E> index : schema.getIndexList()) {
			String indexName = schema.getName() + "_idx_" + (indexCount++);

			String keyType = getKeyType(index.getIndexType());
			
			if (createDefinitionCount > 0) {
				sql.append(",");
			}
			sql.append("\n  ");
			
			sql.append(keyType + " INDEX ");
			sql.append(indexName);
			sql.append(" (");
			int fieldCount = 0;
			for (ModelObjectField<? super E, ?> field : index.getFieldList()) {
				if (fieldCount > 0) {
					sql.append(", ");
				}
				sql.append(field.getName());
				fieldCount++;
			}
			sql.append(")");
			
			createDefinitionCount++;
		}
		
		sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8");
		
		return sql.toString();
	}

	private static String getKeyType(ModelObjectIndexType indexType) {
		switch (indexType) {
		case NONE: throw new IllegalArgumentException();
		case NON_UNIQUE: return "";
		case UNIQUE: return "UNIQUE";
		case IDENTITY: return "PRIMARY";
		default:
			throw new IllegalArgumentException("Unknown index type " + indexType);
		}
	}

	/**
	 * Get the SQL datatype for a {@link ModelObjectField}.
	 * Note that the datatype will <em>not</em> include modifiers such
	 * as NOT NULL, etc.
	 * 
	 * @param field the {@link ModelObjectField}
	 * @return the SQL datatype
	 */
	private static String getSQLDatatype(ModelObjectField<?,?> field) {
		if (field.getType() == String.class) {
			// If the field length is Integer.MAX_VALUE, make it a text field.
			// Otherwise, make it VARCHAR.
			return field.getSize() == Integer.MAX_VALUE ? "text" : ("varchar(" + field.getSize() + ")");
		} else if (field.getType() == Short.class) {
			return "mediumint(9)";
		} else if (field.getType() == Integer.class) {
			return "int(11)";
		} else if (field.getType() == Long.class) {
			return "bigint(20)";
		} else if (field.getType() == Boolean.class) {
			return "tinyint(1)";
		} else if (Enum.class.isAssignableFrom(field.getType())) {
			// Enumeration values are represented as integers (their ordinal values)
			// in the database
			return "int(11)";
		} else {
			throw new IllegalArgumentException("Unknown field type: " + field.getType().getName());
		}
	}
	
	/**
	 * Get the SQL datatype for a {@link ModelObjectField}, with modifiers such
	 * as NOT NULL, AUTO_INCREMENT, DEFAULT, etc.
	 * 
	 * @param schema the model class schema
	 * @param field  the field
	 * @return the SQL datatype, with modifiers
	 */
	public static<E> String getSQLDatatypeWithModifiers(ModelObjectSchema<E> schema, ModelObjectField<? super E,?> field) {
		StringBuilder buf = new StringBuilder();
		
		buf.append(getSQLDatatype(field));
		
		buf.append(field.isAllowNull() ? " NULL" : " NOT NULL");
		
		String defaultValue = field.getDefaultValue();
		if (defaultValue != null) {
			buf.append(" DEFAULT ");
			buf.append(defaultValue);
		}
		
		if (schema.getIndexType(field) == ModelObjectIndexType.IDENTITY) {
			buf.append(" AUTO_INCREMENT");
		}
		
		return buf.toString();
	}

	/**
	 * Execute a literal SQL statement.
	 * There is no provision for binding parameters, getting results, etc.
	 * 
	 * @param conn  the Connection to use to execute the statement
	 * @param sql   the statement
	 * @throws SQLException if an error occurs
	 */
	public static void execSql(Connection conn, String sql) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt  = conn.prepareStatement(sql);
			stmt.execute();
		} finally {
			closeQuietly(stmt);
		}
	}


//    // Use introspection to store an arbitrary bean in the database.
//    // Eventually we could use this sort of approach to replace much
//    // of our hand-written JDBC code, although I don't know how great
//    // and idea that would be (for example, it might not yield adequate
//    // performance.)  For just creating the database, it should be
//    // fine.
//    static void storeBean(Connection conn, Object bean, ModelObjectSchema schema, String tableName) throws SQLException {
//    	StringBuilder buf = new StringBuilder();
//    	
//    	buf.append("insert into " + tableName);
//    	buf.append(" values (");
//    	buf.append(getInsertPlaceholdersNoId(schema));
//    	buf.append(")");
//    	
//    	PreparedStatement stmt = null;
//    	ResultSet genKeys = null;
//    	
//    	try {
//    		stmt = conn.prepareStatement(buf.toString(), schema.hasUniqueId() ? PreparedStatement.RETURN_GENERATED_KEYS : 0);
//    		
//    		// Now for the magic: iterate through the schema fields
//    		// and bind the query parameters based on the bean properties.
//    		int index = 1;
//    		for (ModelObjectField field : schema.getFieldList()) {
//    			if (field.isUniqueId()) {
//    				continue;
//    			}
//    			try {
//    				Object value = BeanUtil.getProperty(bean, field.getPropertyName());
//    				if (value instanceof Enum) {
//    					// Enum values are converted to integers
//    					value = Integer.valueOf(((Enum<?>)value).ordinal());
//    				}
//    				stmt.setObject(index++, value);
//    			} catch (Exception e) {
//    				throw new SQLException(
//    						"Couldn't get property " + field.getPropertyName() +
//    						" of " + bean.getClass().getName() + " object");
//    			}
//    		}
//    		
//    		// Execute the insert
//    		stmt.executeUpdate();
//    		
//    		if (schema.hasUniqueId()) {
//    			genKeys = stmt.getGeneratedKeys();
//    			if (!genKeys.next()) {
//    				throw new SQLException("Couldn't get generated id for " + bean.getClass().getName()); 
//    			}
//    			int id = genKeys.getInt(1);
//    			
//    			// Set the unique id value in the bean
//    			try {
//    				BeanUtil.setProperty(bean, schema.getUniqueIdField().getPropertyName(), id);
//    			} catch (Exception e) {
//    				e.printStackTrace();
//    				throw new SQLException("Couldn't set generated unique id for " + bean.getClass().getName(), e);
//    			}
//    		}
//    	} finally {
//    		closeQuietly(genKeys);
//    		closeQuietly(stmt);
//    	}
//    }

	/**
	 * Connect to the database server without connecting to a specific
	 * database on that server.
	 * 
	 * @param configProperties CloudCoder configuration properties
	 * @param prefix           the prefix for database-related configuration properties (e.g., "cloudcoder.db")
	 * @return the Connection to the database server
	 * @throws SQLException 
	 */
	public static Connection connectToDatabaseServer(Properties config, String prefix) throws SQLException {
		return doConnectToDatabaseServer(config, prefix, "");
	}

	/**
	 * Connect to the CloudCoder database.
	 * 
	 * @param config the CloudCoder configuration properties
	 * @param prefix           the prefix for database-related configuration properties (e.g., "cloudcoder.db")
	 * @return the Connection to the CloudCoder database
	 * @throws SQLException 
	 */
	public static Connection connectToDatabase(Properties config, String prefix) throws SQLException {
		return doConnectToDatabaseServer(config, prefix, getProp(config, prefix + ".databaseName"));
	}

	private static Connection doConnectToDatabaseServer(Properties config, String prefix, String databaseName) throws SQLException {
		String dbUser = getProp(config, prefix + ".user");
		String dbPasswd = getProp(config, prefix + ".passwd");
		String dbHost = getProp(config, prefix + ".host");

		String portStr = "";
		if (config.getProperty(prefix + ".portStr") != null) {
			portStr = config.getProperty(prefix + ".portStr");
		}
		try {
		    String url="jdbc:mysql://" + dbHost + portStr+ "/" + databaseName + "?user=" + dbUser + "&password=" + URLEncoder.encode(dbPasswd, "UTF-8");
		    logger.info(url);
		    return DriverManager.getConnection(url);
		} catch (UnsupportedEncodingException e) {
		    // should never happen
		    throw new RuntimeException(e);
		}
	}
	
	private static String getProp(Properties properties, String propName) {
		String value = properties.getProperty(propName);
		if (value == null) {
			throw new IllegalArgumentException("configuration property " + propName + " is not defined");
		}
		return value;
	}

	/**
	 * Create a database table.
	 * 
	 * @param conn       the Connection to the database
	 * @param schema     the {@link ModelObjectSchema} describing the type of object to be stored in the table
	 * @throws SQLException
	 */
	public static<E> void createTable(Connection conn, ModelObjectSchema<E> schema) throws SQLException {
		String sql = getCreateTableStatement(schema);
		execSql(conn, sql);
		
		// Check the schema to see if there are any PersistModelObjectDeltas.
		// If so, store the specified model object(s).
		for (Delta<? super E> delta_ : schema.getDeltaList()) {
			if (delta_ instanceof PersistModelObjectDelta) {
				SchemaUtil.applyDelta(conn, (PersistModelObjectDelta<?, ?>) delta_);
			}
		}
	}

	/**
	 * Store an arbitrary model object in the database.
	 * 
	 * @param conn      the Connection to the database
	 * @param bean      the bean (model object) to store in the database
	 */
	public static<E extends IModelObject<E>> void storeModelObject(Connection conn, E bean) throws SQLException {
		storeModelObject(conn, bean, bean.getSchema());
	}

	/**
	 * Store an arbitrary model object in the database.
	 * 
	 * @param conn     the Connection to the database
	 * @param bean     the bean (model) object to store in the database
	 * @param schema   the model object's schema
	 * @throws SQLException
	 */
	public static<E> void storeModelObject(
			Connection conn, E bean, ModelObjectSchema<? super E> schema) throws SQLException {
		doInsertModelObject(conn, bean, schema, false);
	}
	
	/**
	 * Insert a model object in the database, using the exact field
	 * values stored in the model object.  Bypasses the automatic assignment
	 * of the unique id.
	 * 
	 * @param conn    the Connection to the database
	 * @param bean    the model object to insert
	 * @param schema  the model object's schema
	 * @throws SQLException
	 */
	public static<E> void insertModelObjectExact(
			Connection conn, E bean, ModelObjectSchema<? super E> schema) throws SQLException {
		doInsertModelObject(conn, bean, schema, true);
	}
	
	public static<E> void doInsertModelObject(Connection conn, E bean, ModelObjectSchema<? super E> schema, boolean exact) throws SQLException {
		
		String insertSql = createInsertStatement(schema);
		
		PreparedStatement stmt = null;
		ResultSet genKeys = null;
		
		try {
			stmt = conn.prepareStatement(insertSql, (!exact && schema.hasUniqueId()) ? PreparedStatement.RETURN_GENERATED_KEYS : 0);
			
			// Bind model object field values
			bindModelObjectValuesForInsert(bean, schema, stmt);
			
			// Execute the insert
			stmt.executeUpdate();

			// Store back the unique id to the model object
			// (if there is a possibility that a unique id was auto-generated)
			if (!exact && schema.hasUniqueId()) {
				genKeys = stmt.getGeneratedKeys();
				List<E> beans = new ArrayList<E>();
				beans.add(bean);
				getModelObjectUniqueIds(beans, schema, genKeys);
			}
		} finally {
			closeQuietly(genKeys);
			closeQuietly(stmt);
		}
		
	}
	
	/**
	 * Update a model object loaded from an database record,
	 * based on the model object's unique id.
	 * The model object schema <em>must</em> have a unique id field.
	 * 
	 * @param conn    the database connection
	 * @param bean    the model object to update
	 * @param schema  the model object's schema
	 * @throws SQLException
	 */
	public static<E> void updateModelObject(Connection conn, E bean, ModelObjectSchema<? super E> schema) throws SQLException {
        ModelObjectField<? super E, ?> uniqueIdField = schema.getUniqueIdField();	
		
        // Generate an SQL update statement to update the model object's database record
		String updateSql = createUpdateStatement(schema);
        
        PreparedStatement stmt = null;
        ResultSet genKeys = null;
        
        try {
        	// Prepare the statement
            stmt = conn.prepareStatement(updateSql);
            
            // Bind model object field values (except for unique id)
            int index = bindModelObjectValuesForUpdate(bean, schema, stmt);
            
            // Bind unique id
            stmt.setObject(index, uniqueIdField.get(bean));
            
            // Execute the update
            stmt.executeUpdate();
        } finally {
            closeQuietly(genKeys);
            closeQuietly(stmt);
        }
    }
	
	/**
	 * Get the generated unique id(s) resulting from an insert statement,
	 * storing them in one or more model objects.
	 * 
	 * @param beans   the model object(s) whose unique ids should be set
	 * @param schema  the model objects' schema
	 * @param genKeys the {@link ResultSet} with the generated unique ids
	 * @throws SQLException
	 */
	public static <E> void getModelObjectUniqueIds(List<E> beans, ModelObjectSchema<? super E> schema, ResultSet genKeys)
			throws SQLException {
		for (E bean : beans) {
			if (!genKeys.next()) {
				throw new SQLException("Couldn't get generated id for " + bean.getClass().getName()); 
			}
			int id = genKeys.getInt(1);
			
			// Set the unique id value in the bean
			try {
				schema.getUniqueIdField().setUntyped(bean, (Integer)id);
			} catch (Exception e) {
				e.printStackTrace();
				throw new SQLException("Couldn't set generated unique id for " + bean.getClass().getName(), e);
			}
		}
	}

	/**
	 * Bind all of the field values in given model object to the given
	 * {@link PreparedStatement}, which should be an insert statement prepared
	 * from the SQL returned by {@link #createInsertStatement(ModelObjectSchema)}.
	 * 
	 * @param bean    the model object
	 * @param schema  the model object's schema
	 * @param stmt    the {@link PreparedStatement}
	 * @throws SQLException
	 */
	public static <E> void bindModelObjectValuesForInsert(E bean, ModelObjectSchema<E> schema, PreparedStatement stmt)
			throws SQLException {
		// Now for the magic: iterate through the schema fields
		// and bind the query parameters based on the bean properties.
		int index = 1;
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			if (field.isUniqueId()) {
				continue;
			}
			Object value = field.get(bean);
			if (value instanceof Enum) {
				// Enum values are converted to integers
				value = Integer.valueOf(((Enum<?>)value).ordinal());
			}
			stmt.setObject(index++, value);
		}
	}
	
	/**
     * Bind all of the field values in given model object to the given
     * {@link PreparedStatement}, which should be an update statement prepared
     * from the SQL returned by {@link #createUpdateStatement(ModelObjectSchema)}.
     * Note that this method does <em>not</em> bind the model object's
     * unique id.
     * 
     * @param bean    the model object
     * @param schema  the model object's schema
     * @param stmt    the {@link PreparedStatement}
     * @return the index of the next open spot in the preparedStatement
     * @throws SQLException
     */
    public static <E> int bindModelObjectValuesForUpdate(E bean, ModelObjectSchema<E> schema, PreparedStatement stmt)
            throws SQLException {
        // Now for the magic: iterate through the schema fields
        // and bind the query parameters based on the bean properties.
        int index = 1;
        for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
            if (field.isUniqueId()) {
                continue;
            }
            Object value = field.get(bean);
            if (value instanceof Enum) {
                // Enum values are converted to integers
                value = Integer.valueOf(((Enum<?>)value).ordinal());
            }
            stmt.setObject(index++, value);
        }
        return index;
    }

	/**
	 * Create an SQL statement for inserting a model object.
	 * The SQL statement will have placeholders for every model object
	 * field except for the unique id (if any).
	 *  
	 * @param schema the model object schema
	 * @return the SQL insert statement
	 */
	public static <E> String createInsertStatement(ModelObjectSchema<E> schema) {
		return doCreateInsertStatement(schema, false);
	}
	
	/**
	 * Create an SQL statement for inserting a model object.
	 * The SQL statement will have placeholders for every model object,
	 * <em>including</em> the unique id (if any).
	 *  
	 * @param schema the model object schema
	 * @return the SQL insert statement
	 */
	public static <E> String createInsertStatementExact(ModelObjectSchema<E> schema) {
		return doCreateInsertStatement(schema, true);
	}

	private static <E> String doCreateInsertStatement(ModelObjectSchema<E> schema, boolean exact) {
		StringBuilder buf = new StringBuilder();
		buf.append("insert into " + schema.getDbTableName());
		buf.append(" values (");
		buf.append(exact ? getInsertPlaceholders(schema) : getInsertPlaceholdersNoId(schema));
		buf.append(")");
		String insertSql = buf.toString();
		return insertSql;
	}

	/**
     * Create an SQL statement for updating a model object.
     * The SQL statement will have update placeholders for every model object
     * field except for the unique id, followed by a placeholder for the
     * unique id (as part of the WHERE clause).
     * The schema <em>must</em> have a unique id field.
     *  
     * @param schema the model object schema
     * @return the SQL update statement
     */
    public static <E> String createUpdateStatement(ModelObjectSchema<E> schema) {
    	ModelObjectField<? super E, ?> uniqueIdField = schema.getUniqueIdField();
    	
        StringBuilder buf = new StringBuilder();
        
        buf.append("update " + schema.getDbTableName());
        buf.append(" set ");
        
        buf.append(getUpdatePlaceholdersNoId(schema));
        
        buf.append(" where ");
        buf.append(uniqueIdField.getName());
        buf.append(" = ?");
        
        return buf.toString();
    }

	/**
	 * Attempt to load the CloudCoder configuration properties;
	 * either from an embedded "cloudcoder.properties" resource
	 * (which would be the case in production) or from the filesystem
	 * (for development).
	 * 
	 * @return the CloudCoder configuration properties
	 * @throws IOException
	 */
	public static Properties getConfigProperties() throws IOException {
		Properties properties = new Properties();
	
		// See if we can load "cloudcoder.properties" as an embedded resource.
		URL u = CreateWebappDatabase.class.getClassLoader().getResource("cloudcoder.properties");
		if (u != null) {
			InputStream in = u.openStream();
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} else {
			System.out.println("Warning: loading cloudcoder.properties from filesystem");
			File cloudCoderProperties=findRecursively("cloudcoder.properties");
			if (cloudCoderProperties==null) {
			    throw new IOException("Cannot find cloudcoder.properties file in any directory "+
			            "between the current dir and the root of the filesystem");
			}
			properties.load(new FileReader(cloudCoderProperties));
		}
		return properties;
	}

	/**
	 * Search for the given filename between the current directory
	 * where Java is running, and the root of the file-system.
	 * 
	 * TODO: Test on Windows
	 * 
	 * @param filename
	 * @return The file if a file with the given name is found,
	 *     null otherwise.
	 * @throws IOException
	 */
	public static File findRecursively(String filename)
	throws IOException
	{
	    File dir=new File(".").getAbsoluteFile();
	    int i=0;
	    File root=new File("/");
	    while (!dir.getAbsoluteFile().equals(root) && i < 25) {
	        File file=new File(dir, filename);
	        if (file.exists()) {
	            return file;
	        }
	        dir=dir.getParentFile();
	        i++;
	    }
	    return null;
	}
	
	/**
	 * Create a database.
	 * 
	 * @param conn    Connection to the database server
	 * @param dbName  name of the database to create
	 * @throws SQLException
	 */
	public static void createDatabase(Connection conn, String dbName) throws SQLException {
		execSql(
				conn,
				"create database " + dbName +
				" character set 'utf8' " +
				" collate 'utf8_general_ci' ");
	}

	/**
	 * Convert given value to given type.
	 * We use this to massage values returned by JDBC so that they
	 * are correct for storing in a model object field.
	 * 
	 * @param value the value to convert
	 * @param type  the type to convert the value to
	 * @return the converted value
	 */
	public static Object convertValue(Object value, Class<?> type) {
		// Easy case: value is correct type already
		if (value.getClass() == type) {
			return value;
		}
		
		if (type.isEnum()) {
			// value must be an Integer
			return type.getEnumConstants()[(Integer)value];
		} else if (type == Boolean.class) {
			// value must be some kind of integer
			if (value instanceof Number) {
				Number n = (Number) value;
				return n.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
			}
		}
		
		throw new IllegalArgumentException("Unsupported conversion from " + value.getClass().getName() + " to " + type.getName());
	}

	/**
	 * Convert a model object field value so it is suitable for storing
	 * in the database.  The main issue that is addressed here is
	 * converting enum values to their ordinal integer values (which is how
	 * they're stored in the database.)
	 * 
	 * @param value a model object field value
	 * @return value suitable for storing in the database
	 */
	public static Object convertValueToStore(Object value) {
		if (value instanceof Enum) {
			// Special case: convert enum values to their ordinal integer values
			Enum<?> member = (Enum<?>) value;
			//System.out.println("Converting enum value " + value + " to integer " + ((Integer)member.ordinal()));
			value = (Integer) member.ordinal();
		}
		return value;
	}

	/**
	 * Store a {@link ConfigurationSetting}.
	 * 
	 * @param conn            the connection to the database
	 * @param configPropName  the {@link ConfigurationSettingName}
	 * @param configPropValue the value of the configuration setting
	 * @throws SQLException
	 */
	public static void storeConfigurationSetting(Connection conn,
			ConfigurationSettingName configPropName, String configPropValue)
			throws SQLException {
		ConfigurationSetting instName = new ConfigurationSetting();
		instName.setName(configPropName);
		instName.setValue(configPropValue);
		storeModelObject(conn, instName);
	}
	
	/**
	 * Get list of all model objects of specified type from the database.
	 * 
	 * @param conn    the database connection
	 * @param schema  the type of model object to retrieve
	 * @param factory factory to create new instances of model objects
	 * @return list of all model objects
	 * @throws SQLException
	 */
	public static<E> List<E> getAllModelObjects(Connection conn, ModelObjectSchema<E> schema, IFactory<E> factory) throws SQLException {
		ArrayList<E> result = new ArrayList<E>();
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = conn.prepareStatement("select * from " + schema.getDbTableName());
			resultSet = stmt.executeQuery();
			
			while (resultSet.next()) {
				// Create new model object
				E obj = factory.create();
				
				// Load model object fields
				loadModelObjectFields(obj, schema, resultSet);
				
				// Add to list
				result.add(obj);
			}
			
			return result;
		} finally {
			closeQuietly(resultSet);
			closeQuietly(stmt);
		}
	}

	/**
	 * Load a model object's fields from a {@link ResultSet}.
	 * 
	 * @param obj        the model object
	 * @param schema     the model object's schema
	 * @param resultSet  the {@link ResultSet}
	 * @return int the index of the field in the result set just past the model object fields
	 * @throws SQLException
	 */
	public static <E> int loadModelObjectFields(E obj,
			ModelObjectSchema<E> schema, ResultSet resultSet)
			throws SQLException {
		return loadModelObjectFields(obj, schema, resultSet, 1);
	}

	/**
	 * Load a model object's fields from a {@link ResultSet}.
	 * 
	 * @param obj        the model object
	 * @param schema     the model object's schema
	 * @param resultSet  the {@link ResultSet}
	 * @param index      the index of the first model object field in the {@link ResultSet}
	 * @return int the index of the field in the result set just past the model object fields
	 * @throws SQLException
	 */
	public static <E> int loadModelObjectFields(E obj,
			ModelObjectSchema<E> schema, ResultSet resultSet, int index)
			throws SQLException {
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			Object value = resultSet.getObject(index++);
			value = convertValue(value, field.getType());
			field.setUntyped(obj, value);
		}
		return index;
	}

	/**
	 * Reload a model object's fields based on its unique id.
	 * 
	 * @param conn   connection to the database
	 * @param obj    the object to load: the unique id must be set!
	 * @param schema the object's schema
	 * @throws SQLException if there is no such object in the database
	 */
	public static<E> void loadModelObject(Connection conn, E obj, ModelObjectSchema<? super E> schema) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			ModelObjectField<? super E, ?> uniqueIdField = schema.getUniqueIdField();
			stmt = conn.prepareStatement(
					"select * from " + schema.getDbTableName() +
					" where " + uniqueIdField.getName() + " = ?");
			Object uniqueId = uniqueIdField.get(obj);
			stmt.setObject(1, uniqueId);
			
			resultSet = stmt.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("No object found with unique id " + uniqueId);
			}
			
			loadModelObjectFields(obj, schema, resultSet);
		} finally {
			closeQuietly(resultSet);
			closeQuietly(stmt);
		}
	}

	/**
	 * Reload a model object's fields based on its unique id.
	 * 
	 * @param conn   connection to the database
	 * @param obj    the object to load: the unique id must be set!
	 * @throws SQLException if there is no such object in the database
	 */
	public static<E extends IModelObject<E>> void loadModelObject(Connection conn, E obj) throws SQLException {
		loadModelObject(conn, obj, obj.getSchema());
	}
}
