package org.cloudcoder.app.shared.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ProblemAndTestCaseListTest {
	private ProblemAndTestCaseList first, second;
	
	@Before
	public void setUp() {
		first = new ProblemAndTestCaseList();
		second = new ProblemAndTestCaseList();
		
		first.setProblem(new Problem());
		second.setProblem(new Problem());
		
		TestCase t1 = new TestCase();
		t1.setTestCaseName("t1");
		TestCase t2 = new TestCase();
		t2.setTestCaseName("t2");
		TestCase t3 = new TestCase();
		t3.setTestCaseName("t3");
		
		first.setTestCaseList(new TestCase[]{t1});
		
		second.setTestCaseList(new TestCase[]{t2, t3});
	}
	
	@Test
	public void testCopyFrom() throws Exception {
		assertEquals(1, first.getTestCaseList().length);
		assertEquals("t1", first.getTestCaseList()[0].getTestCaseName());
		int probId = System.identityHashCode(first.getProblem());
		int firstTestCaseId = System.identityHashCode(first.getTestCaseList()[0]);
		
		first.copyFrom(second);
		
		// TestCase data should match copied ProblemAndTestCaseList
		assertEquals(2, first.getTestCaseList().length);
		assertEquals("t2", second.getTestCaseList()[0].getTestCaseName());
		assertEquals("t3", second.getTestCaseList()[1].getTestCaseName());
		
		// Identities of Problem and first TestCase should not have changed:
		// copyFrom() should always attempt to preserve existing Problem/TestCase
		// objects when possible.
		assertEquals(probId, System.identityHashCode(first.getProblem()));
		assertEquals(firstTestCaseId, System.identityHashCode(first.getTestCaseList()[0]));
	}
}
