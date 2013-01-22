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

import org.cloudcoder.app.client.model.CourseSelection;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.UserAdminUsersListView;
import org.cloudcoder.app.client.view.UserProgressListView;
import org.cloudcoder.app.client.view.ViewUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
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
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
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
 * 
 * TODO
 *    Fix the slowness of bulk registration 
 *
 */
public class UserAdminPage extends CloudCoderPage
{
    private static final long serialVersionUID = 1L;
    
    private enum ButtonPanelAction {
        NEW("Add new user"),
        EDIT("Edit user"),
        DELETE("Delete user"),
        REGISTER_USERS("Register users"),
        VIEW_ALL_PROGRESS("View All Progress"),
        VIEW_USER_PROGRESS("View User Progress");
        
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
        
        public boolean isEnabledByDefault() {
            return this == NEW || this == REGISTER_USERS || this == VIEW_ALL_PROGRESS;
        }
    }
    private class UI extends Composite implements SessionObserver, Subscriber {
        private static final double USERS_BUTTON_BAR_HEIGHT_PX = 28.0;

        private PageNavPanel pageNavPanel;
        private String rawCourseTitle;
        private Label courseLabel;
        private int courseId;
        private Button[] userManagementButtons;
        private UserAdminUsersListView userAdminUsersListView;
        private StatusMessageView statusMessageView;
        private AddUserPopupPanel popupPanel;
        
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
                        case NEW:
                            handleNewUser(event);
                            break;

                        case EDIT:
                            handleEditUser(event);
                            break;
                            
                        case REGISTER_USERS:
                            handleRegisterNewUsers(event);
                            break;
                            
                        case DELETE:
                            handleDeleteUser();
                            break;
                            
                        case VIEW_ALL_PROGRESS:
                            handleDeleteUser();
                            break;
                            
