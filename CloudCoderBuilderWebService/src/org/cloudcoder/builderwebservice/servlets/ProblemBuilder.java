// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builderwebservice.servlets;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.webservice.util.BadRequestException;

/**
 * Build a {@link Problem} from a {@link Request}.
 * 
 * @author David Hovemeyer
 */
public class ProblemBuilder {
	private Request request;

	/**
	 * Constructor.
	 * 
	 * @param request the {@link Request}
	 */
	public ProblemBuilder(Request request) {
		this.request = request;
	}

	/**
	 * Build a {@link Problem} from the {@link Request}.
	 * 
	 * @return the {@link Problem}
	 * @throws BadRequestException if the Request doesn't contain the required information
	 */
	public Problem build() throws BadRequestException {
		Problem problem = new Problem();
		
		// Some of the ProblemData fields are significant
		problem.setProblemType(DecodeRequest.getProblemType(request));
		problem.setTestname(request.getTestname());
		problem.setBriefDescription(request.getTestname());
		problem.setDescription("BWS submission");
		problem.setSkeleton("");
		problem.setSchemaVersion(Problem.SCHEMA.getVersion());
		problem.setAuthorName("BWS");
		problem.setAuthorEmail("support@cloudcoder.org");
		problem.setAuthorWebsite("http://cloudcoder.org");
		problem.setTimestampUtc(0L);
		problem.setLicense(ProblemLicense.NOT_REDISTRIBUTABLE);
		problem.setParentHash("");
		problem.setEvaluator("");
		
		// None of the Problem-specific fields are significant
		// Important: the builder must ignore the problem id.
		// CloudCoderBuilder2 does ignore the problem id (and never attempts
		// to cache Problem/TestCases).  So, it doesn't matter
		// what value we set here.
		problem.setProblemId(Constants.FAKE_PROBLEM_ID);
		problem.setCourseId(121);
		problem.setWhenAssigned(0L);
		problem.setWhenDue(0L);
		problem.setProblemAuthorship(ProblemAuthorship.ORIGINAL);
		problem.setDeleted(false);
		problem.setModuleId(1);
		problem.setShared(false);
		
		return problem;
	}
}
