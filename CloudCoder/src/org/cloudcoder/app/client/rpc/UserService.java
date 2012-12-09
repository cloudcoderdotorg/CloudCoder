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

package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * @author jspacco
 *
 */
@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService
{
    /**
     * 
     * Get a list of all {@link User} objects for each of
     * the users registered for the given Course.
     * 
     * @param course The course for which we want the users.
     * @return An array of all of the Users registered for the given course.
     * @throws CloudCoderAuthenticationException if the client is not authenticated
     */
    User[] getUsers(int courseId) throws CloudCoderAuthenticationException;
    
    Boolean addUserToCourse(User user, int courseId, CourseRegistrationType type, int section) throws CloudCoderAuthenticationException;
    
    /**
     * Edit the fields of the {@link User} record with the new values.
     * Use the id field in the given record to locate the record to update.
     * 
     * @param user
     * @return
     */
    Boolean editUser(User user) throws CloudCoderAuthenticationException;
    
    
    void editCourseRegistrationType(int userId, int courseId, CourseRegistrationType type) throws CloudCoderAuthenticationException;
}
