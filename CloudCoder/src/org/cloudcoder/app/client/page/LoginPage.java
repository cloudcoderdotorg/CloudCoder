package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.PageStack;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Login page.
 */
public class LoginPage extends CloudCoderPage {
	/**
	 * UI class for LoginPage.
	 */
	private class UI extends LayoutPanel implements SessionObserver {
		private InlineLabel pageTitleLabel;
		private TextBox usernameTextBox;
		private PasswordTextBox passwordTextBox;
		private InlineLabel errorLabel;
		private InlineLabel loggingInLabel;
		
		public UI() {
			//setSize("640px", "480px");
			
			InlineLabel usernameLabel = new InlineLabel("User name:");
			add(usernameLabel);
			setWidgetLeftWidth(usernameLabel, 57.0, Unit.PX, 91.0, Unit.PX);
			setWidgetTopHeight(usernameLabel, 127.0, Unit.PX, 15.0, Unit.PX);
			
			usernameTextBox = new TextBox();
			usernameTextBox.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						passwordTextBox.setFocus(true);
					}
				}
			});
			add(usernameTextBox);
			setWidgetLeftWidth(usernameTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
			setWidgetTopHeight(usernameTextBox, 148.0, Unit.PX, 31.0, Unit.PX);
			
			InlineLabel passwordLabel = new InlineLabel("Password:");
			add(passwordLabel);
			setWidgetLeftWidth(passwordLabel, 57.0, Unit.PX, 91.0, Unit.PX);
			setWidgetTopHeight(passwordLabel, 185.0, Unit.PX, 15.0, Unit.PX);
			
			passwordTextBox = new PasswordTextBox();
			passwordTextBox.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						attemptLogin();
					}
				}
			});
			add(passwordTextBox);
			setWidgetLeftWidth(passwordTextBox, 57.0, Unit.PX, 200.0, Unit.PX);
			setWidgetTopHeight(passwordTextBox, 206.0, Unit.PX, 33.0, Unit.PX);
			
			Button loginButton = new Button("Log in");
			loginButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					attemptLogin();
				}
			});
			add(loginButton);
			setWidgetLeftWidth(loginButton, 176.0, Unit.PX, 81.0, Unit.PX);
			setWidgetTopHeight(loginButton, 245.0, Unit.PX, 27.0, Unit.PX);
			
			Image cloudCoderLogoImage = new Image(GWT.getModuleBaseURL() + "images/CloudCoderLogo-med.png");
			add(cloudCoderLogoImage);
			setWidgetLeftWidth(cloudCoderLogoImage, 301.0, Unit.PX, 240.0, Unit.PX);
			setWidgetTopHeight(cloudCoderLogoImage, 148.0, Unit.PX, 165.0, Unit.PX);
			
			InlineLabel promptLabel = new InlineLabel("Please enter your username and password.");
			add(promptLabel);
			setWidgetLeftWidth(promptLabel, 57.0, Unit.PX, 343.0, Unit.PX);
			setWidgetTopHeight(promptLabel, 328.0, Unit.PX, 20.0, Unit.PX);
			
			errorLabel = new InlineLabel("");
			errorLabel.setStylePrimaryName("cc-errorText");
			add(errorLabel);
			setWidgetLeftWidth(errorLabel, 57.0, Unit.PX, 484.0, Unit.PX);
			setWidgetTopHeight(errorLabel, 383.0, Unit.PX, 73.0, Unit.PX);
			
			pageTitleLabel = new InlineLabel("");
			pageTitleLabel.setStylePrimaryName("cc-pageTitle");
			add(pageTitleLabel);
			setWidgetLeftWidth(pageTitleLabel, 57.0, Unit.PX, 533.0, Unit.PX);
			setWidgetTopHeight(pageTitleLabel, 44.0, Unit.PX, 31.0, Unit.PX);
			
			InlineLabel welcomeLabel = new InlineLabel("Welcome to CloudCoder at");
			add(welcomeLabel);
			setWidgetLeftWidth(welcomeLabel, 57.0, Unit.PX, 313.0, Unit.PX);
			setWidgetTopHeight(welcomeLabel, 23.0, Unit.PX, 15.0, Unit.PX);
			
			loggingInLabel = new InlineLabel("");
			add(loggingInLabel);
			setWidgetLeftWidth(loggingInLabel, 57.0, Unit.PX, 343.0, Unit.PX);
			setWidgetTopHeight(loggingInLabel, 353.0, Unit.PX, 20.0, Unit.PX);
		}
		
		@Override
		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			// Load and display the institution name
			RPC.configurationSettingService.getConfigurationSettingValue(ConfigurationSettingName.PUB_TEXT_INSTITUTION, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					// FIXME: display error
				}
				
				@Override
				public void onSuccess(String result) {
					setPubTextInstitution(result);
				}
			});
		}

		protected void attemptLogin() {
			String username = usernameTextBox.getText();
			String password = passwordTextBox.getText();
			if (username.equals("") || password.equals("")) {
				errorLabel.setText("Please enter your username and password");
			}
			
			loggingInLabel.setText("Logging in...");
			RPC.loginService.login(username, password, new AsyncCallback<User>() {
				@Override
				public void onFailure(Throwable caught) {
					loggingInLabel.setText("");
					errorLabel.setText("Error communicating with server (are you connected to the network?)");
				}
				
				@Override
				public void onSuccess(User result) {
					if (result == null) {
						errorLabel.setText("Username and password not found");
					} else {
						// Successful login!
						getSession().add(result);
						getSession().get(PageStack.class).push(PageId.COURSES_AND_PROBLEMS);
					}
				}
			});
		}

		public void setPubTextInstitution(String result) {
			pageTitleLabel.setText(result);
			usernameTextBox.setFocus(true);
		}
	}

	private UI ui;

	@Override
	public void createWidget() {
		ui = new UI();
	}
	
	@Override
	public void activate() {
		ui.activate(getSession(), getSubscriptionRegistrar());
	}

	@Override
	public void deactivate() {
	}

	@Override
	public IsWidget getWidget() {
		return ui;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
	 */
	@Override
	public boolean isActivity() {
		return false;
	}
	
	@Override
	public PageId getPageId() {
		return PageId.LOGIN;
	}
	
	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		throw new IllegalStateException("Not an activity");
	}
}
