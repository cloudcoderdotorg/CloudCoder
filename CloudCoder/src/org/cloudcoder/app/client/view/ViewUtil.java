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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;

/**
 * Utility methods for views.
 * 
 * @author David Hovemeyer
 */
public class ViewUtil {
    public static Image ajaxImage = new Image(GWT.getModuleBaseURL() + ViewUtil.LOADING_IMAGE);
    public static final String LOADING_IMAGE="images/ajax-loader.gif";
    
	/**
	 * Format a Date as a string.
	 * 
	 * @param date the Date to format
	 * @return the formatted Date
	 */
	public static String formatDate(Date date) {
		DateTimeFormat f = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
		return f.format(date);
	}
	
	/**
	 * Determine the {@link AceEditorMode} to use for a given {@link Language}.
	 * 
	 * @param language the Language
	 * @return the AceEditorMode for the Language, or null if the Language is not known
	 */
	public static AceEditorMode getModeForLanguage(Language language) {
		switch (language) {
		case JAVA:
			return AceEditorMode.JAVA;
		case PYTHON:
			return AceEditorMode.PYTHON;
		case C:
		case CPLUSPLUS:
			return AceEditorMode.C_CPP;
		case RUBY:
			return AceEditorMode.RUBY;
		}
		
		// unknown Language
		return null;
	}

	/**
	 * Create a Grid instance with a "loading" animated gif
	 * and the message "Loading..." for display with
	 * waiting for asynchronous calls to complete.
	 * 
	 * The returned result can be added to a variety of
	 * PopupPanels which will then be hidden.
	 * 
	 * @return
	 */
	public static Grid createLoadingGrid() {
	    return createLoadingGrid("");
	}
	
    /**
     * Create a Grid instance with a "loading" animated gif and
     * the message "Loading...", along with the supplied message.
     * 
     * @param message
     * @return
     */
    public static Grid createLoadingGrid(String message)
    {
        Grid grid;
        if (message.length()>0) {
            grid=new Grid(2, 2);
        } else {
            grid=new Grid(1, 2);
        }
        grid.setWidget(0, 0, ViewUtil.ajaxImage);
        grid.setText(0, 1, "Loading...");
        if (message.length()>0) {
            grid.setText(1, 0, message);
        }
        return grid;
    }

    /**
     * Sort array of {@link ProblemAndSubmissionReceipt}s by their due date/time.
     * This is important for helping students to focus on what is due when.
     * 
     * @param list array of {@link ProblemAndSubmissionReceipt}s to sort
     */
	public static void sortProblemsByDueDate(ProblemAndSubmissionReceipt[] list) {
		Arrays.sort(list, new Comparator<ProblemAndSubmissionReceipt>() {
			@Override
			public int compare(ProblemAndSubmissionReceipt o1, ProblemAndSubmissionReceipt o2) {
				long left = o1.getProblem().getWhenDue();
				long right = o2.getProblem().getWhenDue();
				if (left < right) {
					return -1;
				} else if (left > right) {
					return 1;
				} else {
					// Use problem id as tiebreaker
					int leftId = o1.getProblem().getProblemId();
					int rightId = o2.getProblem().getProblemId();
					return leftId - rightId; // We'll take a chance that underflow won't occur
				}
			}
		});
	}
}
