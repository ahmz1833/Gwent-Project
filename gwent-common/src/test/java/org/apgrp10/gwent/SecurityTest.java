package org.apgrp10.gwent;

import org.apgrp10.gwent.utils.SecurityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PublicKey;

public class SecurityTest {
	@Test
	public void keyGenTest(){
		KeyPair k = null;
		try {
			 k = SecurityUtils.generateKeyPair();
		} catch (Exception e){
			assert false;
		}
		Assertions.assertNotNull(k);
	}
	@Test
	public void encrypt(){
		try {
			Assertions.assertEquals(SecurityUtils.sha256("test"),
					"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08");
		} catch (Exception e) {
			assert false;
		}
	}
	@Test
	public void i(){
		Assertions.assertEquals(SecurityUtils.hmacSha256("test", "salam"),
				"A46yUYvdeZ0j17REYyYnacjLjt2KaL551p75qSUCc1Q=");
	}
}
