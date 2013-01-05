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

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.QuizEndedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("editCode")
public interface EditCodeService extends RemoteService {
	/**
	 * Set the {@link Problem} the user wants to work on.
	 * 
	 * @param problemId the problem id
	 * @return the {@link Problem}
	 * @throws CloudCoderAuthenticationException if the user is not permitted to work on the problem
	 */
	public Problem setProblem(int problemId) throws CloudCoderAuthenticationException;
	
	/**
	 * Load the problem text of the user's work on the current {@link Problem}.
	 * 
	 * @return the {@link ProblemText}
	 * @throws CloudCoderAuthenticationException
	 */
    public ProblemText loadCurrentText() throws CloudCoderAuthenticationException;
    
    /**
     * Record changes to the problem text of the user's work on the
     * current {@link Problem}.
     * 
     * @param changeList        list of {@link Change}s to record
     * @param clientSubmitTime  client-side submission timestamp
     * @return true if successful
     * @throws CloudCoderAuthenticationException
     * @throws QuizEndedException if changes are not permitted because a quiz has ended
     */
	public Boolean logChange(Change[] changeList, long clientSubmitTime)
			throws CloudCoderAuthenticationException, QuizEndedException;
}
