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

/**
 * A limit that can be set in a {@link CommandExecutionPreferences} object.
 * 
 * @author David Hovemeyer
 */
public enum CommandLimit {
	/**
	 * Maximum size of file child process is allowed to write.
	 */
	FILE_SIZE_KB,
	
	/**
	 * Maximum stack size.
	 */
	STACK_SIZE_KB,
	
	/**
	 * Maximum CPU time allowed.
	 */
	CPU_TIME_SEC,
	
	/**
	 * Maximum number of processes.
	 */
	PROCESSES,
	
	/**
	 * Maximum virtual memory.
	 */
	VM_SIZE_KB,
	
	/**
	 * Maximum bytes of output allowed.
	 */
	OUTPUT_MAX_BYTES,
	
	/**
	 * Maximum lines of output that will be captured.
	 */
	OUTPUT_MAX_LINES,
	
	/**
	 * Maximum number of characters that will be captured for any single line of output.
	 */
	OUTPUT_LINE_MAX_CHARS,
}