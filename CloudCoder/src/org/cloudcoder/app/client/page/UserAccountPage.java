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

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.PageStack;
import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.UserAccountView;
import org.cloudcoder.app.client.view.UserProgressListView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author jspacco
 * @author apapance
 *
 */
public class UserAccountPage extends CloudCoderPage
{
    private static final long serialVersionUID = 1L;
    
    private enum ButtonPanelAction {
    	EDIT("Edit account"),
    	VIEW_PROGRESS("View progress in course");
        
        private String name;
        
        private ButtonPanelAction(String name) {
            this.name = name;
        }
        
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        
    }
    private class UI extends Composite implements SessionObserver, Subscriber {
        private static final double USERS_BUTTON_BAR_HEIGHT_PX = 28.0;

        private PageNavPanel pageNavPanel;
        private String rawCourseTitle;
        private Label courseLabel;
        private Button[] userManagementButtons;
        private UserAccountView userAccountView;
        private StatusMessageView statusMessageView;
        
        public UI() {
            DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
            
            // Create a north panel with course info and a PageNavPanel
            LayoutPanel northPanel = new LayoutPanel();
            this.courseLabel = new Label();
            northPanel.add(courseLabel);
            northPanel.setWidgetLeftRight(courseLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
            northPanel.setWidgetTopHeight(courseLabel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);
            courseLabel.setStyleName("cc-courseLabel");
            
            this.pageNavPanel = new PageNavPanel();
            northPanel.add(pageNavPanel);
            northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
            northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);
            
            dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX);
            
            // Create a center panel with user button panel and list of users 
            // registered for the given course.
            // Can eventually put other stuff here too.
            LayoutPanel centerPanel = new LayoutPanel();
            
