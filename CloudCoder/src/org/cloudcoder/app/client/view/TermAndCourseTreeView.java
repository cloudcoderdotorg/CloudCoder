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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.TermAndYear;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

/**
 * Tree view for terms and courses.
 * 
 * @author David Hovemeyer
 */
public class TermAndCourseTreeView extends Composite {
	/**
	 * Tree view model for TermAndCourseTreeView.
	 */
	private class Model implements TreeViewModel {
		private List<TermAndYear> termAndYearList;
		private Map<TermAndYear, Course[]> termAndYearToCourseList;
		
		private SingleSelectionModel<Course> selectionModel;
		
		public Model(CourseAndCourseRegistration[] courseAndRegList) {
			// Build sorted collection of TermAndYear objects
			TreeSet<TermAndYear> termAndYearSet = new TreeSet<TermAndYear>();
			for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
				Course course = courseAndReg.getCourse();
				termAndYearSet.add(course.getTermAndYear());
			}
			
			termAndYearList = new ArrayList<TermAndYear>();
			termAndYearToCourseList = new HashMap<TermAndYear, Course[]>();

			// Build list of TermAndYears, in reverse chronological order.
			// For each TermAndYear, map it to an array of Courses.
			for (TermAndYear termAndYear : termAndYearSet) {
				termAndYearList.add(termAndYear);
				ArrayList<Course> courseListForTermAndYear = new ArrayList<Course>();
				for (CourseAndCourseRegistration courseAndReg : courseAndRegList) {
					Course course = courseAndReg.getCourse();
					// Note: because a user may have multiple CourseRegistrations
					// for the same course (e.g., an instructor who is teaching
					// multiple sections), we may have duplicate courses.
					// So, check before adding one to the course list.
					if (course.getTermAndYear().equals(termAndYear) && !courseListForTermAndYear.contains(course)) {
						courseListForTermAndYear.add(course);
					}
				}
				termAndYearToCourseList.put(
						termAndYear,
						courseListForTermAndYear.toArray(new Course[courseListForTermAndYear.size()]));
			}
			
			// Selection model: for now, just single selection for Courses only
			// TODO: multiple selection, all nodes should be eligible
			selectionModel = new SingleSelectionModel<Course>();
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.view.client.TreeViewModel#getNodeInfo(java.lang.Object)
		 */
		@Override
		public <T> NodeInfo<?> getNodeInfo(T value) {
			if (value == null) {
				// root nodes - TermAndYear
				ListDataProvider<TermAndYear> dataProvider = new ListDataProvider<TermAndYear>();
				dataProvider.getList().addAll(termAndYearList);
				Cell<TermAndYear> cell = new AbstractCell<TermAndYear>() {
					/* (non-Javadoc)
					 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
					 */
					@Override
					public void render(
							com.google.gwt.cell.client.Cell.Context context,
							TermAndYear value, SafeHtmlBuilder sb) {
						sb.appendEscaped(value.toString());
					}
				};
				return new DefaultNodeInfo<TermAndYear>(dataProvider, cell);
			} else if (value instanceof TermAndYear) {
				Course[] courseList = termAndYearToCourseList.get(value);
				ListDataProvider<Course> dataProvider = new ListDataProvider<Course>();
				dataProvider.getList().addAll(Arrays.asList(courseList));
				Cell<Course> cell = new AbstractCell<Course>() {
					/* (non-Javadoc)
					 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
					 */
					@Override
					public void render(
							com.google.gwt.cell.client.Cell.Context context,
							Course value, SafeHtmlBuilder sb) {
						sb.appendEscaped(value.toString());
					}
				};
				return new DefaultNodeInfo<Course>(dataProvider, cell, selectionModel, null);
			} else {
				throw new IllegalStateException();
			}
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.view.client.TreeViewModel#isLeaf(java.lang.Object)
		 */
		@Override
		public boolean isLeaf(Object value) {
			return value instanceof Course;
		}

		/**
		 * Add Handler for selection change events.
		 * 
		 * @param h the Handler to add
		 */
		public void addSelectionChangeHandler(Handler h) {
			selectionModel.addSelectionChangeHandler(h);
		}
	}
	
	private CellTree cellTree;
	private Model model;
	
	public TermAndCourseTreeView(CourseAndCourseRegistration[] courseList) {
		model = new Model(courseList);
		cellTree = new CellTree(model, null);
		initWidget(cellTree);
		
		// Expand the first child of the root.
		if (cellTree.getRootTreeNode().getChildCount() > 0) {
			cellTree.getRootTreeNode().setChildOpen(0, true);
		}
	}
	
	/**
	 * Add Handler for selection change events.
	 * 
	 * @param h the Handler to add
	 */
	public void addSelectionHandler(Handler h) {
		model.addSelectionChangeHandler(h);
	}

	/**
	 * @return the currently selected course
	 */
	public Course getSelectedCourse() {
		return model.selectionModel.getSelectedObject();
	}
}
