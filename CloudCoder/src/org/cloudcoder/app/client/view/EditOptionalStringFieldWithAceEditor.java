// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2015, Andras Eisenberger
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

import org.cloudcoder.app.shared.model.ModelObjectField;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Edit a string field of a model object using an {@link AceEditor}.
 * 
 * @author David Hovemeyer
 */
public class EditOptionalStringFieldWithAceEditor<ModelObjectType>
		extends EditModelObjectField<ModelObjectType, String> {
	
	private class UI extends EditModelObjectFieldUI {
		private AceEditor editor;
		private boolean editorStarted;
		private AceEditorMode currentMode;

		public UI() {
			FlowPanel panel = new FlowPanel();
			panel.setStyleName("cc-fieldEditor", true);
			
			Label label = new Label(getDescription());
			label.setStyleName("cc-fieldEditorLabel", true);
			panel.add(label);
			
			panel.add(getErrorLabel());

			editor = new AceEditor();
			editor.setSize("600px", "300px");
			panel.add(editor);
			editorStarted = false;
			
			initWidget(panel);
		}

		public boolean isEditorStarted() {
			return editorStarted;
		}

		public void startEditor() {
			editor.startEditor();
			if (editorMode != null) {
				editor.setMode(editorMode);
				currentMode = editorMode;
			}
			editor.setTheme(editorTheme);
			editor.setFontSize("14px");
			editorStarted = true;
		}

		/**
		 * @param editorReadOnly whether the editor should be read-only now
		 */
		public void resetEditorReadOnly() {
			editor.setReadOnly(editorReadOnly);
		}

		public void setText(String text) {
			editor.setText(text);
		}

		public String getText() {
			return editor.getText();
		}

		public void resetEditorMode() {
			if (editorMode != null && editorMode != currentMode) {
				editor.setMode(editorMode);
				currentMode = editorMode;
				GWT.log("Changing editor mode to " + editorMode);
			}
		}

		public void resetEditorTheme() {
			if (editorTheme != null) {
				editor.setTheme(editorTheme);
			}
		}
	}

	private AceEditorMode editorMode;
	private AceEditorTheme editorTheme;
	private boolean editorReadOnly = false;
	private UI ui;

	/**
	 * Constructor.
	 * 
	 * @param desc human-readable description of field being edited
	 * @param field the {@link ModelObjectField} being edited
	 */
	public EditOptionalStringFieldWithAceEditor(String desc, ModelObjectField<? super ModelObjectType, String> field) {
		super(desc, field);
		this.ui = new UI();
	}
	
	/**
	 * Set the editor mode.
	 * 
	 * @param editorMode the editorMode to set
	 */
	public void setEditorMode(AceEditorMode editorMode) {
		this.editorMode = editorMode;
		if (ui.isEditorStarted()) {
			ui.resetEditorMode();
		}
	}
	
	/**
	 * Set the editor theme.
	 * 
	 * @param editorTheme the editorTheme to set
	 */
	public void setEditorTheme(AceEditorTheme editorTheme) {
		this.editorTheme = editorTheme;
		if (ui.isEditorStarted()) {
			ui.resetEditorTheme();
		}
	}
	
	/**
	 * Set the editor theme.
	 * 
	 * @param editorTheme the editorTheme to set
	 */
	public void setEditorReadOnly(boolean editorReadOnly) {
		this.editorReadOnly = editorReadOnly;
		if (ui.isEditorStarted()) {
			ui.resetEditorReadOnly();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#getUI()
	 */
	@Override
	public IsWidget getUI() {
		return ui;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#commit()
	 */
	@Override
	public void commit() {
		String text = ui.getText();
		if (text.length() > getModelObjectField().getSize()) {
			setCommitError(true);
			ui.setError("Value cannot be longer than " + getModelObjectField().getSize() + " characters");
		} else {
			setCommitError(false);
			ui.clearError();
			setField(text);
		}
	}

	@Override
	public void update() {
		if (!ui.isEditorStarted()) {
			// At this point, we'll assume that the UI has been added to the page DOM,
			// so it's safe to start the AceEditor.
			ui.startEditor();
		}
		
		ui.setText(getField());
	}
}
