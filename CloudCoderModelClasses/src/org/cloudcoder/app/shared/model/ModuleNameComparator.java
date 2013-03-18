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

import java.util.Comparator;

/**
 * Comparator to compare {@link Module}s by name.
 * Handles the case of "---DDD" specially, where "DDD"
 * is a sequence of digits, in order to get numeric ordering
 * in the case where the prefixes ("---") are the same.
 * For example, the module "Week10" should compare as
 * greater than "Week8", even though lexicographically
 * it would be considered less. 
 * 
 * @author David Hovemeyer
 */
public class ModuleNameComparator implements Comparator<Module> {

	@Override
	public int compare(Module left, Module right) {
		String leftDigits = findDigits(left.getName());
		String rightDigits = findDigits(right.getName());
		
		if (leftDigits != null && rightDigits != null) {
			// Both left and right modules have trailing digits.
			// See if they have a common prefix.
			String leftPrefix = left.getName().substring(0, left.getName().length() - leftDigits.length());
			String rightPrefix = right.getName().substring(0, right.getName().length() - rightDigits.length());
			if (leftPrefix.equals(rightPrefix)) {
				// Prefixes are the same.
				
				// The result of the comparison is a numeric comparison
				// of the trailing digits.
				int cmp = Integer.valueOf(leftDigits).compareTo(Integer.valueOf(rightDigits));
				if (cmp != 0) {
					return cmp;
				}
				
				// Special case: digit strings are numerically equal, but
				// if they are textually different, do a lexicographical
				// comparison of the digit strings.
				// (E.g., "Week08" and "Week8" should compare as different,
				// with the former being < the latter.)
				return leftDigits.compareTo(rightDigits);
			}
		}
		
		// No trailing digits, or prefixes aren't the same.
		// Just compare by name.
		return left.getName().compareTo(right.getName());
	}

	private String findDigits(String s) {
		int index = -1;
		for (int i = s.length() - 1; i >= 0; i--) {
			char c = s.charAt(i);
			if (c >= '0' && c <= '9') {
				index = i;
			} else {
				break;
			}
		}
		if (index < 0) {
			return null;
		}
		return s.substring(index);
	}

}
