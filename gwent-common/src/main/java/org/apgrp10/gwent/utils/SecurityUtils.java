package org.apgrp10.gwent.utils;

import com.google.gson.JsonObject;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.security.*;
import java.util.Base64;

public class SecurityUtils {
	private static final String ALGORITHM_RSA = "RSA";

	public static KeyPair generateKeyPair() {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
			keyGen.initialize(2048);
			return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String sha256(String input) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] hash = digest.digest(input.getBytes());
		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static String hmacSha256(String key, String s) {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			sha256_HMAC.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
			return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(s.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String makeJWT(JsonObject payload, String key) {
		String header = MGson.toJson(MGson.makeJsonObject("alg", "HS256", "typ", "JWT"));
		String payloadStr = MGson.toJson(payload);
		String headerBase64 = Base64.getEncoder().encodeToString(header.getBytes());
		String payloadBase64 = Base64.getEncoder().encodeToString(payloadStr.getBytes());
		String finalJwt = headerBase64 + "." + payloadBase64;
		finalJwt += '.' + Base64.getEncoder().encodeToString(hmacSha256(key, finalJwt).getBytes());
		return finalJwt;
	}

	public static JsonObject verifyJWT(String jwt, String key) {
		String[] parts = jwt.split("\\.");
		if (parts.length != 3) return null;
		String header = parts[0];
		String payload = parts[1];
		String signature = parts[2];
		String headerPayload = header + "." + payload;
		String expectedSignature = Base64.getEncoder().encodeToString(hmacSha256(key, headerPayload).getBytes());
		if(expectedSignature.equals(signature))
			return MGson.fromJson(new String(Base64.getDecoder().decode(payload)), JsonObject.class);
		return null;
	}
}
