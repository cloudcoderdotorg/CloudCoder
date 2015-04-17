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

package org.cloudcoder.app.shared.model;

import java.io.UnsupportedEncodingException;

/**
 * Compute a hash of the data in a {@link IProblemAndTestCaseData} object.
 * The hash should be "unique enough" that collisions will not arise
 * in practice.  <b>Important</b>: we only try to hash the "content"
 * of the problem and test cases, and not the "provenence" of the problem.
 * So, things like timestamps, parent hashes (for derived problems), etc.
 * should not be hashed.
 * 
 * @author David Hovemeyer
 */
public class HashProblemAndTestCaseData<
	ObjType extends IProblemAndTestCaseData<? extends IProblemData, ? extends ITestCaseData>
	> {
	private ObjType problemAndTestCaseData;
	private SHA1 sha1;
	
	/**
	 * Constructor.
	 * 
	 * @param problemAndTestCaseData the ProblemData to compute a hash for
	 */
	public HashProblemAndTestCaseData(ObjType problemAndTestCaseData) {
		this.problemAndTestCaseData = problemAndTestCaseData;
		this.sha1 = new SHA1();
	}
	
	/**
	 * Compute the hash.  The result is a 40-character hex string (160 bits of data).
	 * 
	 * @return the hash
	 */
	public String compute() {
		// Incorporate the ProblemData
		hashProblemData(problemAndTestCaseData.getProblem());

		// Incorporate each TestCase
		for (ITestCaseData testCaseData : problemAndTestCaseData.getTestCaseData()) {
			hashTestCaseData(testCaseData);
		}

		// Return the computed hash as a hex string
		byte[] digest = sha1.digest();
		return new ConvertBytesToHex(digest).convert();
	}

	private void hashProblemData(IProblemData problemData) {
		// Fields present in schema version 0 and later.
		updateString(problemData.getProblemType().toString());
		updateString(problemData.getTestname());
		updateString(problemData.getBriefDescription());
		updateString(problemData.getDescription());
		updateString(problemData.getSkeleton());
		updateInt(problemData.getSchemaVersion());

		// Things like author info, author email, creation timestamp, etc.
		// are part of the "provenence" of the problem, and as such
		// should not be included in the hash.
		/*
		updateString(problemData.getAuthorName());
		updateString(problemData.getAuthorEmail());
		updateString(problemData.getAuthorWebsite());
		updateLong(problemData.getTimestampUtc());
		updateString(problemData.getLicense().toString());
		*/
		
		// Digest external library URL, MD5 and problem evaluator if they are present
		updateStringIfNonEmpty(problemData.getExternalLibraryUrl());
		updateStringIfNonEmpty(problemData.getExternalLibraryMD5());
		
		updateStringIfNonEmpty(problemData.getEvaluator());
	}

	private void hashTestCaseData(ITestCaseData testCaseData) {
		updateString(testCaseData.getTestCaseName());
		updateString(testCaseData.getInput());
		updateString(testCaseData.getOutput());
		updateBoolean(testCaseData.isSecret());
	}

	private void updateString(String s) {
		// Note: we always digest the trimmed version of the string,
		// and always using UTF-8 to convert to bytes.
		try {
			sha1.update(s.trim().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Can't convert string to UTF-8 bytes?");
		}
	}

	private void updateStringIfNonEmpty(String s) {
		if (s == null) {
			return;
		}
		if (s.equals("")) {
			return;
		}
		updateString(s);
	}
	
	private void updateInt(int value) {
		// Digest an integer value by first converting it to a string.
		updateString(String.valueOf(value));
	}
	
//	private void updateLong(long value) {
//		// Digest an integer value by first converting it to a string.
//		updateString(String.valueOf(value));
//	}

	private void updateBoolean(boolean value) {
		// Digest a boolean value by first converting it to a string.
		updateString(String.valueOf(value));
	}
}
