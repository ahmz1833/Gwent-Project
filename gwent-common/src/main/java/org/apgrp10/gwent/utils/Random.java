package org.apgrp10.gwent.utils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class Random {
	private static final String PASS_CHAR_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+{}|;:,.<>?/~";
	private static final int PASS_MIN_LENGTH = 8;
	private static final int PASS_MAX_LENGTH = 20;
	private static final Pattern PASSWORD_PATTERN = Pattern.compile(
			"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?/~])[^ ]{" + PASS_MIN_LENGTH + "," + PASS_MAX_LENGTH + "}$"
	);

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

	public static long nextId() {
		long id;
		do id = nextPosLong(); while (id < 0x1_00_00_00_00l);
		return id;
	}

	public static String nextPassword() {
		String password;
		do {
			password = generateRandomString();
		} while (!isValidPassword(password));
		return password;
	}

	private static String generateRandomString() {
		int length = nextInt(PASS_MIN_LENGTH, PASS_MAX_LENGTH + 1);
		StringBuilder password = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			password.append(PASS_CHAR_SET.charAt(get().nextInt(PASS_CHAR_SET.length())));
		}
		return password.toString();
	}

	private static boolean isValidPassword(String password) {
		return PASSWORD_PATTERN.matcher(password).matches();
	}
}
