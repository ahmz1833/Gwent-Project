package org.apgrp10.gwent;

import org.apgrp10.gwent.utils.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RandomTest {
	@Test
	public void testPassword(){
		String password = Random.nextPassword();
		assert password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?/~])[^ ]{8,20}$");
	}
	@Test
	public void testId(){
		long id1 = Random.nextId();
		long id2 = Random.nextId();
		long id3 = Random.nextPosLong();
		long id4 = Random.nextLong(0, Long.MAX_VALUE);
		assert id1 != id2;
		assert id3 != id2;
		assert id1 != id4;
	}
	@Test
	public void testBoolean(){
		boolean[] list = new boolean[100];
		for(int i = 0; i < 100; i++)
			list[i] = Random.nextBoolean();
		int countTrue = 0;
		for(int i = 0; i < 100; i++)
			if(list[i])
				countTrue++;
		assert countTrue > 5 && countTrue < 95;
	}
	@Test
	public void testDecimal(){
		float f1 = Random.nextFloat(0, 100);
		float f2 = Random.nextFloat(0, 100);
		assert f1 != f2;
		double d1 = Random.nextDouble(0, 100);
		double d2 = Random.nextDouble(0, 100);
		assert d1 != d2;
	}
	@Test
	public void testBytes(){
		byte[] list = Random.nextBytes(8);
		boolean diff = false;
		for(int i = 0; i < 8; i++){
			if (list[i] != list[0]) {
				diff = true;
				break;
			}
		}
		assert diff;
	}
}
