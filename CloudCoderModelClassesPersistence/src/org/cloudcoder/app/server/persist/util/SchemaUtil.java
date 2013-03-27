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

package org.cloudcoder.app.server.persist.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.cloudcoder.app.shared.model.IModelObject;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectIndexType;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.ModelObjectSchema.AddFieldDelta;
import org.cloudcoder.app.shared.model.ModelObjectSchema.Delta;
import org.cloudcoder.app.shared.model.ModelObjectSchema.DeltaType;
import org.cloudcoder.app.shared.model.ModelObjectSchema.IncreaseFieldSizeDelta;
import org.cloudcoder.app.shared.model.ModelObjectSchema.PersistModelObjectDelta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with table schema versions and migrations.
 * 
 * @author David Hovemeyer
 */
public class SchemaUtil {
	private static final Logger logger = LoggerFactory.getLogger(SchemaUtil.class);
	
	/**
	 * Create the schema version table if it does not already exist.
	 * 
	 * @param conn  the Connection to the database (which must have the
	 *              CloudCoder webapp or repository database set as the current
	 *              database)
	 * @param tables the list of tables that the database contains
	 * @return true if the cc_schema_version table was created, false otherwise
	 * @throws SQLException
	 */
	public static boolean createSchemaVersionTableIfNeeded(Connection conn, ModelObjectSchema<?>[] tables) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Statement create = null;
		PreparedStatement insert = null;
		
