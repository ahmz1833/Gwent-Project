package org.apgrp10.gwent.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Random {
	public static ThreadLocalRandom get() {
		return ThreadLocalRandom.current();
	}

	public static long nextLong(long lower, long upper) {return get().nextLong(lower, upper);}

	public static int nextInt(int lower, int upper) {return get().nextInt(lower, upper);}

	public static boolean nextBoolean() {return get().nextBoolean();}

	public static double nextDouble(double min, double max) {return get().nextDouble() * (max - min) + min;}

	public static float nextFloat(float min, float max) {return get().nextFloat() * (max - min) + min;}

	public static byte[] nextBytes(int length) {
		byte[] bytes = new byte[length];
		get().nextBytes(bytes);
		return bytes;
	}

	public static long nextPosLong() {return get().nextLong() & 0x7f_ff_ff_ff_ff_ff_ff_ffl;}
	public static long nextId() {return nextPosLong();}
}
