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
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EditCodeServiceAsync {
	public void setProblem(int problemId, AsyncCallback<Problem> callback);
	void loadCurrentText(AsyncCallback<ProblemText> callback);
	void logChange(Change[] changeList, long clientSubmitTime,
			AsyncCallback<Boolean> callback);
	void getSubmissionText(User submitter, Problem problem,
			SubmissionReceipt receipt, AsyncCallback<ProblemText> callback);
}
