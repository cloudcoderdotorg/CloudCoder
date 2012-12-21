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

package org.cloudcoder.builder2.process;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.builder2.model.CommandExecutionPreferences;
import org.cloudcoder.builder2.model.CommandLimit;

/**
 * ProcessRunner implementation that sets process resource limits
 * appropriate for an untrusted test process.
 * 
 * @author David Hovemeyer
 */
public class LimitedProcessRunner extends ProcessRunner {
	private static final Map<CommandLimit, Integer> DEFAULT_LIMIT_MAP = new HashMap<CommandLimit, Integer>();
	static {
		// Set default limits: these serve as reasonable defaults
		// for any limits not explicitly specified.
		DEFAULT_LIMIT_MAP.put(CommandLimit.FILE_SIZE_KB, 0);
		DEFAULT_LIMIT_MAP.put(CommandLimit.STACK_SIZE_KB, 128);
		DEFAULT_LIMIT_MAP.put(CommandLimit.CPU_TIME_SEC, 10);
		DEFAULT_LIMIT_MAP.put(CommandLimit.PROCESSES, 0);
		DEFAULT_LIMIT_MAP.put(CommandLimit.VM_SIZE_KB, 32768);
		DEFAULT_LIMIT_MAP.put(CommandLimit.OUTPUT_LINE_MAX_CHARS, 200);
		DEFAULT_LIMIT_MAP.put(CommandLimit.OUTPUT_MAX_BYTES, 10000);
		DEFAULT_LIMIT_MAP.put(CommandLimit.OUTPUT_MAX_LINES, 50);
	}
	
	private Map<CommandLimit, Integer> limitMap;

	/**
	 * Constructor.
	 */
	public LimitedProcessRunner() {
		limitMap = new HashMap<CommandLimit, Integer>();
		limitMap.putAll(DEFAULT_LIMIT_MAP);
	}

	/**
	 * Set the {@link CommandExecutionPreferences} specifying process limits.
	 * 
	 * @param prefs the {@link CommandExecutionPreferences} to set
	 */
	public void setPreferences(CommandExecutionPreferences prefs) {
		limitMap.putAll(prefs.getMap());
	}
	
	/**
	 * Clear all currently-set limits.
	 */
	public void clearLimits() {
		limitMap.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.ProcessRunner#getEnvp(java.lang.String[])
	 */
	@Override
	protected String[] getEnvp(String... extraVars) {
		List<String> allEnvVars = new ArrayList<String>();
		allEnvVars.addAll(Arrays.asList(super.getEnvp(extraVars)));
		
		// In addition to the environment variables created by the superclass getEnvp(),
		// define the CC_PROCESS_RESOURCE_LIMITS environment variable used
		// by runProcess.sh to define resource limits for the created
		// process.
		
		StringBuilder buf = new StringBuilder();
		buf.append("CC_PROCESS_RESOURCE_LIMITS=");
		for (Map.Entry<CommandLimit, Integer> entry : limitMap.entrySet()) {
			CommandLimit limit = entry.getKey();
			if (limit.isUlimitFlag()) {
				buf.append(limit.getFlag());
				buf.append(String.valueOf(entry.getValue()));
				buf.append(" ");
			}
		}
		buf.setLength(buf.length() - 1);
		
		String limits = buf.toString();
		//System.out.println("Limits: " + limits);
		allEnvVars.add(limits);
		
		return allEnvVars.toArray(new String[allEnvVars.size()]);
	}

	@Override
	protected IOutputCollector createOutputCollector(InputStream inputStream) {
		LimitedOutputCollector collector = new LimitedOutputCollector(inputStream);
		
		collector.setMaxBytesAllowed(limitMap.get(CommandLimit.OUTPUT_MAX_BYTES));
		collector.setMaxLinesAllowed(limitMap.get(CommandLimit.OUTPUT_MAX_LINES));
		collector.setMaxCharactersPerLine(limitMap.get(CommandLimit.OUTPUT_LINE_MAX_CHARS));
		
		return collector;
	}
}
