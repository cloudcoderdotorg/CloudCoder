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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * Aggregate of {@link User} and {@link SubmissionReceipt}.
 * Useful for pairing a user with a specific SubmissionReceipt (for example,
 * the user's best submission receipt.)
 * 
 * @author David Hovemeyer
 */
public class UserAndSubmissionReceipt implements Serializable {
	private User user;
	private SubmissionReceipt submissionReceipt;
	
	/**
	 * Constructor.
	 */
	public UserAndSubmissionReceipt() {
		
	}
	
	/**
	 * Set the user.
	 * 
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * Set the submission receipt.
	 * 
	 * @param submissionReceipt the submission receipt to set
	 */
	public void setSubmissionReceipt(SubmissionReceipt submissionReceipt) {
		this.submissionReceipt = submissionReceipt;
	}
	
	/**
	 * @return the submission receipt
	 */
	public SubmissionReceipt getSubmissionReceipt() {
		return submissionReceipt;
	}
}