		try {
			stmt = conn.prepareStatement(
					"select count(*) from information_schema.tables " +
					" where table_schema = DATABASE() " +
					"   and table_name = 'cc_schema_version'"
			);
			
			resultSet = stmt.executeQuery();
			
			if (!resultSet.next()) {
				throw new SQLException("Couldn't retrieve information from information_schema.tables");
			}
			
			int count = resultSet.getInt(1);
			if (count > 0) {
				// it exists
				return false;
			}
			
			// schema version table doesn't exist yet, so create it
			create = conn.createStatement();
			create.execute(
					"create table cc_schema_version (" +
					"  table_name varchar(50) PRIMARY KEY, " +
					"  schema_version MEDIUMINT " +
					")"
			);
			
			insert = conn.prepareStatement("insert into cc_schema_version values (?, ?)");
			for (ModelObjectSchema<?> table : tables) {
				insert.setString(1, table.getDbTableName());
				insert.setInt(2, table.getVersion());
				insert.addBatch();
			}
			
			insert.executeBatch();

			return true;
		} finally {
			DBUtil.closeQuietly(insert);
			DBUtil.closeQuietly(create);
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
		}
	}
	
	/**
	 * Get the schema version number for given table.
	 * 
	 * @param conn the connection
	 * @param table the table to check
	 * @return the schema version, or -1 if there is no schema version
	 *         (which would be the case for a table that doesn't exist yet)
	 * @throws SQLException
	 */
	public static int getDbSchemaVersion(Connection conn, ModelObjectSchema<?> table)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = conn.prepareStatement(
					"select schema_version from cc_schema_version " +
					" where table_name = ?"
			);
			stmt.setString(1, table.getDbTableName());
			
			resultSet = stmt.executeQuery();
			if (!resultSet.next()) {
				//throw new SQLException("No entry in cc_schema_version for table " + table.getDbTableName());
				return -1;
			}
			
			return resultSet.getInt(1);
		} finally {
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
		}
	}
	
	/**
	 * If necessary, migrate given table in databsae to the current schema version
	 * (as specified by the model object metadata).
	 * 
	 * @param conn    connection to the database
	 * @param table   current schema (model object metadata)
	 * @return   true if the table was migrated to the latest schema version,
	 *           false if the table was already at the latest schema version
	 * @throws SQLException
	 */
	public static<E> boolean migrateTable(Connection conn, ModelObjectSchema<E> table) throws SQLException {
		
		conn.setAutoCommit(false);
		
		try {
		
			int dbSchemaVersion = getDbSchemaVersion(conn, table);
			if (dbSchemaVersion == table.getVersion()) {
				// Table is at most recent version: nothing to do
				logger.info("Table " + table.getDbTableName() + " is already at latest version (" + dbSchemaVersion + ")");
				return false;
			}
			
			if (dbSchemaVersion < 0) {
				// Table doesn't exist yet: create it
				DBUtil.createTable(conn, table);
				
				// Add entry to cc_schema_version table
				PreparedStatement insertSchemaVersion = null;
				try {
					insertSchemaVersion = conn.prepareStatement("insert into cc_schema_version values (?, ?)");
					insertSchemaVersion.setString(1, table.getDbTableName());
					insertSchemaVersion.setInt(2, table.getVersion());
					insertSchemaVersion.executeUpdate();
				} finally {
					DBUtil.closeQuietly(insertSchemaVersion);
				}
				return true;
			}
			
			// Apply deltas from each schema version more recent than the
			// database schema version
			for (int version = dbSchemaVersion + 1; version <= table.getVersion(); version++) {
				ModelObjectSchema<E> prevSchema = table.getSchemaWithVersion(version);
				for (Delta<? super E> delta_ : prevSchema.getDeltaList()) {
					if (delta_ instanceof AddFieldDelta) {
						applyDelta(conn, table.getDbTableName(), prevSchema, (AddFieldDelta<? super E>)delta_, version);
					} else if (delta_ instanceof PersistModelObjectDelta) {
						applyDelta(conn, (PersistModelObjectDelta<?, ?>)delta_);
					} else if (delta_ instanceof IncreaseFieldSizeDelta) {
						applyDelta(conn, table, (IncreaseFieldSizeDelta<? super E>) delta_);
					}
				}
			}
			
			// Critically important: update the table's schema version in cc_schema_version
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement("update cc_schema_version set schema_version = ? where table_name = ?");
				stmt.setInt(1, table.getVersion());
				stmt.setString(2, table.getDbTableName());
				stmt.executeUpdate();
			} finally {
				DBUtil.closeQuietly(stmt);
			}
			
			conn.commit();
			
			return true;
		
		} finally {
			conn.setAutoCommit(true);
		}
	}
	
	public static<E> void applyDelta(Connection conn, ModelObjectSchema<E> schema, IncreaseFieldSizeDelta<? super E> delta) throws SQLException {
		PreparedStatement stmt = null;
		
		try {
			ModelObjectField<? super E, ?> field = delta.getField();
			
			StringBuilder buf = new StringBuilder();
			buf.append("alter table ");
			buf.append(schema.getDbTableName());
			buf.append(" modify ");
			buf.append(field.getName());
			buf.append(" ");
			buf.append(DBUtil.getSQLDatatypeWithModifiers(schema, field));
			
			String sql = buf.toString();
			//System.out.println("Modifying column: " + sql);
			
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		} finally {
			DBUtil.closeQuietly(stmt);
		}
	}
	
	public static<E, M extends IModelObject<M>> void applyDelta(Connection conn, PersistModelObjectDelta<E, M> delta) throws SQLException {
		M obj = delta.getObj();
		DBUtil.insertModelObjectExact(conn, obj, obj.getSchema());
	}

	public static<E> void applyDelta(Connection conn, String dbTableName, ModelObjectSchema<E> schema, AddFieldDelta<? super E> delta, int version) throws SQLException {
		if (delta.getType() == DeltaType.ADD_FIELD_AFTER) {
			Statement stmt = null;
			StringBuilder buf;
			
			try {
				stmt = conn.createStatement();
				
				buf = new StringBuilder();
				buf.append("alter table ");
				buf.append(dbTableName);
				buf.append("  add column ");
				buf.append(delta.getField().getName());
				buf.append(" ");
				buf.append(DBUtil.getSQLDatatypeWithModifiers(schema, delta.getField()));
				buf.append(" after ");
				buf.append(delta.getPreviousField().getName());
				
				String sql = buf.toString();
				//System.out.println(sql);
				logger.debug("Migration: {}", sql);
				
				stmt.execute(sql);
				
				ModelObjectIndexType indexType = schema.getIndexType(delta.getField());
				if (indexType != ModelObjectIndexType.NONE) {
					buf = new StringBuilder();
					
					buf.append("alter table ");
					buf.append(dbTableName);
					buf.append(" add ");
					if (indexType == ModelObjectIndexType.UNIQUE) {
						buf.append("unique ");
					}
					buf.append("index ");
					buf.append("addcol_" + version + "_" + delta.getField().getName());
					buf.append(" (");
					buf.append(delta.getField().getName());
					buf.append(")");
					
					String sqlAddIndex = buf.toString();
					
					//System.out.println(sqlAddIndex);
					logger.debug("Migration: {}", sqlAddIndex);
					
					stmt.execute(sqlAddIndex);
				}
			} finally {
				DBUtil.closeQuietly(stmt);
			}
		} else {
			throw new IllegalStateException("Unknown delta type " + delta.getType());
		}
	}

	/*
	public static void main(String[] args) throws Exception {
		Scanner keyboard = new Scanner(System.in);
		
		System.out.println("Do what: create (schema version table) or migrate?");
		String what = keyboard.nextLine();
		
		if (what.equals("create")) {
			// Just for testing
			Properties config = DBUtil.getConfigProperties();
			Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
			createSchemaVersionTableIfNeeded(conn, CreateWebappDatabase.TABLES);
			System.out.println("Created schema version table");
			
			int problemsVersion = getDbSchemaVersion(conn, Problem.SCHEMA);
			System.out.println("cc_problems is at schema version " + problemsVersion);
		} else if (what.equals("migrate")) {
			Properties config = DBUtil.getConfigProperties();
			Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
			
			for (ModelObjectSchema<?> table : CreateWebappDatabase.TABLES) {
				System.out.println("Migrating " + table.getDbTableName() + "...");
				if (migrateTable(conn, table)) {
					System.out.println("Success!");
				} else {
					System.out.println("Already at latest version");
				}
			}
		}
	}
	*/
}
