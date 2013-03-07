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

import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Dialog for editing an existing user.
 * 
 * @author Andrei Papancea
 * @author David Hovemeyer
 */
public class EditUserDialog extends DialogBox {
	private EditUserView editUserView;
	private Button editUserButton;
	private Button cancelButton;
	private ICallback<EditedUser> editUserCallback;

	/**
	 * Constructor.
	 * 
	 * @param user                  the {@link User} to edit
	 * @param userIsInstructor      true if the {@link User} is an instructor in the course
	 * @param sectionNum            the section number in which the user is registered
	 * @param verifyCurrentPassword true if the user must verify his/her current password
	 */
	public EditUserDialog(User user, boolean userIsInstructor, int sectionNum, boolean verifyCurrentPassword) {
		setGlassEnabled(true);
		
		FlowPanel panel = new FlowPanel();
		
		HTML passwordsMsg = new HTML("<div>Note: leave password fields blank to leave passwords unchanged</div>");
		panel.add(passwordsMsg);
		
		this.editUserView = new EditUserView(verifyCurrentPassword, false);
		editUserView.populate(user, sectionNum, userIsInstructor);
		panel.add(editUserView);
		
		FlowPanel buttonPanel = new FlowPanel();
		
		this.editUserButton = new Button("Edit User");
		editUserButton.setStyleName("cc-floatRightButton", true);
		buttonPanel.add(editUserButton);
		editUserButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (editUserView.checkValidity()) {
					editUserCallback.call(editUserView.getData());
				}
			}
		});
		
		this.cancelButton = new Button("Cancel");
		cancelButton.setStyleName("cc-floatRightButton", true);
		buttonPanel.add(cancelButton);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		panel.add(buttonPanel);
		
		add(panel);
	}
	
	/**
	 * @param editUserCallback the editUserCallback to set
	 */
	public void setEditUserCallback(ICallback<EditedUser> editUserCallback) {
		this.editUserCallback = editUserCallback;
	}
	
	public EditedUser getData() {
		return editUserView.getData();
	}
}
