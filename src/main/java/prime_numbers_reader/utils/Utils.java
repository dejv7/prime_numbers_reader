package prime_numbers_reader.utils;

import java.math.BigInteger;

public class Utils {

	/**
	 * Check if number is prime
	 * 
	 * @param number
	 * @return true if number is prime
	 */
	public static boolean isPrime(BigInteger number) {
		if (number.signum() > 0)
			return number.isProbablePrime(100);
		return false;
	}

}
