package org.apgrp10.gwent.utils;

public class Random
{
	private static final java.util.Random random = new java.util.Random(System.currentTimeMillis());
	
	public static java.util.Random get()
	{
		return random;
	}

	public static long nextLong(long lower, long upper) { return random.nextLong(lower, upper); }
	public static int nextInt(int lower, int upper) { return random.nextInt(lower, upper); }
	public static boolean nextBoolean() { return random.nextBoolean(); }
	public static double nextDouble(double min, double max) { return random.nextDouble() * (max - min) + min; }
	public static float nextFloat(float min, float max) { return random.nextFloat() * (max - min) + min; }
	
	public static byte[] nextBytes(int length)
	{
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}
	
	public static void setSeed(long seed) { random.setSeed(seed); }
	
	public static long nextUid() { return random.nextLong() & 0x7f_ff_ff_ff_ff_ff_ff_ffl; }
}
