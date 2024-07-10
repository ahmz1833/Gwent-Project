package org.apgrp10.gwent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.SecurityUtils;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.BooleanControl;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

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
		payload.addProperty("sub", 7243168145879485845L);
		payload.addProperty("name", "ahmz");
		payload.addProperty("exp", 1720790323515L);

		String jwt = SecurityUtils.makeJWT(payload, TEST_SECRET_KEY);
		assertNotNull(SecurityUtils.verifyJWT(jwt, TEST_SECRET_KEY));
		System.out.println(SecurityUtils.verifyJWT(jwt, TEST_SECRET_KEY));
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

		assertNull(SecurityUtils.verifyJWT(modifiedJwt, TEST_SECRET_KEY));
	}

	@Test
	public void testVerifyJWTInvalidToken() {
		String invalidJwt = "invalid.jwt.token";
		assertNull(SecurityUtils.verifyJWT(invalidJwt, TEST_SECRET_KEY));
	}
	@Test
	public void testCreatingGson(){

		JsonObject o = MGson.makeJsonObject("int", 2,
				"long", 2L,"boolean", true,"null", null, "char", 'c', "person",
				new Person("abc", 20));
		assert (o.get("long").getAsLong() == 2L);
		assert (o.get("boolean").getAsBoolean());
		assert (o.get("null").isJsonNull());
		Person person = MGson.fromJson(o.get("person"), Person.class);
		assert person.age == 20;
		assert Objects.equals(person.name, "abc");
	}
	@Test
	public void testJsonElement(){
		Person person = new Person("boss", 50);
		ArrayList<Person> persons = new ArrayList<>();
		persons.add(person);
		JsonElement json = MGson.toJsonElement(persons);
		ArrayList<Person> persons1 = MGson.fromJson(json, TypeToken.getParameterized(ArrayList.class, Person.class).getType());
		assert persons1.get(0).name.equals(persons.get(0).name);
	}
	@Test
	public void testGson() {
		Gson builder = MGson.get(true, true);
		String json = MGson.toJson(new Person("aaa", 2));
		Person person = builder.fromJson(json, Person.class);
		assert person.age == 2;
		assert person.name.equals("aaa");
		ArrayList<Person> persons = new ArrayList<>();
		persons.add(person);
		json = MGson.toJson(persons);
		ArrayList<Person> persons1 = MGson.fromJson(json, TypeToken.getParameterized(ArrayList.class, Person.class).getType());
		assert persons1.get(0).name.equals(persons.get(0).name);
	}
	static class Person{
		String name;
		int age;
		Person(String name, int age){
			this.name = name;
			this.age = age;
		}
	}

}
