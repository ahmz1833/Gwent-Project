package org.apgrp10.gwent.utils;

public class Random
{
	private static final java.util.Random random = new java.util.Random(System.currentTimeMillis());
	
	public static java.util.Random get()
	{
		return random;
	}
	
	public static int generateRandomNumber(int min, int max)
	{
		return random.nextInt(max - min + 1) + min;
	}
	
	public static boolean generateRandomBoolean()
	{
		return random.nextBoolean();
	}
	
	public static double generateRandomDouble(double min, double max)
	{
		return random.nextDouble() * (max - min) + min;
	}
	
	public static float generateRandomFloat(float min, float max)
	{
		return random.nextFloat() * (max - min) + min;
	}
	
	public static long generateRandomLong(long min, long max)
	{
		return random.nextLong() * (max - min) + min;
	}
	
	public static byte[] generateRandomBytes(int length)
	{
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}
	
	public static void setSeed(long seed)
	{
		random.setSeed(seed);
	}
	
	public static int generateRandomUserId()
	{
		setSeed(System.currentTimeMillis());
		return generateRandomNumber(0x10000000, 0x20000000);
	}
}
