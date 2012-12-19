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

package org.cloudcoder.builder2.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Preferences for how {@link Command}s should be executed.
 * 
 * @author David Hovemeyer
 */
public class CommandExecutionPreferences {
	private Map<CommandLimit, Integer> limitMap;
	
	/**
	 * Constructor. No preferences are set by default.
	 */
	public CommandExecutionPreferences() {
		limitMap = new HashMap<CommandLimit, Integer>();
	}
	
	/**
	 * Set a limit.
	 * 
	 * @param limitType type of limit to set
	 * @param value     the limit value
	 */
	public void setLimit(CommandLimit limitType, int value) {
		limitMap.put(limitType, value);
	}
	
	/**
	 * Check whether a limit has been set.
	 * 
	 * @param limitType type of limit
	 * @return true if a limit value has been set, false otherwise
	 */
	public boolean isSet(CommandLimit limitType) {
		return limitMap.containsKey(limitType);
	}
	
	/**
	 * Get limit value.
	 * Assumes that a limit of specified has been set.
	 * 
	 * @param limitType type of limit
	 * @return value for the limit
	 */
	public int getLimit(CommandLimit limitType) {
		return limitMap.get(limitType).intValue();
	}
}
