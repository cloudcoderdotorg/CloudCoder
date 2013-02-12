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
import java.util.List;

import org.cloudcoder.app.client.model.NamedTestResult;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * View displaying the list of {@link NamedTestResult}s.
 * 
 * @author David Hovemeyer
 */
public class TestResultListView extends ResizeComposite implements SessionObserver, Subscriber, IResultsTabPanelWidget {
	private DataGrid<NamedTestResult> cellTable;
	
	public TestResultListView() {
		cellTable = new DataGrid<NamedTestResult>();
		
		TestCaseNameColumn testCaseNameColumn = new TestCaseNameColumn();
		cellTable.addColumn(testCaseNameColumn, "Test name");
		cellTable.setColumnWidth(testCaseNameColumn, "160px");
		
		OutcomeColumn outcomeColumn = new OutcomeColumn();
		cellTable.addColumn(outcomeColumn, "Outcome");
		cellTable.setColumnWidth(outcomeColumn, "160px");
		
		cellTable.addColumn(new MessageColumn(), "Message");
		
		cellTable.addColumn(new OutputColumn(new ExtractOutputText() {
			@Override
			public String getOutputText(NamedTestResult testResult) {
				return testResult.getTestResult().getStdout();
			}
		}), "Output");
		
		cellTable.addColumn(new OutputColumn(new ExtractOutputText() {
			@Override
			public String getOutputText(NamedTestResult testResult) {
				return testResult.getTestResult().getStderr();
			}
		}), "Error output");
		
		initWidget(cellTable);
	}
	
	private static class TestCaseNameColumn extends TextColumn<NamedTestResult> {
		@Override
		public String getValue(NamedTestResult object) {
			return object.getTestCaseName();
		}
	}
	
	private static class OutcomeColumn extends TextColumn<NamedTestResult> {
		@Override
		public String getValue(NamedTestResult object) {
			return object.getTestResult().getOutcome().toString().toLowerCase();
		}
		
		@Override
		public String getCellStyleNames(Context context, NamedTestResult object) {
			if (object.getTestResult().getOutcome() == TestOutcome.PASSED) {
				return "cc-passedTest";
			} else {
				return "cc-failedTest";
			}
		}
	}
	
	private static class MessageColumn extends TextColumn<NamedTestResult> {
		@Override
		public String getValue(NamedTestResult object) {
			return object.getTestResult().getMessage();
		}
	}
	
	private static abstract class ShowFullOutputButtonColumn extends Column<NamedTestResult, String> {
		public ShowFullOutputButtonColumn() {
			super(new ButtonCell());

			// Set a FieldUpdater to handle the button click
			setFieldUpdater(new FieldUpdater<NamedTestResult, String>() {
				@Override
				public void update(int index, NamedTestResult object, String value) {
					// Show the TestResultOutputDialog.
					TestResultOutputDialog dialog = new TestResultOutputDialog(getText(object));
					dialog.center();
				}
			});
		}

		protected abstract String getText(NamedTestResult object);

		@Override
		public String getValue(NamedTestResult object) {
			return "Show all";
		}
	}
	
	private static abstract class OutputFirstLineColumn extends TextColumn<NamedTestResult> {
		@Override
		public String getValue(NamedTestResult object) {
			return firstLine(getText(object));
		}

		protected abstract String getText(NamedTestResult object);
		
		private static String firstLine(String s) {
			int eol = s.indexOf('\n');
			return (eol < 0) ? s : s.substring(0, eol);
		}
	}
	
	private interface ExtractOutputText {
		public String getOutputText(NamedTestResult testResult);
	}
	
	private static class OutputColumn extends Column<NamedTestResult, NamedTestResult> {
		public OutputColumn(ExtractOutputText extractor) {
			super(new CompositeCell<NamedTestResult>(getCells(extractor)));
		}
		
		private static List<HasCell<NamedTestResult, ?>> getCells(final ExtractOutputText extractor) {
			List<HasCell<NamedTestResult, ?>> result = new ArrayList<HasCell<NamedTestResult, ?>>();
			result.add(new ShowFullOutputButtonColumn(){
				@Override
				protected String getText(NamedTestResult object) {
					return extractor.getOutputText(object);
				}
			});
			result.add(new OutputFirstLineColumn() {
				@Override
				protected String getText(NamedTestResult object) {
					return extractor.getOutputText(object);
				}
			});
			return result;
		}

		@Override
		public NamedTestResult getValue(NamedTestResult object) {
			return object;
		}
	}
	
	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		NamedTestResult[] testResultList = session.get(NamedTestResult[].class);
		if (testResultList != null) {
			displayTestResults(testResultList);
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof NamedTestResult[]) {
			displayTestResults((NamedTestResult[]) hint);
		}
	}
	
	private void displayTestResults(NamedTestResult[] testResultList) {
		cellTable.setRowData(Arrays.asList(testResultList));
	}
	
	@Override
	public void setSelected() {
		// Workaround for http://code.google.com/p/google-web-toolkit/issues/detail?id=7065
		cellTable.redraw();
	}
}
