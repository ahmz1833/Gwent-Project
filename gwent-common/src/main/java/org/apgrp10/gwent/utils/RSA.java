package org.apgrp10.gwent.utils;

import javax.crypto.Cipher;
import java.security.*;

public class RSA {
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
}