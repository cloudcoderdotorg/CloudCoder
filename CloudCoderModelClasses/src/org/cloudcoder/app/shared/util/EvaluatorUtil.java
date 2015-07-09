// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2015, Andras Eisenberger 
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

package org.cloudcoder.app.shared.util;

import org.cloudcoder.app.shared.model.IProblem;
import org.cloudcoder.app.shared.model.ProblemType;

/**
 * Utility methods problem evaluators
 * 
 * @author Andras Eisenberger
 */
public class EvaluatorUtil {
	/**
	 * Determine whether custom evaluators can be used for {@link ProblemType}.
	 * 
	 * @param problemType the problem type
	 * @return the AceEditorMode for the Language, or null if the Language is not known
	 */
	public static boolean isEvaluatorUsedForProblemType(ProblemType problemType) {
		switch (problemType) {
		case PYTHON_FUNCTION:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Generate the default evaluator for the given problem. Returns empty
	 * {@link String} for problem types which don't use custom evaluators. 
	 */
	public static String getDefaultEvaluator(IProblem problem) {
		switch (problem.getProblemType()) {
		case PYTHON_FUNCTION:
			return getDefaultPythonFunctionEvaluator(problem);
		default:
			return "";
		}
	}
	
	/**
	 * Generate the default evaluator for a python problem
	 */
	public static String getDefaultPythonFunctionEvaluator(IProblem problem) {
		return "def _eval(_input, _expected):\n" +
		       "  _output=" + problem.getTestname() + "(*_input)\n" +
		       "  _result=(_expected == _output) if (type(_output) != float and type(_expected) != float) else (math.fabs(_output-_expected) < 0.00001)\n" +
		       "  return (_result, _output)\n";
	}
}