                        case VIEW_USER_PROGRESS:
                            handleUserProgress(event);
                            break;
                        }                    }
                });
                button.setEnabled(action.isEnabledByDefault());
                userButtonPanel.add(button);
            }
            
            centerPanel.add(userButtonPanel);
            centerPanel.setWidgetTopHeight(userButtonPanel, 0.0, Unit.PX, 28.0, Unit.PX);
            centerPanel.setWidgetLeftRight(userButtonPanel, 0.0, Unit.PX, 0.0, Unit.PX);
            
            // Create users list
            this.userAdminUsersListView = new UserAdminUsersListView();
            centerPanel.add(userAdminUsersListView);
            centerPanel.setWidgetTopBottom(userAdminUsersListView, USERS_BUTTON_BAR_HEIGHT_PX, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
            centerPanel.setWidgetLeftRight(userAdminUsersListView, 0.0, Unit.PX, 0.0, Unit.PX);
            
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
               setGlassEnabled(true);

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
        
        private class AddUserPopupPanel extends PopupPanel{

            public AddUserPopupPanel(final Widget widget, final int courseId){
               super(true);
               
               VerticalPanel vp = new VerticalPanel();
               setWidget(vp);
               final FormPanel form = new FormPanel();
               // copy of the current instance (this)
               // to be used by inner classes
               final PopupPanel panelCopy=this;
               panelCopy.setGlassEnabled(true);
               
               // We actually perform the submit asynchronously
               //form.setEncoding(FormPanel.ENCODING_MULTIPART);
               //form.setMethod(FormPanel.METHOD_POST);
              
               // TODO are these style file hooks?
               form.addStyleName("table-center");
               form.addStyleName("demo-FormPanel");

               VerticalPanel holder = new VerticalPanel();

               // username
               holder.add(new Label("Username"));
               final TextBox username = new TextBox();
               username.setName("username");
               holder.add(username);
               
               // firstname
               holder.add(new Label("Firstname"));
               final TextBox firstname = new TextBox();
               firstname.setName("firstname");
               holder.add(firstname);
               
               // lastname
               holder.add(new Label("Lastname"));
               final TextBox lastname = new TextBox();
               lastname.setName("lastname");
               holder.add(lastname);
               
               // email
               holder.add(new Label("Email"));
               final TextBox email = new TextBox();
               email.setName("email");
               holder.add(email);
               
               // website
               holder.add(new Label("Website"));
               final TextBox website = new TextBox();
               email.setName("website");
               holder.add(website);

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
               
               // radio button for the account type
               holder.add(new Label("Account type"));
               final RadioButton studentAccountButton = new RadioButton("account-type","student");
               studentAccountButton.setValue(true);
               final RadioButton instructorAccountButton = new RadioButton("account-type","instructor");
               holder.add(studentAccountButton);
               holder.add(instructorAccountButton);
               
               holder.add(new Button("Add user", new ClickHandler() {
                   @Override
                   public void onClick(ClickEvent event) {
                       //This is more like a fake form
                       //we're not submitting it to a server-side servlet
                       GWT.log("Add user button clicked");
                       final User user=new User();
                       if (username.getValue().equals("")) {
                           Window.alert("Username cannot be empty");
                           return;
                       }
                       user.setUsername(username.getValue());
                       if (firstname.getValue().equals("")) {
                           Window.alert("Firstname cannot be empty");
                           return;
                       }
                       user.setFirstname(firstname.getValue());
                       if (lastname.getValue().equals("")) {
                           Window.alert("Lastname cannot be empty");
                           return;
                       }
                       user.setLastname(lastname.getValue());
                       if (email.getValue().equals("")) {
                           Window.alert("Email cannot be empty");
                           return;
                       }
                       user.setEmail(email.getValue());
                       if (passwd.getValue().equals("")) {
                           Window.alert("Password cannot be empty");
                           return;
                       }
                       // website is allowed to be empty, but not null
                       user.setWebsite(website.getValue());
                       if (!passwd.getValue().equals(passwd2.getValue())) {
                           Window.alert("Passwords do no match");
                           return;
                       }
                       user.setPasswordHash(passwd.getValue());
                       user.setWebsite("");
                       
                       CourseRegistrationType type=CourseRegistrationType.STUDENT;
                       if (instructorAccountButton.getValue()) {
                           type=CourseRegistrationType.INSTRUCTOR;
                       }
                       //TODO add support for sections
                       int section=101;
                       
                       GWT.log("courseId is " +courseId);
                       RPC.usersService.addUserToCourse(user, courseId, type, section, new AsyncCallback<Boolean>() {
                        
                           @Override
                           public void onSuccess(Boolean result) {
                               GWT.log("Added "+user.getUsername()+" to course "+rawCourseTitle);
                               panelCopy.hide(true);
                               reloadUsers();
                               getSession().add(StatusMessage.goodNews("Added "+user.getUsername()+" to course "+rawCourseTitle));
                           }

                           @Override
                           public void onFailure(Throwable caught) {
                               GWT.log("Failed to add student");
                               getSession().add(StatusMessage.error("Unable to add "+user.getUsername()+" to course"));
                           }
                       });
                   }
               }));
               form.add(holder);

               vp.add(form);
            }
        }
  
        private class EditUserPopupPanel extends PopupPanel{

            public EditUserPopupPanel(final Widget widget, final User user, 
                    final Course course, final CourseRegistrationType originalType)
            {
               super(true);
               setGlassEnabled(true);
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
               		"Any fields left blank will be unchanged\n" +
               		"Leave password fields blank to leave password unchanged.").toSafeHtml()));
               
               // username
               holder.add(new Label("Username"));
               final TextBox username = new TextBox();
               username.setName("username");
               username.setValue(user.getUsername());
               holder.add(username);
               
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
               
               // radio button for the account type
               holder.add(new Label("Account type"));
               final RadioButton studentAccountButton = new RadioButton("type","student");
               studentAccountButton.setValue(true);
               final RadioButton instructorAccountButton = new RadioButton("type","instructor");
               holder.add(studentAccountButton);
               holder.add(instructorAccountButton);
               
               form.add(holder);
               vp.add(form);
               
               final PopupPanel panelCopy=this;
               
               holder.add(new Button("Edit user", new ClickHandler() {
                   @Override
                   public void onClick(ClickEvent event) {
                       //This is more like a fake form
                       //we're not submitting it to a server-side servlet
                       GWT.log("edit user submit clicked");
                       final User user=userAdminUsersListView.getSelectedUser();
                       
                       //TODO add support for editing registration type
                       CourseRegistrationType type=CourseRegistrationType.STUDENT;
                       if (instructorAccountButton.getValue()) {
                           type=CourseRegistrationType.INSTRUCTOR;
                       }
                       //TODO add support for sections
                       int section=101;
                       
                       if (!user.getUsername().equals(username.getValue()) ||
                               user.getFirstname().equals(firstname.getValue()) ||
                               user.getLastname().equals(lastname.getValue()) ||
                               user.getEmail().equals(email.getValue()) ||
                               passwd.getValue().length()>0)
                       {
                           if (!passwd.getValue().equals(passwd2.getValue())) {
                               Window.alert("Passwords do no match");
                               return;
                           }
                           if (passwd.getValue().length()==60) {
                               Window.alert("Passwords cannot be 60 characters long");
                               return;
                           }
                           // set the new fields to be saved into the DB
                           user.setUsername(username.getValue());
                           user.setFirstname(firstname.getValue());
                           user.setLastname(lastname.getValue());
                           user.setEmail(email.getValue());
                           if (passwd.getValue().length()>0) {
                               // it's sort of a hack but if a new password is set
                               // the backend will figure out that it's not a hash
                               // and then hash it to storage into the DB.
                               // The backend figures this out by checking
                               // if the password is exactly 60 characters long,
                               // which is why 60 char long passwords are illegal.
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
                                   reloadUsers();
                                   getSession().add(StatusMessage.goodNews("Successfully edited user record"));
                               }

                               @Override
                               public void onFailure(Throwable caught) {
                                   GWT.log("Failed to edit student");
                                   getSession().add(StatusMessage.error("Unable to edit "+user.getUsername()+" in course "+rawCourseTitle));
                               }
                           });
                       } else {
                           panelCopy.hide();
                           getSession().add(StatusMessage.information("Nothing was changed"));
                       }
                   }
               }));
               
            }
        }
        
        private class RegisterUsersPopupPanel extends PopupPanel{
            public RegisterUsersPopupPanel(final Widget widget, final int courseId) 
            {
               super(true);
               
               final VerticalPanel vp = new VerticalPanel();
               final PopupPanel panelCopy=this;
               //final PopupPanel loadingPopupPanel=new LoadingPopupPanel();
               
               panelCopy.setGlassEnabled(true);
               setWidget(vp);
               
               final FormPanel form = new FormPanel();
               form.setEncoding(FormPanel.ENCODING_MULTIPART);
               form.setMethod(FormPanel.METHOD_POST);
              
               // TODO are these style file hooks?
               form.addStyleName("table-center");
               form.addStyleName("demo-FormPanel");

               VerticalPanel holder = new VerticalPanel();
               holder.add(new Hidden("courseId", Integer.toString(courseId)));
               holder.add(new Label("Choose a file"));
               holder.add(new HTML(new SafeHtmlBuilder().
                       appendEscapedLines("File should be tab-delimited in format:\n").toSafeHtml()));
               holder.add(new HTML(new SafeHtmlBuilder().appendHtmlConstant("<font face=courier>")
                       .appendEscaped("username firstname lastname email password").appendHtmlConstant("</font>").toSafeHtml()));
               FileUpload fup=new FileUpload();
               fup.setName("fileupload");
               holder.add(fup);
               holder.add(new Button("Register students", new ClickHandler() {
                   @Override
                   public void onClick(ClickEvent event) {
                       form.submit();
                   }
               }));
               form.setAction(GWT.getModuleBaseURL()+"registerStudents");
               GWT.log("URL: "+GWT.getModuleBaseURL()+"registerStudents");
               form.add(holder);
               vp.add(form);
               form.addSubmitHandler(new FormPanel.SubmitHandler() {
                   @Override
                   public void onSubmit(SubmitEvent event) {
                       GWT.log("pushing submit");
                       vp.add(ViewUtil.createLoadingGrid(""));
                       GWT.log("Loading...");
                   }
               });
               form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
                   public void onSubmitComplete(SubmitCompleteEvent event) {
                       GWT.log("onSubmitComplete complete");
                       // now we can hide the panel
                       panelCopy.hide();
                       reloadUsers();
                       getSession().add(StatusMessage.goodNews(event.getResults()));
                   }                   
               });
            }
        }
        
        /* (non-Javadoc)
         * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
         */
        public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
            session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
            
            // Activate views
            pageNavPanel.setBackHandler(new BackHomeHandler(session));
            pageNavPanel.setLogoutHandler(new LogoutHandler(session));
            userAdminUsersListView.activate(session, subscriptionRegistrar);
            statusMessageView.activate(session, subscriptionRegistrar);
            
            // The session should contain a course
            Course course = getCurrentCourse();
            rawCourseTitle=course.getName()+" - "+course.getTitle();
            courseLabel.setText(rawCourseTitle);
            courseId=course.getId();
            session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
        }
        
        @Override
        public void eventOccurred(Object key, Publisher publisher, Object hint) {
            if (key == Session.Event.ADDED_OBJECT && (hint instanceof User)) {
                onSelectUser((User) hint);
            } else if (key == Session.Event.ADDED_OBJECT && (hint instanceof CourseSelection)) {
                
            }
        }
        
        private void reloadUsers() {
            userAdminUsersListView.loadUsers(getSession());
        }
        
        private void onSelectUser(User user) {
            // Problem selected: enable/disable buttons appropriately
            userManagementButtons[ButtonPanelAction.EDIT.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.NEW.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.DELETE.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.REGISTER_USERS.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.VIEW_ALL_PROGRESS.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.VIEW_USER_PROGRESS.ordinal()].setEnabled(true);
        }
        
        private void handleEditUser(ClickEvent event) {
            GWT.log("handle edit user");
            //final User chosen = getSession().get(User.class);
            final User chosen = userAdminUsersListView.getSelectedUser();
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
        
        private void handleDeleteUser() {
            GWT.log("Delete User not implemented");
            Window.alert("Not implemented yet, sorry");
        }
        
        private void handleNewUser(ClickEvent event) {
            GWT.log("handle new user");
            
            Widget w = (Widget)event.getSource();
            AddUserPopupPanel pop = new AddUserPopupPanel(w, courseId);
            pop.center();
            pop.setGlassEnabled(true);
            pop.show();

        }
        
        private void handleRegisterNewUsers(ClickEvent event) {
            GWT.log("handle Register new users");
            Widget w = (Widget)event.getSource();
            RegisterUsersPopupPanel pop = new RegisterUsersPopupPanel(w, courseId);
            pop.center();
            pop.setGlassEnabled(true);
            pop.show();
        }
        
        private void handleUserProgress(ClickEvent event) {
            GWT.log("handle user progress");
            final User chosen = userAdminUsersListView.getSelectedUser();
            if (chosen==null) {
                return;
            }
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
}
