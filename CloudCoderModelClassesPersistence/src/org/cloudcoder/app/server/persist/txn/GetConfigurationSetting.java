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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;

/**
 * Query to get a {@link ConfigurationSetting} given a {@link ConfigurationSettingName}.
 */
public class GetConfigurationSetting extends AbstractDatabaseRunnableNoAuthException<ConfigurationSetting> {
	private final ConfigurationSettingName name;

	/**
	 * Constructor.
	 * 
	 * @param name name of the configuration setting to get
	 */
	public GetConfigurationSetting(ConfigurationSettingName name) {
		this.name = name;
	}

	@Override
	public ConfigurationSetting run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select s.* from " + ConfigurationSetting.SCHEMA.getDbTableName() + " as s where s.name = ?");
		stmt.setString(1, name.toString());
		ResultSet resultSet = executeQuery(stmt);
		if (!resultSet.next()) {
			return null;
		}
		ConfigurationSetting configurationSetting = new ConfigurationSetting();
		Queries.load(configurationSetting, resultSet, 1);
		return configurationSetting;
	}

	@Override
	public String getDescription() {
		return " retrieving configuration setting";
	}
}