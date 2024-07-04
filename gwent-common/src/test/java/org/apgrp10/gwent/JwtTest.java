package org.apgrp10.gwent;

import com.google.gson.JsonObject;
import org.apgrp10.gwent.utils.SecurityUtils;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTest {
	private static final String TEST_SECRET_KEY = "mysecretkey12345";

	@Test
	public void testMakeJWT() {
		JsonObject payload = new JsonObject();
		payload.addProperty("sub", "1234567890");
		payload.addProperty("name", "John Doe");
		payload.addProperty("iat", 1516239022);

		String jwt = SecurityUtils.makeJWT(payload, TEST_SECRET_KEY);
		assertNotNull(jwt);

		String[] parts = jwt.split("\\.");
		assertEquals(3, parts.length);

		String headerJson = new String(Base64.getDecoder().decode(parts[0]));
		assertEquals("{\"alg\":\"HS256\",\"typ\":\"JWT\"}", headerJson);

		String payloadJson = new String(Base64.getDecoder().decode(parts[1]));
		assertEquals(payload.toString(), payloadJson);
	}

	@Test
	public void testVerifyJWT() {
		JsonObject payload = new JsonObject();
		payload.addProperty("sub", "1234567890");
		payload.addProperty("name", "John Doe");
		payload.addProperty("iat", 1516239022);

		String jwt = SecurityUtils.makeJWT(payload, TEST_SECRET_KEY);
		assertTrue(SecurityUtils.verifyJWT(jwt, TEST_SECRET_KEY));
	}

	@Test
	public void testVerifyJWTInvalidSignature() {
		JsonObject payload = new JsonObject();
		payload.addProperty("sub", "1234567890");
		payload.addProperty("name", "John Doe");
		payload.addProperty("iat", 1516239022);

		String jwt = SecurityUtils.makeJWT(payload, TEST_SECRET_KEY);
		String[] parts = jwt.split("\\.");
		String modifiedJwt = parts[0] + "." + parts[1] + "." + "invalidsignature";

		assertFalse(SecurityUtils.verifyJWT(modifiedJwt, TEST_SECRET_KEY));
	}

	@Test
	public void testVerifyJWTInvalidToken() {
		String invalidJwt = "invalid.jwt.token";
		assertFalse(SecurityUtils.verifyJWT(invalidJwt, TEST_SECRET_KEY));
	}

}
