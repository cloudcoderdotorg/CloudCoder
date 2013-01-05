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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.EditCodeService;
import org.cloudcoder.app.server.model.ApplyChangeToTextDocument;
import org.cloudcoder.app.server.model.TextDocument;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.QuizEndedException;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link EditCodeService}.
 * 
 * @author David Hovemeyer
 */
public class EditCodeServiceImpl extends RemoteServiceServlet implements EditCodeService {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(EditCodeServiceImpl.class);
	
	private static final boolean DEBUG_CODE_DELTAS = false;

	@Override
	public Problem setProblem(int problemId) throws CloudCoderAuthenticationException {
		// make sure client is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());

		// Get the problem
		Pair<Problem, Quiz> pair = Database.getInstance().getProblem(user, problemId);
		if (pair == null) {
			return null;
		}

		// Store the Problem and (if there is one) Quiz in the HttpSession -
		// that way, the servlets that depend on knowing the problem/quiz
		// have access to a known-authentic problem/quiz. (I.e., we don't have
		// to trust a problem id sent as an RPC parameter which might have
		// been forged.)
		getThreadLocalRequest().getSession().setAttribute(SessionAttributeKeys.PROBLEM_KEY, pair.getLeft());
		getThreadLocalRequest().getSession().setAttribute(SessionAttributeKeys.QUIZ_KEY, pair.getRight());

		// If appropriate, record that the user has started the problem
		Database.getInstance().getOrAddLatestSubmissionReceipt(user, pair.getLeft());
		
		return pair.getLeft();
	}

    @Override
    public ProblemText loadCurrentText() throws CloudCoderAuthenticationException {
    	// make sure client is authenticated
    	User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
    	
    	// make sure a problem has been loaded
    	Problem problem = (Problem) getThreadLocalRequest().getSession().getAttribute(SessionAttributeKeys.PROBLEM_KEY);
    	
    	if (problem == null) {
    		// Can't load current text unless a Problem has been loaded
    		throw new CloudCoderAuthenticationException();
    	}

    	ProblemText text = doLoadCurrentText(user, problem);
    	
    	// Check to see if current problem is a quiz
    	Quiz quiz = (Quiz) getThreadLocalRequest().getSession().getAttribute(SessionAttributeKeys.QUIZ_KEY);
    	if (quiz != null) {
    		text.setQuiz(true);
    	}
    	
    	if (DEBUG_CODE_DELTAS) {
	    	// Keep a TextDocument in the session for debugging code deltas
	    	TextDocument doc = new TextDocument();
	    	doc.setText(text.getText());
	    	getThreadLocalRequest().getSession().setAttribute("doc", doc);
    	}
    	
    	return text;
    }

	protected ProblemText doLoadCurrentText(User user, Problem problem) {
    	Change mostRecent = Database.getInstance().getMostRecentChange(user, problem.getProblemId());

    	if (mostRecent == null) {
    		// Presumably, user has never worked on this problem.
    		logger.debug("No changes recorded for user " + user.getId() + ", problem " + problem.getProblemId());
    		
    		// If the problem has a skeleton, it is the initial problem text.
    		// Otherwise, just use the empty string.
    		String initialText = problem.hasSkeleton() ? problem.getSkeleton() : "";
    		ProblemText initialProblemText = new ProblemText(initialText, true);
    		
    		return initialProblemText;
    	} else {
    		Change change = mostRecent; // result.get(0);

    		// If the Change is a full text change, great.
    		if (change.getType() == ChangeType.FULL_TEXT) {
    			return new ProblemText(change.getText(), false);
    		}

    		// Otherwise, find the last full-text change (if any) and
    		// apply all later changes.
    		
    		// Find the most recent full-text change.
    		Change fullText = Database.getInstance().getMostRecentFullTextChange(user, problem.getProblemId());
    		
    		// Text doc to accumulate changes.
    		TextDocument textDocument = new TextDocument();
    		
    		// Find the base revision (event id) that the deltas are relative to, if any.
    		int baseRev;
    		if (fullText != null) {
    			// found a full-text change to use as a base revision
    			textDocument.setText(fullText.getText());
    			baseRev = fullText.getEventId();
    		} else {
    			// no full-text change exists: base revision is implicitly the empty document
    			baseRev = -1;
    		}
    		
    		// Get all deltas that follow the base revision.
    		List<Change> deltaList = Database.getInstance().getAllChangesNewerThan(user, problem.getProblemId(), baseRev);
    		
    		// Apply the deltas to the base revision.
    		try {
        		ApplyChangeToTextDocument applicator = new ApplyChangeToTextDocument();
	    		for (Change delta : deltaList) {
	    			applicator.apply(delta, textDocument);
	    		}
	    		return new ProblemText(textDocument.getText(), false);
    		} catch (RuntimeException e) {
    			// FIXME: should do something smarter than this 
    			return new ProblemText(fullText != null ? fullText.getText() : "", false);
    		}
    	}
	}

	@Override
	public Boolean logChange(Change[] changeList, long clientSubmitTime)
			throws CloudCoderAuthenticationException, QuizEndedException {
		long serverSubmitTime = System.currentTimeMillis();

		// make sure client is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		
		// if there is a quiz, check whether it has ended
		Quiz quiz = (Quiz) getThreadLocalRequest().getSession().getAttribute(SessionAttributeKeys.QUIZ_KEY);
		if (quiz != null) {
			// User is working on a quiz.
			
			// Important: reload the object from the database.
			// The instructor may have ended the quiz by changing the end time
			// from 0.
			if (!Database.getInstance().reloadModelObject(quiz)) {
				logger.error("logChange: could not reload Quiz object");
				return false;
			}
			
			if (quiz.getEndTime() > 0) { // end time of 0 means open-ended
				long currentTime = System.currentTimeMillis();
				if (currentTime > quiz.getEndTime()) {
					throw new QuizEndedException();
				}
			}
		}
		
		// Make sure all Changes have proper user id
		for (Change change : changeList) {
			if (change.getEvent().getUserId() != user.getId()) {
				throw new CloudCoderAuthenticationException();
			}
		}
		
		// Adjust the timestamps of each Change based on the delta between
		// the client submit time and the server submit time,
		// changing them to server times.
		// That way, we don't assume that the client and the server clocks
		// are synchronized (even approximately), only that they are
		// advancing at the same rate.  Note that we are ignoring network
		// latency here, but if it remains relatively consistent, and is
		// small compared to the frequency at which changes are flushed on
		// the client side, it shouldn't affect things too much.
		long clientServerTimeDelta = serverSubmitTime - clientSubmitTime;
		//System.out.println("client/server time delta: " + clientServerTimeDelta);
		for (Change change : changeList) {
			long orig = change.getEvent().getTimestamp();
			change.getEvent().setTimestamp(orig + clientServerTimeDelta);
		}

		// Insert changes
		Database.getInstance().storeChanges(changeList);
		
		if (DEBUG_CODE_DELTAS) {
			// For debugging - keep a TextDocument in the session,
			// and apply changes to it
			
			HttpServletRequest req = this.getThreadLocalRequest();
			HttpSession session = req.getSession();
			
			TextDocument doc = (TextDocument) session.getAttribute("doc");
			if (doc == null) {
				doc = new TextDocument();
				session.setAttribute("doc", doc);
			}
			ApplyChangeToTextDocument applicator = new ApplyChangeToTextDocument();
			for (Change change : changeList) {
				applicator.apply(change, doc);
			}
			
			logger.debug("Document is now:\n" + doc.getText());
		}
		
		return true;
	}

}