            // Create a button panel with buttons for problem-related actions
            // (new problem, edit problem, make visible, make invisible, quiz, share)
            FlowPanel userButtonPanel = new FlowPanel();
            ButtonPanelAction[] actions = ButtonPanelAction.values();
            userManagementButtons = new Button[actions.length];
            for (final ButtonPanelAction action : actions) {
                final Button button = new Button(action.getName());
                userManagementButtons[action.ordinal()] = button;
                button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        switch (action) {
                        case EDIT:
                            handleEditUser(event);
                            break;
                            
                        case VIEW_PROGRESS:
                            handleUserProgress(event);
                            break;
                        }                    }
                });
                button.setEnabled(false);
                userButtonPanel.add(button);
            }
            
            centerPanel.add(userButtonPanel);
            centerPanel.setWidgetTopHeight(userButtonPanel, 0.0, Unit.PX, 28.0, Unit.PX);
            centerPanel.setWidgetLeftRight(userButtonPanel, 0.0, Unit.PX, 0.0, Unit.PX);
            
            // Create users list
            this.userAccountView = new UserAccountView();
            centerPanel.add(userAccountView);
            centerPanel.setWidgetTopBottom(userAccountView, USERS_BUTTON_BAR_HEIGHT_PX, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
            centerPanel.setWidgetLeftRight(userAccountView, 0.0, Unit.PX, 0.0, Unit.PX);
            
            // Create a StatusMessageView
            this.statusMessageView = new StatusMessageView();
            centerPanel.add(statusMessageView);
            centerPanel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
            centerPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
            
            dockLayoutPanel.add(centerPanel);
            
            initWidget(dockLayoutPanel);
        }
        
        /**
         * @author Andrei Papancea
         *
         * View a particular user's progress throughout the course.
         * The pop-up will display the problems that the user has started
         * and their status (complete/incomplete, num_tests_passed/num_tests_total).
         * 
         */
        private class UserProgressPopupPanel extends PopupPanel{
        	
        	public UserProgressPopupPanel(final Widget widget, final User user, 
                    final Course course, final CourseRegistrationType originalType, final Session session)
            {
               super(true);
               
               VerticalPanel vp = new VerticalPanel();
               
               setWidget(vp);
               
               vp.setWidth("600px");
               
               vp.add(new HTML("Problem statistics for <b>"+
            		   			user.getFirstname()+" "+user.getLastname()+" ("+
            		   			user.getUsername()+")</b><br /><br />"));
               
               UserProgressListView myGrid = new UserProgressListView(user);
               myGrid.activate(session, getSubscriptionRegistrar());
               vp.add(myGrid);
            }        	
        }
  
        private class EditUserPopupPanel extends PopupPanel{

            public EditUserPopupPanel(final Widget widget, final User user, 
                    final Course course, final CourseRegistrationType originalType)
            {
               super(true);
               
               VerticalPanel vp = new VerticalPanel();

               setWidget(vp);
               
               final FormPanel form = new FormPanel();
               // We won't actually submit the form to a servlet
               // instead we intercept the form fields
               // and make an async call

               // TODO are these style file hooks?
               form.addStyleName("table-center");
               form.addStyleName("demo-FormPanel");

               VerticalPanel holder = new VerticalPanel();

               holder.add(new HTML(new SafeHtmlBuilder().appendEscapedLines("Change the fields you want to edit.\n" +
               		"Any fields left blank will be unchanged\n\n").toSafeHtml()));
               
               // username
               holder.add(new HTML("Username:<br /><b>"+user.getUsername()+"</b><br /><br />"));
               
               // firstname
               holder.add(new Label("Firstname"));
               final TextBox firstname = new TextBox();
               firstname.setName("firstname");
               firstname.setValue(user.getFirstname());
               holder.add(firstname);
               
               // lastname
               holder.add(new Label("Lastname"));
               final TextBox lastname = new TextBox();
               lastname.setName("lastname");
               lastname.setValue(user.getLastname());
               holder.add(lastname);
               
               // email
               holder.add(new Label("Email"));
               final TextBox email = new TextBox();
               email.setName("email");
               email.setValue(user.getEmail());
               holder.add(email);
               
               // password
               holder.add(new Label("Password"));
               final PasswordTextBox passwd = new PasswordTextBox();
               passwd.setName("passwd");
               holder.add(passwd);
               
               // re-enter password
               holder.add(new Label("re-enter Password"));
               final PasswordTextBox passwd2 = new PasswordTextBox();
               passwd2.setName("passwd2");
               holder.add(passwd2);
               
               String consent=user.getConsent();
               if (consent.equals(User.GIVEN_CONSENT)) {
                   holder.add(new Label("You are allowing the anonymous use of your coding history for research purposes. Thank you!"));
               } else if (consent.equals(User.NO_CONSENT)){
                   holder.add(new Label("You are currently NOT allowing the anonymous use of your coding history for research purposes."));
               } else {
                   holder.add(new Label("You have not yet decided whether we can use your anonymized coding history for research purposes."));
                   holder.add(new Label("Please select one of the following two options"));
               }

               // radio button for the account type
               holder.add(new Label(""));
               holder.add(new Label("Allow anonymous collection of your coding data:"));
               final RadioButton consentButton = new RadioButton("consent","Allow");
               final RadioButton noConsentButton = new RadioButton("consent","Do NOT allow");
               if (consent.equals(User.GIVEN_CONSENT)) {
                   consentButton.setValue(true);
               } else if (consent.equals(User.NO_CONSENT)){
                   noConsentButton.setValue(true);
               }
               
               holder.add(consentButton);
               holder.add(noConsentButton);
               
               
               
               form.add(holder);
               vp.add(form);
               
               final PopupPanel panelCopy=this;
               
               holder.add(new Button("Edit user", new ClickHandler() {
                   @Override
                   public void onClick(ClickEvent event) {
                       //This is more like a fake form
                       //we're not submitting it to a server-side servlet
                       GWT.log("edit user submit clicked");
                       final User user=getSession().get(User.class);
                       
                       String consent="";
                       if (consentButton.getValue()) {
                           consent=User.GIVEN_CONSENT;
                       } else if (noConsentButton.getValue()) {
                           consent=User.NO_CONSENT;
                       }
                       
                       if (user.getFirstname().equals(firstname.getValue()) ||
                               user.getLastname().equals(lastname.getValue()) ||
                               user.getEmail().equals(email.getValue()) ||
                               user.getConsent().equals(consent) ||
                               passwd.getValue().length()>0)
                       {
                           if (!passwd.getValue().equals(passwd2.getValue())) {
                               // TODO: User Daveho's warning system
                               Window.alert("Passwords do no match");
                               return;
                           }
                           if (passwd.getValue().length()==60) {
                               Window.alert("Passwords cannot be 60 characters long");
                               return;
                           }
                           // set the new fields to be saved into the DB
                           user.setFirstname(firstname.getValue());
                           user.setLastname(lastname.getValue());
                           user.setEmail(email.getValue());
                           user.setConsent(consent);
                           if (passwd.getValue().length()>0) {
                               user.setPasswordHash(passwd.getValue());
                           }
                           // at least one field was edited
                           GWT.log("user id is "+user.getId());
                           GWT.log("username from the session is "+user.getUsername());
                           RPC.usersService.editUser(user,
                                   new AsyncCallback<Boolean>()
                           { 
                               @Override
                               public void onSuccess(Boolean result) {
                                   GWT.log("Edited "+user.getUsername()+" in course "+rawCourseTitle);
                                   panelCopy.hide();
                                   Window.alert("Successfully edited user record");
                                   reloadUser();
                               }

                               @Override
                               public void onFailure(Throwable caught) {
                                   GWT.log("Failed to edit student");
                                   Window.alert("Unable to edit "+user.getUsername()+" in course "+rawCourseTitle);
                               }
                           });
                       } else {
                           panelCopy.hide();
                           Window.alert("Nothing was changed");
                       }
                   }
               }));
               
            }
        }
        
        /* (non-Javadoc)
         * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
         */
        public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
            session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
            
            // Activate views
            pageNavPanel.setBackHandler(new PageBackHandler(session));
            pageNavPanel.setLogoutHandler(new LogoutHandler(session));
            userAccountView.activate(session, subscriptionRegistrar);
            statusMessageView.activate(session, subscriptionRegistrar);
            
            // The session should contain a course
            Course course = getCurrentCourse();
            rawCourseTitle=course.getName()+" - "+course.getTitle();
            courseLabel.setText(rawCourseTitle);
            session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
        }
        
        @Override
        public void eventOccurred(Object key, Publisher publisher, Object hint) {
            if (key == Session.Event.ADDED_OBJECT && (hint instanceof User)) {
                onSelectUser((User) hint);
            } else if (key == Session.Event.ADDED_OBJECT && (hint instanceof CourseSelection)) {
                
            }
        }
        
        private void reloadUser() {
            userAccountView.loadUser(getSession());
        }
        
        private void onSelectUser(User user) {
            // Problem selected: enable/disable buttons appropriately
            userManagementButtons[ButtonPanelAction.EDIT.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.VIEW_PROGRESS.ordinal()].setEnabled(true);
        }
        
        private void handleEditUser(ClickEvent event) {
            GWT.log("handle edit user");
            final User chosen = getSession().get(User.class);
            final Course course = getCurrentCourse();
            //TODO get the course type?
            //TODO wtf is the in the user record and how does it get there?
            CourseRegistrationType type=null;
            Widget w = (Widget)event.getSource();
            EditUserPopupPanel pop = new EditUserPopupPanel(w, 
                    chosen, 
                    course,
                    type);
            pop.center();
            pop.setGlassEnabled(true);
            pop.show();
        }
        
        private void handleUserProgress(ClickEvent event) {
            GWT.log("handle user progress");
            final User chosen = getSession().get(User.class);
            final Course course = getCurrentCourse();
            
            GWT.log("handling user "+chosen.getUsername());
            //TODO get the course type?
            //TODO wtf is the in the user record and how does it get there?
            CourseRegistrationType type=null;
            Widget w = (Widget)event.getSource();
            UserProgressPopupPanel pop = new UserProgressPopupPanel(w, 
                    chosen, 
                    course,
                    type,
                    getSession());
            pop.center();
            pop.setGlassEnabled(true);
            pop.show();
        }
    }
    
    private UI ui;

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
     */
    @Override
    public void createWidget() {
        ui = new UI();
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
     */
    @Override
    public void activate() {
        ui.activate(getSession(), getSubscriptionRegistrar());
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#deactivate()
     */
    @Override
    public void deactivate() {
        getSubscriptionRegistrar().cancelAllSubscriptions();
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#getWidget()
     */
    @Override
    public IsWidget getWidget() {
        return ui;
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
     */
    @Override
    public boolean isActivity() {
        return true;
    }
    
    @Override
    public PageId getPageId() {
    	return PageId.USER_ACCOUNT;
    }
    
    @Override
    public void initDefaultPageStack(PageStack pageStack) {
    	pageStack.push(PageId.COURSES_AND_PROBLEMS);
    }
}
