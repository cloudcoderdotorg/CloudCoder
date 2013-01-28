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

package org.cloudcoder.app.server.rpc;

import java.util.List;

import org.cloudcoder.app.client.rpc.UserService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author jaimespacco
 *
 */
public class UserServiceImpl extends RemoteServiceServlet implements UserService
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger=LoggerFactory.getLogger(UserServiceImpl.class);

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.rpc.UserService#getUsers(org.cloudcoder.app.shared.model.Course)
     */
    @Override
    public User[] getUsers(int courseId)
    throws CloudCoderAuthenticationException
    {
        //logger.warn("Getting all users in course "+course.getName());
        GWT.log("Getting all users in courseId "+courseId);
        User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
        logger.debug(user.getUsername() + " listing all users");
        // TODO: how to authenticate that this is an instructor?
        List<User> resultList = Database.getInstance().getUsersInCourse(courseId);
        
        User[] userArr=new User[resultList.size()];
        return resultList.toArray(userArr);
    }
    
    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.rpc.UserService#addUserToCourse(org.cloudcoder.app.shared.model.EditedUser, int)
     */
    @Override
    public Boolean addUserToCourse(EditedUser editedUser, int courseId) throws CloudCoderAuthenticationException {
    	// make sure user is logged in
    	User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
    	
    	// Add the EditedUser to the database
        User user = editedUser.getUser();
    	logger.info("Adding "+user.getUsername()+" to courseId "+courseId);
        Database.getInstance().addUserToCourse(authenticatedUser, courseId, editedUser);
        
        return true;
    }
    
    @Override
    public Boolean editUser(User user)
    throws CloudCoderAuthenticationException
    {
        logger.warn("Editing userid "+user.getId()+", username "+user.getUsername());
        User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
        Database.getInstance().editUser(user);
        return true;
    }
    
    @Override
    public void editCourseRegistrationType(int userId, int courseId, CourseRegistrationType type)
    throws CloudCoderAuthenticationException
    {
        logger.warn("Editing registration type of "+userId+" in course "+courseId);
        User authenticatedUser = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
        Database.getInstance().editRegistrationType(userId, courseId, type);
    }

    
}
