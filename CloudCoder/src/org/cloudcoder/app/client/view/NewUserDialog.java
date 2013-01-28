// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2012, Andrei Papancea
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

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Dialog for creating a new {@link User} account in the context
 * of a {@link Course}.
 * 
 * @author Andrei Papancea
 * @author David Hovemeyer
 */
public class NewUserDialog extends DialogBox {
	private EditUserView editUserView;
	private Button addUserButton;
	private Button cancelButton;
	private ICallback<EditedUser> addUserCallback;

	public NewUserDialog() {
		setGlassEnabled(true);
		
		FlowPanel panel = new FlowPanel();
		
		this.editUserView = new EditUserView(false);
		panel.add(editUserView);

		FlowPanel buttonPanel = new FlowPanel();
		
		this.addUserButton = new Button("Add User");
		addUserButton.setStyleName("cc-floatRightButton", true);
		buttonPanel.add(addUserButton);
		addUserButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (editUserView.checkValidity()) {
					addUserCallback.call(editUserView.getData());
				}
			}
		});
		
		this.cancelButton = new Button("Cancel");
		cancelButton.setStyleName("cc-floatRightButton", true);
		buttonPanel.add(cancelButton);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				NewUserDialog.this.hide();
			}
		});
		
		panel.add(buttonPanel);
		
		add(panel);
	}
	
	/**
	 * @param addUserCallback the addUserCallback to set
	 */
	public void setAddUserCallback(ICallback<EditedUser> addUserCallback) {
		this.addUserCallback = addUserCallback;
	}

	/**
	 * Return the {@link EditedUser}.
	 * 
	 * @return the {@link EditedUser}
	 */
	public EditedUser getData() {
		return editUserView.getData();
	}
}
