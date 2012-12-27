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

package org.cloudcoder.app.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * A widget to use as a section label for describing a region of a UI.
 * 
 * @author David Hovemeyer
 */
public class SectionLabel extends Composite {
	/**
	 * A SectionLabel should be this many pixels high when positioned absolutely.
	 */
	public static final double HEIGHT_PX = 36.0;

	/**
	 * Constructor.
	 * 
	 * @param text the text to show
	 */
	public SectionLabel(String text) {
		HTML label = new HTML("<div><span class=\"cc-sectionLabel\">" + text + "</span></div>");
		
		initWidget(label);
	}
}
