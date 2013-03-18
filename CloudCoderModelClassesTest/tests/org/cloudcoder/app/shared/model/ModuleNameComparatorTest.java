package org.cloudcoder.app.shared.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ModuleNameComparatorTest {
	private ModuleNameComparator comp;
	
	private Module Week8;
	private Module Week9;
	private Module Week10;
	
	private Module Week08;
	private Module Week008;
	
	private Module Plan9;
	private Module Plan10;
	
	@Before
	public void setUp() {
		comp = new ModuleNameComparator();
		
		Week8 = new Module();
		Week8.setName("Week8");
		Week9 = new Module();
		Week9.setName("Week9");
		Week10 = new Module();
		Week10.setName("Week10");
		
		Week08 = new Module();
		Week08.setName("Week08");
		Week008 = new Module();
		Week008.setName("Week008");
		
		Plan9 = new Module();
		Plan9.setName("Plan9");
		
		Plan10 = new Module();
		Plan10.setName("Plan10");
	}
	
	@Test
	public void testTrailingDigits() throws Exception {
		assertTrue(comp.compare(Week8, Week8) == 0);
		assertTrue(comp.compare(Week8, Week9) < 0);
		assertTrue(comp.compare(Week9, Week8) > 0);
		assertTrue(comp.compare(Week8, Week10) < 0);
		assertTrue(comp.compare(Week10, Week8) > 0);
		assertTrue(comp.compare(Plan9, Plan10) < 0);
		assertTrue(comp.compare(Plan10, Plan9) > 0);
	}
	
	@Test
	public void testTrailingDigitsDifferentPrefixes() throws Exception {
		// If the trailing digits are numerically equal but textually different,
		// then the comparison should be done by lexicographically comparing
		// the digit strings.  (E.g., "08" < "8".)
		assertTrue(comp.compare(Week8, Week08) > 0);
		assertTrue(comp.compare(Week08, Week8) < 0);
		assertTrue(comp.compare(Week8, Week008) > 0);
		assertTrue(comp.compare(Week008, Week8) < 0);
		assertTrue(comp.compare(Week08, Week008) > 0);
		assertTrue(comp.compare(Week008, Week08) < 0);
	}
	
	@Test
	public void testDifferentPrefixes() throws Exception {
		assertTrue(comp.compare(Week8, Plan9) > 0);
		assertTrue(comp.compare(Plan9, Week8) < 0);
		assertTrue(comp.compare(Week10, Plan9) > 0);
		assertTrue(comp.compare(Plan9, Week10) < 0);
	}
}
