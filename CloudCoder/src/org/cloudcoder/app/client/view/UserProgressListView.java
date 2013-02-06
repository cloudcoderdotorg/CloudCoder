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

package org.cloudcoder.app.client.view;

import java.util.Arrays;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * @author jaimespacco
 * @author Andrei Papancea
 *
 */
public class UserProgressListView extends ResizeComposite implements Subscriber, SessionObserver
{
    private DataGrid<ProblemAndSubmissionReceipt> grid;
    private Grid loadingImage;
    private LayoutPanel panel;
    private Session session;
    private User user;
    private SubmissionReceipt[] submissionReceipts;
    
    /**
     * Constructor.
     */
    public UserProgressListView(User myUser) {
    	    user = myUser;

    	    panel=new LayoutPanel();
    	    loadingImage=ViewUtil.createLoadingGrid("results for "+user.getUsername());
    	    loadingImage.setVisible(true);
    	    panel.add(loadingImage);
    	    panel.setVisible(true);
    	    initWidget(panel);
    }

    /**
     * 
     */
    private void createGridHeader() {
        TextColumn<ProblemAndSubmissionReceipt> colID = new TextColumn<ProblemAndSubmissionReceipt>() {
            @Override
            public String getValue(ProblemAndSubmissionReceipt problem) {
                return problem.getProblem().getProblemId()+"";
            }
        };
        grid.addColumn(colID,"ID");
        grid.setColumnWidth(colID, "50px");
        grid.addColumn(new TextColumn<ProblemAndSubmissionReceipt>() {
            @Override
            public String getValue(ProblemAndSubmissionReceipt problem) {
                return problem.getProblem().getTestname();
            }
        }, "Problem name");
        grid.addColumn(new TextColumn<ProblemAndSubmissionReceipt>() {
            @Override
            public String getValue(ProblemAndSubmissionReceipt problem) {
            	if(problem.getReceipt() == null)
            		return "0/0";
            	else
            		return problem.getReceipt().getNumTestsPassed()+"/"+problem.getReceipt().getNumTestsAttempted();
            	
            }
        }, "Best score");
        grid.addColumn(new TextColumn<ProblemAndSubmissionReceipt>() {
            @Override
            public String getValue(ProblemAndSubmissionReceipt problem) {
                return problem.getProblem().getWhenDueAsDate().toString().substring(4,19);
            }
        }, "Due date");
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
     */
    @Override
    public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
    {
    	    this.session = session;
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		loadProblems(session);
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
     */
    @Override
    public void eventOccurred(Object key, Publisher publisher, Object hint) {
        if (key == Session.Event.ADDED_OBJECT && (hint instanceof CourseSelection)) {
            // load all the users for the current course
            loadProblems(session);
        }
    }
    
    public void loadProblems(final Session session) {
        CourseSelection courseSelection=session.get(CourseSelection.class);
        Course course = courseSelection.getCourse();
        
        RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(course, user, null, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
			@Override
			public void onSuccess(ProblemAndSubmissionReceipt[] result) {
				GWT.log("displaying problems for "+user.getUsername());

				grid = new DataGrid<ProblemAndSubmissionReceipt>();
		        createGridHeader();
				panel.clear();
				panel.add(grid);
				displayProblems(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				session.add(new StatusMessage(StatusMessage.Category.ERROR, "Could not load problems for course"));
			}
		});
    }
    
    protected void displayProblems(ProblemAndSubmissionReceipt[] result) {
        if(result != null){
            grid.setRowCount(result.length);
            grid.setRowData(Arrays.asList(result));
            grid.setWidth("100%");
            grid.setHeight(result.length*50+"px");
        }
    }

}
