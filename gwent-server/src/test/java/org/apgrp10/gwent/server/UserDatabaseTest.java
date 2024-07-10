package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.ANSI;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class UserDatabaseTest {
//
//	@BeforeAll @AfterAll
//	public static void delete(){
//		ANSI.log("Deleting users.db");
//		Files.delete(new File(ServerMain.SERVER_FOLDER + "users.db"));
//	}
//
//	@Test
//	public void testAddUser() throws Exception {
//		User.RegisterInfo userInfo = new User.RegisterInfo(
//				new User.PublicInfo(0, "testUser", "Test Nickname", Avatar.random()),
//				"passwordHash", "test@example.com", "securityQuestion");
//		User user = UserManager.addUser(userInfo);
//		assertNotNull(user);
//		assertEquals(userInfo.username(), user.username());
//		assertEquals(userInfo.nickname(), user.nickname());
//	}
//
//	@Test
//	public void testGetUserByUsername() throws Exception {
//		User.RegisterInfo userInfo = new User.RegisterInfo(
//				new User.PublicInfo(0, "testUser2", "Test Nickname", Avatar.random()),
//				"passwordHash", "test@example.com", "securityQuestion");
//
//		UserManager.addUser(userInfo);
//		User user = UserManager.getUserByUsername("testUser2");
//		assertNotNull(user);
//		assertEquals(userInfo.username(), user.publicInfo().username());
//	}
//
//	@Test
//	public void testGetUserById() throws Exception {
//		User.RegisterInfo userInfo = new User.RegisterInfo(
//				new User.PublicInfo(0, "testUser3", "Test Nickname", Avatar.random()),
//				"passwordHash", "test@example.com", "securityQuestion");
//
//		User addedUser = UserManager.addUser(userInfo);
//		User user = UserManager.getUserById(addedUser.registerInfo().id());
//		assertNotNull(user);
//		assertEquals(userInfo.username(), user.publicInfo().username());
//	}
//
//	@Test
//	public void testIsUsernameTaken() throws Exception {
//		User.RegisterInfo userInfo = new User.RegisterInfo(
//				new User.PublicInfo(0, "testUser4", "Test Nickname", Avatar.random()),
//				"passwordHash", "test@example.com", "securityQuestion");
//
//		UserManager.addUser(userInfo);
//
//		assertTrue(UserManager.isUsernameTaken("testUser4"));
//		assertFalse(UserManager.isUsernameTaken("nonExistentUser"));
//	}
//
//	@Test
//	public void testAddFriend() throws Exception {
//		User.RegisterInfo userInfo1 = new User.RegisterInfo(
//				new User.PublicInfo(0, "testUser5", "Test Nickname 5", Avatar.random()),
//				"passwordHash", "test5@example.com", "securityQuestion");
//		User.RegisterInfo userInfo2 = new User.RegisterInfo(
//				new User.PublicInfo(0, "testUser6", "Test Nickname 6", Avatar.random()),
//				"passwordHash", "test6@example.com", "securityQuestion");
//		User u1 = UserManager.addUser(userInfo1);
//		User u2 = UserManager.addUser(userInfo2);
//		UserManager.addFriendshipRequest(u1.id(),u2.id());
//		UserManager.haveFriendshipRequest(u1.id(), u2.id());
//		UserManager.removeFriendship(u1.registerInfo().id(), u2.registerInfo().id());
//		UserManager.rejectFriendshipRequest(u1.registerInfo().id(), u2.registerInfo().id());
//		UserManager.acceptFriendshipRequest(u1.id(), u2.id());
//	}
//	@Test
//	public void updateInfo() throws Exception {
//		User.RegisterInfo userInfo1 = new User.RegisterInfo(
//				new User.PublicInfo(10, "testUser7", "Test Nickname 5", Avatar.random()),
//				"passwordHash", "test5@example.com", "securityQuestion");
//		User test = UserManager.addUser(userInfo1);
//		UserManager.updateUserInfo(new User.PublicInfo(test.id(), "testUser7", "Test Nickname 6", Avatar.random()));
//		UserManager.updatePassword(test.id(), "123");
//		UserManager.updateEmail(test.id(), "@");
//		User out = UserManager.getUserById(test.id());
//		assert Objects.equals(out.email(), "@");
//		assert out.isPassHashCorrect("123");
//		Assertions.assertFalse(UserManager.isUserOnline(test.id()));
//	}
}