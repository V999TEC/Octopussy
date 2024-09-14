package uk.co.myzen.a_z;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestOctopussyOne {

	private static Octopussy instance = null;

	int defaultPower;
	float[] prices;
	final int index = 1;

	int[] result;

	@BeforeClass
	public static void init() {

		instance = Octopussy.getInstance();
	}

	@Test
	public void test01() {

		defaultPower = 5000;

		prices = new float[] { -2.1f, 3.0f, 20.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be maximum", result[0], 6000);
		assertEquals("S2 power should be < S1", result[1], 5024);
		assertEquals("S3 power should be < S2", result[2], 3976);
	}

	@Test
	public void test02() {

		defaultPower = 5000;

		prices = new float[] { 5.0f, 10.0f, 15.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be maximum", result[0], 6000);
		assertEquals("S2 power should be < S1", result[1], 5501);
		assertEquals("S3 power should be < S2", result[2], 3499);
	}

	@Test
	public void test03() {

		defaultPower = 4000;

		prices = new float[] { 10.0f, 10.0f, 10.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be default", result[0], defaultPower);
		assertEquals("S2 power should be default", result[1], defaultPower);
		assertEquals("S3 power should be default", result[2], defaultPower);
	}

	@Test
	public void test04() {

		defaultPower = 3000;

		prices = new float[] { 2.0f, 3.0f, 4.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be default", result[0], 6000);
		assertEquals("S2 power should be default", result[1], 2250);
		assertEquals("S3 power should be default", result[2], 750);
	}

	@Test
	public void test05() {

		defaultPower = 3000;

		prices = new float[] { 2.0f, 2.0f, 10.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be maximum", result[0], 6000);
		assertEquals("S2 power should be < S1", result[1], 3000);
		assertEquals("S3 power should be zero", result[2], 0);
	}

	@Test
	public void test06() {

		defaultPower = 3000;

		prices = new float[] { 2.0f, 10.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be maximum", result[0], 6000);
		assertEquals("S2 power should be zero", result[1], 0);

	}

	@Test
	public void test07() {

		defaultPower = 3000;

		prices = new float[] { 2.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be default", result[0], 3000);

	}

	@Test
	public void test08() {

		defaultPower = 3000;

		prices = new float[] { 2.0f, 2.5f, 3.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be maximum", result[0], 6000);
		assertEquals("S2 power should be 2650", result[1], 2650);
		assertEquals("S3 power should be 350", result[2], 350);

	}

	@Test
	public void test09() {

		defaultPower = 3000;

		prices = new float[] { 2.0f, 2.0f, 3.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be maximum", result[0], 6000);
		assertEquals("S2 power should be 3000", result[1], 3000);
		assertEquals("S3 power should be 0", result[2], 0);

	}

	@Test
	public void test10() {

		defaultPower = 3000;

		prices = new float[] { 2.0f, 2.0f, 2.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be 3000", result[0], 3000);
		assertEquals("S2 power should be 3000", result[1], 3000);
		assertEquals("S3 power should be 3000", result[2], 3000);

	}

	@Test
	public void test11() {

		defaultPower = 2000;

		prices = new float[] { 2.0f, 4.0f, 6.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be 6000", result[0], 6000);
		assertEquals("S2 power should be zero", result[1], 0);
		assertEquals("S3 power should be zero", result[2], 0);

	}

	@Test
	public void test12() {

		defaultPower = 3333;

		prices = new float[] { 2.0f, 4.0f, 6.0f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be 6000", result[0], 6000);
		assertEquals("S2 power should be 2562", result[1], 2559);
		assertEquals("S3 power should be 1938", result[2], 1440);

	}

	@Test
	public void test13() {

		defaultPower = 4500;

		prices = new float[] { 13.02f, 14.6f };

		result = instance.testableOPC(defaultPower, prices, index);

		assertEquals("S1 power should be 6000", result[0], 6000);
		assertEquals("S2 power should be 3000", result[1], 3000);

	}

}
