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

import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.QuizEndedException;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("submit")
public interface SubmitService extends RemoteService {
	/**
	 * Submit program text for problem with given problem id to server.
	 * 
	 * @param problemId the problem id
	 * @param programText the program text
	 * @throws CloudCoderAuthenticationException
	 * @throws QuizEndedException 
	 */
    public void submit(int problemId, String programText) throws CloudCoderAuthenticationException, SubmissionException, QuizEndedException;
    
    /**
     * Check to see if a {@link SubmissionResult} for the most-recently-submitted
     * program text is available.  (I.e., has compilation/testing of the submission completed.)
     * 
     * @return a SubmissionResult, or null if compilation/testing of the
     *         submission has not completed yet) 
     * @throws CloudCoderAuthenticationException
     */
    public SubmissionResult checkSubmission()  throws CloudCoderAuthenticationException, SubmissionException;
}
