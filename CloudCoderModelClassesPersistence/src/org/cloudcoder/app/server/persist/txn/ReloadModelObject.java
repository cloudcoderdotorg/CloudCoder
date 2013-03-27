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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.IModelObject;
import org.cloudcoder.app.shared.model.ModelObjectField;

/**
 * Transaction to reload an arbitrary model object implementing the
 * {@link IModelObject} interface.
 */
public class ReloadModelObject<E extends IModelObject<E>> extends AbstractDatabaseRunnableNoAuthException<Boolean> {
	private final E obj;

	/**
	 * Constructor.
	 * 
	 * @param obj the model object to reload
	 */
	public ReloadModelObject(E obj) {
		this.obj = obj;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		ModelObjectField<? super E, ?> idField = obj.getSchema().getUniqueIdField();
		PreparedStatement stmt = prepareStatement(
				conn,
				"select * from " + obj.getSchema().getDbTableName() + " where " + idField.getName() + " = ?"
		);
		stmt.setObject(1, idField.get(obj));
		
		ResultSet resultSet = executeQuery(stmt);
		if (!resultSet.next()) {
			return false;
		}
		
		DBUtil.loadModelObjectFields(obj, obj.getSchema(), resultSet);
		return true;
	}

	@Override
	public String getDescription() {
		return " reloading model object";
	}
}