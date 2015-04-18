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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
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
		private CheckBox checkBox;
		private AceEditor editor;
		private boolean editorStarted;
		private AceEditorMode currentMode;

		public UI(String checkboxLabel) {
			FlowPanel panel = new FlowPanel();
			panel.setStyleName("cc-fieldEditor", true);
			
			Label label = new Label(getDescription());
			label.setStyleName("cc-fieldEditorLabel", true);
			panel.add(label);
			
			panel.add(getErrorLabel());

			this.checkBox = new CheckBox(checkboxLabel);

			this.editor = new AceEditor();
			editor.setSize("600px", "300px");
			
			checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if(event.getValue()) {
						editor.setReadOnly(false);
						editor.setTheme(editorEnabledTheme);
					} else {
						editor.setReadOnly(true);
						editor.setTheme(editorDisabledTheme);
					}
					
				}
			});
			
			panel.add(checkBox);
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
			
			resetEditorTheme();
			
			if(checkBox.getValue()) {
				editor.setReadOnly(false);
			} else {
				editor.setReadOnly(true);
			}
			
			editor.setFontSize("14px");
			editorStarted = true;
		}

		/**
		 * @param editorReadOnly whether the editor should be read-only now
		 */
		public void resetEnabled() {
			checkBox.setEnabled(enabled);
		}

		public void setText(String text) {
			editor.setText(text);
		}

		public String getText() {
			return editor.getText();
		}

		public void setCheckBox(boolean value) {
			checkBox.setValue(value, true);
		}

		public boolean getCheckBox() {
			return checkBox.getValue();
		}

		public void resetEditorMode() {
			if (editorMode != null && editorMode != currentMode) {
				editor.setMode(editorMode);
				currentMode = editorMode;
				GWT.log("Changing editor mode to " + editorMode);
			}
		}

		public void resetEditorTheme() {
			if(getCheckBox()) {
				if (editorEnabledTheme != null) {
					editor.setTheme(editorEnabledTheme);
				}
			} else {
				if (editorDisabledTheme != null) {
					editor.setTheme(editorDisabledTheme);
				}
			}
		}
	}

	private AceEditorMode editorMode;
	private AceEditorTheme editorEnabledTheme;
	private AceEditorTheme editorDisabledTheme;
	private boolean enabled = true;
	private UI ui;

	/**
	 * Constructor.
	 * 
	 * @param desc human-readable description of field being edited
	 * @param field the {@link ModelObjectField} being edited
	 */
	public EditOptionalStringFieldWithAceEditor(String desc, String checkboxLabel, ModelObjectField<? super ModelObjectType, String> field) {
		super(desc, field);
		this.ui = new UI(checkboxLabel);
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
	public void setEditorThemes(AceEditorTheme editorEnabledTheme, AceEditorTheme editorDisabledTheme) {
		this.editorEnabledTheme = editorEnabledTheme;
		this.editorDisabledTheme = editorDisabledTheme;
		if (ui.isEditorStarted()) {
			ui.resetEditorTheme();
		}
	}
	
	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			this.enabled = enabled;
			ui.resetEnabled();
			if(enabled) {
				if(!ui.getText().trim().isEmpty()) {
					ui.setCheckBox(true);
				}
			} else {
				ui.setCheckBox(false);
			}
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
		String text;
		if(ui.getCheckBox()) {
			text = ui.getText();
		} else {
			text = "";
		}
		
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
		
		String field = getField();
		ui.setText(getField());
		if(!field.isEmpty()) {
			ui.setCheckBox(true);
		}
	}
}
