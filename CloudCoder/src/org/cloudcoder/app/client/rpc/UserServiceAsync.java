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
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author jspacco
 *
 */
public interface UserServiceAsync
{
    void getUsers(int courseId, AsyncCallback<User[]> callback);
    void addUserToCourse(EditedUser editedUser, int courseId,
			AsyncCallback<Boolean> callback);
    void editUser(User user, AsyncCallback<Boolean> asyncCallback);
    void editCourseRegistrationType(int userId, int courseId, CourseRegistrationType type,
        AsyncCallback<Void> callback); 
        
        
}
