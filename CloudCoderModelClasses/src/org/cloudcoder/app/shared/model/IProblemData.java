// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
// Copyright (C) 2015, Eisenberger Andras <csirkeee@gmail.com>
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

/**
 * Interface describing getters/setters for {@link ProblemData}.
 * Classes that use this interface (instead of using ProblemData directly)
 * can be more flexible because they permit the use of adapter
 * objects.
 * 
 * @author David Hovemeyer
 */
public interface IProblemData {

	public abstract void setProblemType(ProblemType problemType);

	public abstract void setProblemType(int problemType);

	public abstract ProblemType getProblemType();

	/**
	 * @return the testName
	 */
	public abstract String getTestname();

	/**
	 * @param testName the testName to set
	 */
	public abstract void setTestname(String testName);

	public abstract void setBriefDescription(String briefDescription);

	public abstract String getBriefDescription();

	/**
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * @param description the description to set
	 */
	public abstract void setDescription(String description);

	/**
	 * @param skeleton the skeleton to set
	 */
	public abstract void setSkeleton(String skeleton);

	/**
	 * @return the skeleton
	 */
	public abstract String getSkeleton();

	/**
	 * @param evaluator the evaluator to set
	 */
	public abstract void setEvaluator(String evaluator);

	/**
	 * @return the evaluator, the code used to evaluate tests for a submission
	 */
	public abstract String getEvaluator();

	/**
	 * Set the schema version.
	 * 
	 * @param schemaVersion the schema version
	 */
	public abstract void setSchemaVersion(int schemaVersion);

	/**
	 * Get the schema version.
	 * 
	 * @return the schema version
	 */
	public abstract int getSchemaVersion();

	/**
	 * Set the name of the author.
	 * 
	 * @param authorName the name of the author
	 */
	public abstract void setAuthorName(String authorName);

	/**
	 * Get the name of the author.
	 * 
	 * @return the name of the author
	 */
	public abstract String getAuthorName();

	/**
	 * Set the author's email address.
	 * @param authorEmail the author's email address
	 */
	public abstract void setAuthorEmail(String authorEmail);

	/**
	 * Get the author's email address.
	 * 
	 * @return the author's email address
	 */
	public abstract String getAuthorEmail();

	/**
	 * Set the URL of the author's website.
	 * 
	 * @param authorWebsite the URL of the author's website
	 */
	public abstract void setAuthorWebsite(String authorWebsite);

	/**
	 * Get the URL of the author's website
	 * 
	 * @return the URL of the author's website
	 */
	public abstract String getAuthorWebsite();

	/**
	 * Get the "creation" timestamp in seconds past the epoch, UTC.
	 * 
	 * @param timestampUTC the "creation" timestamp in seconds past the epoch, UTC
	 */
	public abstract void setTimestampUtc(long timestampUTC);

	/**
	 * Get the "creation" timestamp in seconds past the epoch, UTC.
	 * 
	 * @return the "creation" timestamp in seconds past the epoch, UTC
	 */
	public abstract long getTimestampUtc();

	/**
	 * Set the license under which this problem is available.
	 * 
	 * @param license the license under which this problem is available
	 */
	public abstract void setLicense(ProblemLicense license);

	/**
	 * Get the license under which this problem is available.
	 * 
	 * @return  the license under which this problem is available
	 */
	public abstract ProblemLicense getLicense();
	
	/**
	 * Set the SHA-1 hash of the parent problem (from which this problem was derived).
	 * 
	 * @param parentHash SHA-1 hash of the parent problem
	 */
	public void setParentHash(String parentHash);
	
	/**
	 * Return the SHA-1 hash of the parent problem (from which this problem was derived).
	 * 
	 * @return SHA-1 hash of the parent problem, or an empty string if this is not a derived problem
	 */
	public String getParentHash();
	
	/**
	 * Set the URL of the external library required for this problem.
	 *  
	 * @param externalLibraryUrl the external library required for this problem
	 */
	public void setExternalLibraryUrl(String externalLibraryUrl);
	
	/**
	 * Get the URL of the external library required for this problem.
	 * 
	 * @return the URL of the external library required for this problem
	 */
	public String getExternalLibraryUrl();
	
	/**
	 * Set the MD5 checksum of the external library required for this problem.
	 * 
	 * @param md5Hash the MD5 checksum of the external library required for this problem
	 */
	public void setExternalLibraryMD5(String md5Hash);
	
	/**
	 * Get the MD5 checksum of the external library required for this problem.
	 * 
	 * @return the MD5 checksum of the external library required for this problem
	 */
	public String getExternalLibraryMD5();
}