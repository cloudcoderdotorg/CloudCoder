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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

/**
 * View for editing an {@link EditedUser} object.
 * This view can be used for creating a new user, and for editing
 * an existing user.
 * 
 * @author Andrei Papancea
 * @author David Hovemeyer
 */
public class EditUserView extends Composite {
	private List<TextBox> validateNonEmpty;
	private TextBox username;
	private TextBox firstname;
	private TextBox lastname;
	private TextBox email;
	private TextBox website;
	private PasswordTextBox currentPassword;
	private PasswordTextBox passwd;
	private PasswordTextBox passwd2;
	private TextBox section;
	private RadioButton studentAccountButton;
	private RadioButton instructorAccountButton;
	private Label errorLabel;

	/**
	 * Constructor.
	 * 
	 * @param verifyCurrentPassword if the view should prompt the user to enter
	 *                              his/her current password
	 */
	public EditUserView(boolean verifyCurrentPassword) {
		FlowPanel holder = new FlowPanel();
		
		this.validateNonEmpty = new ArrayList<TextBox>();
		
		// username
		username = addTextBox(holder, "Username");

		// firstname
		firstname = addTextBox(holder, "First name");

		// lastname
		lastname = addTextBox(holder, "Last name");

		// email
		email = addTextBox(holder, "Email");

		// website
		website = addTextBox(holder, "Website", false);
		
		// verify current password
		if (verifyCurrentPassword) {
			currentPassword = addPasswordTextBox(holder, "Current password");
		}

		// password
		passwd = addPasswordTextBox(holder, "Password");

		// re-enter password
		passwd2 = addPasswordTextBox(holder, "Re-enter password");
		
		// section
		section = addTextBox(holder, "Section");

		// radio button for the account type
		holder.add(new Label("Account type"));
		this.studentAccountButton = new RadioButton("account-type","student");
		studentAccountButton.setValue(true);
		this.instructorAccountButton = new RadioButton("account-type","instructor");
		holder.add(studentAccountButton);
		holder.add(instructorAccountButton);

		// label for displaying validation errors
		this.errorLabel = new Label(" ");
		errorLabel.setStyleName("cc-errorText", true);
		holder.add(errorLabel);
		
		initWidget(holder);
	}
	
	private TextBox addTextBox(FlowPanel holder, String labelText) {
		return addTextBox(holder, labelText, true);
	}

	private TextBox addTextBox(FlowPanel holder, String labelText, boolean required) {
		holder.add(new Label(labelText));
		TextBox textBox = new TextBox();
		holder.add(textBox);
		if (required) {
			validateNonEmpty.add(textBox);
		}
		return textBox;
	}

	private PasswordTextBox addPasswordTextBox(FlowPanel holder, String labelText) {
		holder.add(new Label(labelText));
		PasswordTextBox textBox = new PasswordTextBox();
		holder.add(textBox);
		validateNonEmpty.add(textBox);
		return textBox;
	}
	
	public boolean checkValidity() {
		// Clear error indicators
		errorLabel.setText(" ");
		boolean invalid = false;
		for (TextBox textBox : validateNonEmpty) {
			textBox.removeStyleName("cc-invalid");
		}
		
		// Check that all required fields were entered
		for (TextBox textBox : validateNonEmpty) {
			if (textBox.getValue().trim().equals("")) {
				invalid = true;
				textBox.setStyleName("cc-invalid");
			}
		}
		if (invalid) {
			errorLabel.setText("Please enter all required fields");
			return false;
		}
		
		// Check that passwords match
		if (!passwd.getValue().equals(passwd2.getValue())) {
			passwd.setStyleName("cc-invalid", true);
			passwd2.setStyleName("cc-invalid", true);
			errorLabel.setText("Passwords do not match");
			return false;
		}
		
		// Section must be numeric
		try {
			Integer.parseInt(section.getValue().trim());
		} catch (NumberFormatException e) {
			section.addStyleName("cc-invalid");
			errorLabel.setText("Section must be numeric");
			return false;
		}
		
		return true;
	}
	
	public EditedUser getData() {
		User user = new User();
		user.setUsername(username.getValue().trim());
		user.setFirstname(firstname.getValue().trim());
		user.setLastname(lastname.getValue().trim());
		user.setEmail(email.getValue().trim());
		user.setWebsite(website.getValue().trim());
		
		EditedUser editedUser = new EditedUser();
		editedUser.setUser(user);
		if (currentPassword != null) {
			editedUser.setCurrentPassword(currentPassword.getValue().trim());
		}
		editedUser.setPassword(passwd.getValue());
		editedUser.setSection(Integer.parseInt(section.getValue().trim()));
		editedUser.setRegistrationType(instructorAccountButton.getValue() ? CourseRegistrationType.INSTRUCTOR : CourseRegistrationType.STUDENT);
		
		return editedUser;
	}
}
