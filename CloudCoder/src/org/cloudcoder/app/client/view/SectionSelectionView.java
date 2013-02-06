// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.client.model.Section;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * A view allowing a {@link Section} of a {@link Course} to be selected.
 * The {@link Session} must have a {@link CourseSelection} in it at the
 * time this view is activated.
 * 
 * @author David Hovemeyer
 */
public class SectionSelectionView extends Composite implements Subscriber, SessionObserver {
	private List<String> sectionList;
	private ListBox chooseSectionBox;
	private Session session;
	private Section selectedSection;
	
	/**
	 * Constructor.
	 */
	public SectionSelectionView() {
		FlowPanel panel = new FlowPanel();

		panel.setStyleName("cc-inlineFlowPanel", true);
		
		InlineLabel chooseSectionLabel = new InlineLabel("Section: ");
		panel.add(chooseSectionLabel);
		sectionList = new ArrayList<String>();
		this.chooseSectionBox = new ListBox();
		chooseSectionBox.setWidth("80px");
		chooseSectionBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				handleSectionChange();
			}
		});
		panel.add(chooseSectionBox);
		
		initWidget(panel);
	}

	private String getSectionChoice() {
		int selected = chooseSectionBox.getSelectedIndex();
		return selected < 0 ? null : chooseSectionBox.getItemText(selected);
	}

	protected void handleSectionChange() {
		String choice = getSectionChoice();
		if (choice != null) {
			// Add a Section object to the session.
			// The StudentProgressView listens for these events,
			// and updates the results appropriately.
			Section section;
			if (choice.equals("All")) {
				section = new Section(); // all sections
			} else {
				int sectionNumber = Integer.parseInt(choice);
				section = new Section(sectionNumber);
			}
			selectedSection = section;
			session.add(section);  // broadcast the section change by adding it to the Session
		}
	}

	@Override
	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		// Use existing Section object if there is one.
		// Otherwise, create a new default one that will show
		// results for all sections.
		selectedSection = session.get(Section.class);
		if (selectedSection == null) {
			selectedSection = new Section();
			session.add(selectedSection);
		}

		CourseSelection courseSelection = session.get(CourseSelection.class);
		
		this.session = session;
		
		// Set section numbers
		sectionList.add("All");
		RPC.getCoursesAndProblemsService.getSectionsForCourse(courseSelection.getCourse(), new AsyncCallback<Integer[]>() {
			@Override
			public void onSuccess(Integer[] result) {
				for (Integer section : result) {
					sectionList.add(section.toString());
				}
				
				for (String item : sectionList) {
					chooseSectionBox.addItem(item);
				}
				
				int selectedIndex = sectionList.indexOf(selectedSection.toString());
				chooseSectionBox.setSelectedIndex(selectedIndex);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				session.add(StatusMessage.error("Couldn't get section numbers for course", caught));
			}
		});
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof Section) {
			// Section changed somehow: update the selection in the listbox if
			// the section is actually different than the current one
			Section added = (Section) hint;
			setSelected(added);
		}
	}

	private void setSelected(Section section) {
		if (selectedSection == null || section.getNumber() != selectedSection.getNumber()) {
			selectedSection = section;
			int index = sectionList.indexOf(selectedSection.toString());
			chooseSectionBox.setSelectedIndex(index);
		}
	}
	
	/**
	 * Return the {@link Section} that is currently selected.
	 * 
	 * @return the selected Section
	 */
	public Section getSelectedSection() {
		return selectedSection;
	}

}
