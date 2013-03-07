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

import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Dialog for importing a problem from the exercise repository.
 * 
 * @author David Hovemeyer
 */
public class ImportProblemDialog extends DialogBox {
	
	private Label exerciseHashLabel;
	private TextBox exerciseHashTextBox;
	private ICallback<ProblemAndTestCaseList> resultCallback;
	private Button cancelButton;
	private Button importButton;
	private Course course;
	
	/**
	 * Constructor.
	 */
	public ImportProblemDialog() {
		setText("Import problem from exercise repository");
		setGlassEnabled(true);
		
		FlowPanel panel = new FlowPanel();
		
		HTML html = new HTML(
				"Please enter the hash code of the exercise you would like to " +
				"import from the repository."
		);
		html.setWidth("480px");
		panel.add(html);
		
		panel.add(new InlineHTML("<br />"));
		
		this.exerciseHashLabel = new Label("Exercise hash:");
		panel.add(exerciseHashLabel);
		
		this.exerciseHashTextBox = new TextBox();
		exerciseHashTextBox.setWidth("400px");
		panel.add(exerciseHashTextBox);
		
		panel.add(new InlineHTML("<br />"));
		
		this.cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		panel.add(cancelButton);
		
		panel.add(new InlineHTML(" "));
		
		this.importButton = new Button("Import exercise!");
		importButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleImportProblem();
			}
		});
		panel.add(importButton);
		
		add(panel);
	}
	
	/**
	 * Set the result callback to be called when the RPC operation to import
	 * the problem completes.  The callback's {@link ICallback#call(Object) call}
	 * method will be called either either the {@link ProblemAndTestCaseList exercise} imported
	 * successfully, or null if the exercise was not found.
	 * 
	 * @param resultCallback the result callback to set
	 */
	public void setResultCallback(ICallback<ProblemAndTestCaseList> resultCallback) {
		this.resultCallback = resultCallback;
	}
	
	/**
	 * Set the course to import the exercise into.
	 * 
	 * @param course the course to import the exercise into
	 */
	public void setCourse(Course course) {
		this.course = course;
	}

	protected void handleImportProblem() {
		RPC.getCoursesAndProblemsService.importExercise(course, exerciseHashTextBox.getText(), new AsyncCallback<ProblemAndTestCaseList>() {
			@Override
			public void onSuccess(ProblemAndTestCaseList result) {
				hide();
				resultCallback.call(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				hide();
				resultCallback.call(null);
				// FIXME: would be nice to convey details about the failure back to the page UI
			}
		});
	}
}
