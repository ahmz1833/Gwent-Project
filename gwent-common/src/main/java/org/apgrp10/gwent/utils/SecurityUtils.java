package org.apgrp10.gwent.utils;

import javax.crypto.Cipher;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.security.*;

public class SecurityUtils {
	private static final String ALGORITHM_RSA = "RSA";
	
	public static KeyPair generateKeyPair() {
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
			keyGen.initialize(2048);
			return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] encrypt(PublicKey key, String message)
			throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(message.getBytes());
	}
	
	public static byte[] decrypt(PrivateKey key, byte[] encryptMessage)
			throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(encryptMessage);
	}
	
	public static String sha256Hash(String input)
			throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(input.getBytes());
		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	public static SSLServerSocketFactory getSSLServerSocketFactory() {
		return (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	}
	
	public static SSLSocketFactory getSSLSocketFactory() {
		return (SSLSocketFactory) SSLSocketFactory.getDefault();
	}
}