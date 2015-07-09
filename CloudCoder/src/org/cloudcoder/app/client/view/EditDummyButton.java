// CloudCoder - a web-based pedagogical programming environment
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

import org.cloudcoder.app.client.page.EditProblemPage;
import org.cloudcoder.app.shared.model.ModelObjectField;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Implementation of {@link EditModelObjectField} which does not edit any
 * field. It extends {@link EditModelObjectField}, because only these can be
 * on the {@link EditProblemPage} UI.
 * 
 * @author Andras Eisenberger
 */
public abstract class EditDummyButton<ModelObjectType, FieldType>
		extends EditModelObjectField<ModelObjectType, FieldType> {
	
	private class UI extends Composite {
		private Button button;

		public UI() {
			FlowPanel panel = new FlowPanel();
			panel.setStyleName("cc-fieldEditor");
			
			this.button = new Button(getDescription(), new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        onButtonClick();
		      }
			});
			button.setStyleName("cc-emphButton");
			panel.add(button);
			
			initWidget(panel);
		}
		
		public void setEnabled(boolean enabled) {
			button.setEnabled(enabled);
		}
	}

	private UI ui;

	/**
	 * Constructor.
	 * 
	 * @param desc the human-readable description of the field, which is also
	 * the text of the button
	 * @param field any of the fields, it doesn't touch it by default
	 */
	public EditDummyButton(String desc, ModelObjectField<? super ModelObjectType, FieldType> field) {
		super(desc, field);
		ui = new UI();
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
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#update()
	 */
	@Override
	public void update() {
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.view.EditModelObjectField#isCommitError()
	 */
	@Override
	public boolean isCommitError() {
		// It's connected to no field, so no
		return false;
	}
	
	/**
	 * Do this when the button is clicked
	 */
	public abstract void onButtonClick();
	
	/**
	 * @param enabled whether the button should be enabled
	 */
	public void setEnabled(boolean enabled) {
		ui.setEnabled(enabled);
	}
}
