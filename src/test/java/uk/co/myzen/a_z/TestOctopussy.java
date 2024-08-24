package uk.co.myzen.a_z;

import org.junit.Before;
import org.junit.Test;

public class TestOctopussy {

	Octopussy instance = null;

	int defaultPower;
	float[] prices;
	int index;

	int[] result;

	@Before
	public void init() {

		instance = Octopussy.getInstance();

	}

	@Test
	public void test() {

		defaultPower = 5000;

		prices = new float[] { -2.1f, 3.0f, 20.0f };

		index = 1;

		result = instance.testableOPC(defaultPower, prices, index);

//		fail("Not yet implemented");
	}

}
