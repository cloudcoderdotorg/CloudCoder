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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Utility methods for working with the {@link Session} object.
 * 
 * @author David Hovemeyer
 */
public class SessionUtil {
	/**
	 * Retrieve list of {@link ProblemAndSubmissionReceipt}s for given {@link Course}.
	 * 
	 * @param page     the {@link CloudCoderPage} which initiated the loading of problems
	 * @param course   the {@link Course}
	 * @param session  the {@link Session}
	 */
	public static void loadProblemAndSubmissionReceiptsInCourse(final CloudCoderPage page, final Course course, final Session session) {
        RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, session.get(User.class), (Module)null, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
            @Override
            public void onFailure(Throwable caught) {
            	if (caught instanceof CloudCoderAuthenticationException) {
            		// See if we can log back in
            		page.recoverFromServerSessionTimeout(new Runnable() {
            			public void run() {
            				// Try again!
            				loadProblemAndSubmissionReceiptsInCourse(page, course, session);
            			}
            		});
            	} else {
	                GWT.log("Error loading problems", caught);
	                session.add(StatusMessage.error("Error loading problems: " + caught.getMessage()));
            	}
            }

            @Override
            public void onSuccess(ProblemAndSubmissionReceipt[] result) {
            	GWT.log(result.length + " ProblemAndSubmissionReceipts loaded successfully, adding to client-side session...");
                session.add(result);
            }
        });
		
	}
}
