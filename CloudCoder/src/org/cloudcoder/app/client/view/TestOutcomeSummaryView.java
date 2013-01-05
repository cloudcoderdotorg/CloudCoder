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

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * View that shows a green/red bar reflecting how many of
 * the test results passed.
 * 
 * @author David Hovemeyer
 */
public class TestOutcomeSummaryView extends Composite implements SessionObserver, Subscriber {
	// Keep these in sync with CloudCoder.css
	public static final double WIDTH_PX = 200.0;
	public static final double HEIGHT_PX = 28.0; 
	
	private static int nextId = 1; // used to generate unique ids
	
	private int id; // the number used in the unique ids for this widget
	
	public TestOutcomeSummaryView() {
		this.id = nextId++;
		HTML passedTestsDiv = new HTML(
				"<div class=\"cc-testOutcomeSummary\">" +
				"<div class=\"cc-testOutcomeSummaryInner\">" +
				"<div id=\"" + getInnerBackgroundElementId() + "\" class=\"cc-testOutcomeSummaryInnerBackground\">" +
				"<div id=\"" + getPassedTestsElementId() + "\" class=\"cc-testOutcomeSummaryPassedTests\"></div></div></div></div>");
		initWidget(passedTestsDiv);
	}

	private String getPassedTestsElementId() {
		return "cc-testOutcomeSummaryPassedTestsElt" + id;
	}

	private String getInnerBackgroundElementId() {
		return "cc-testOutcomeSummaryInnerBackgroundElt" + id;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
	 */
	@Override
	public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		displayEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
	 */
	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && (hint instanceof TestResult[])) {
			TestResult[] testResultList = (TestResult[]) hint;
			
			// Display empty state if there are no TestResults
			if (testResultList.length == 0) {
				displayEmpty();
				return;
			}
			
			// Determine what percentage of tests passed
			double passed = 0, total = 0;
			for (TestResult testResult : testResultList) {
				total += 1.0;
				if (testResult.getOutcome() == TestOutcome.PASSED) {
					passed += 1.0;
				}
			}
			
			// Display the green/red bar
			displayPercentPassed(passed / total);
		}
	}

	private void displayEmpty() {
		Element innerBackgroundElt = DOM.getElementById(getInnerBackgroundElementId());
		innerBackgroundElt.removeClassName("cc-testOutcomeSummaryInnerBackgroundNonEmpty");
		innerBackgroundElt.addClassName("cc-testOutcomeSummaryInnerBackgroundEmpty");
		
		Element passedTestsElt = DOM.getElementById(getPassedTestsElementId());
		passedTestsElt.getStyle().setWidth(0.0, Unit.PX);
	}

	private void displayPercentPassed(double passedRatio) {
		Element innerBackgroundElt = DOM.getElementById(getInnerBackgroundElementId());
		innerBackgroundElt.removeClassName("cc-testOutcomeSummaryInnerBackgroundEmpty");
		innerBackgroundElt.addClassName("cc-testOutcomeSummaryInnerBackgroundNonEmpty");
		
		Element passedTestsElt = DOM.getElementById(getPassedTestsElementId());
		passedTestsElt.getStyle().setWidth(passedRatio * 100.0, Unit.PCT);
	}
	
}
