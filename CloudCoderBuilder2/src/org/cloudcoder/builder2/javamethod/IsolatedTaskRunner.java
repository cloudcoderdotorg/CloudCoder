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

package org.cloudcoder.builder2.javamethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IsolatedTask} for testing {@link ProblemType#JAVA_METHOD}
 * submissions.
 * 
 * @author Jaime Spacco
 */
public class IsolatedTaskRunner implements IsolatedTask<TestResult>
{
	private static final Logger logger = LoggerFactory.getLogger(IsolatedTaskRunner.class);
	
    private Class<?> theClass;
    private TestCase testCase;
    
    public IsolatedTaskRunner(Class<?> theClass, TestCase testCase) {
        this.theClass=theClass;
        this.testCase=testCase;
    }

    @Override
    public TestResult execute() {
        try {
            Method m = theClass.getMethod(testCase.getTestCaseName());
            Boolean result = (Boolean) m.invoke(null);
            if (result) {
            	return TestResultUtil.createResultForPassedTest(testCase);
            } else {
            	return TestResultUtil.createResultForFailedTest(testCase);
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof SecurityException) {
                //logger.warn("Security exception with code: "+programText);
                return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Security exception while testing submission");
            } 
            logger.warn("InvocationTargetException", e);
            return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, 
                    "Failed for input=(" + testCase.getInput() + ") expected=" + testCase.getOutput()+
                    ", exception "+e.getTargetException().getMessage());
        } catch (NoSuchMethodException e) {
            return new TestResult(TestOutcome.INTERNAL_ERROR, "Method not found while testing submission");
        } catch (IllegalAccessException e) {
            return new TestResult(TestOutcome.INTERNAL_ERROR, "Illegal access while testing submission");
        }
        //TODO: Catch Throwable and report INTERNAL_ERROR for anything else
    }
    
}
