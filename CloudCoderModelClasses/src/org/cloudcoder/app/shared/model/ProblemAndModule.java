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
 * A {@link Problem} and the {@link Module} in which the problem
 * is categorized.
 * 
 * @author David Hovemeyer
 */
public class ProblemAndModule implements Serializable {
	private static final long serialVersionUID = 1L;

	private Problem problem;
	private Module module;
	
	public ProblemAndModule() {
		
	}
	
	public ProblemAndModule(Problem problem, Module module) {
		this.problem = problem;
		this.module = module;
	}
	
	public void setProblem(Problem problem) {
		this.problem = problem;
	}
	
	public Problem getProblem() {
		return problem;
	}
	
	public void setModule(Module module) {
		this.module = module;
	}
	
	public Module getModule() {
		return module;
	}
}
