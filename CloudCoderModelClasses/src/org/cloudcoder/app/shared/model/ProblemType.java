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

/**
 * Problem type enumeration.
 * In general, CloudCoder can support many kinds of problems in
 * many programming languages.  This enumeration represents the
 * various kinds of problems.
 * 
 * @author David Hovemeyer
 */
public enum ProblemType {
	/**
	 * Problem involving writing a complete Java method.
	 */
	JAVA_METHOD(Language.JAVA, true),
	
	/**
	 * Problem involving writing a complete Python method.
	 */
	PYTHON_FUNCTION(Language.PYTHON, true),
	
	/**
	 * Problem involving writing a complete C function.
	 */
	C_FUNCTION(Language.C, true),
	
	/**
	 * Problem involving writing a complete C program,
	 * complete with #includes, a main function, etc.
	 * Input is read from stdin, output is written to stdout.
	 * Correctness is judged by testing each line of output against
	 * a regular expression.  If the regexp matches one line,
	 * then the output is judged to be correct.
	 */
	C_PROGRAM(Language.C, false),
	
	/**
	 * Problem involving writing a complete Java program: a top level
	 * class with a main method.  Judging works the same way
	 * as {@link #C_PROGRAM} (read from stdin, write to stdout,
	 * judge correctness by testing output lines against a regexp.)
	 */
	JAVA_PROGRAM(Language.JAVA, false),
	
	/**
	 * Problem involving writing a complete Ruby method.
	 */
	RUBY_METHOD(Language.RUBY, true),
	;
	
	private Language language;
	private boolean outputLiteral;
	
	private ProblemType(Language language, boolean outputLiteral) {
		this.language = language;
		this.outputLiteral = outputLiteral;
	}
	
	/**
	 * Get the Language associated with this ProblemType.
	 * 
	 * @return the Language associated with this ProblemType
	 */
	public Language getLanguage() {
		return this.language;
	}
	
	/**
	 * Return whether or not the {@link TestCase}s for this problem type
	 * specify a literal expected output.  Returns true for function/method
	 * problem types (where the result of the function/method is compared
	 * to a literal expected value), and false for problem types where
	 * output is checked against a regular expression.
	 * 
	 * @return
	 */
	public boolean isOutputLiteral() {
		return outputLiteral;
	}
}
