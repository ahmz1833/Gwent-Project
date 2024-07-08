package org.apgrp10.gwent.server;

public class UserDatabaseTest {
	/*
	private UserDatabase userDB;

	@BeforeEach
	public void setUp() throws Exception {
		userDB = UserDatabase.getInstance();
	}

	@AfterEach
	public void tearDown() throws Exception {
		// Cleanup the database if necessary
	}

	@Test
	public void testAddUser() throws Exception {
		User.RegisterInfo userInfo = new User.RegisterInfo(
				new User.PublicInfo(0, "testUser", "Test Nickname", Avatar.random()),
				"passwordHash", "test@example.com", "securityQuestion");

		User user = userDB.addUser(userInfo);

		assertNotNull(user);
		assertEquals(userInfo.username(), user.publicInfo().username());
		assertEquals(userInfo.nickname(), user.publicInfo().nickname());
	}

	@Test
	public void testGetUserByUsername() throws Exception {
		User.RegisterInfo userInfo = new User.RegisterInfo(
				new User.PublicInfo(0, "testUser2", "Test Nickname", Avatar.random()),
				"passwordHash", "test@example.com", "securityQuestion");

		userDB.addUser(userInfo);

		User user = userDB.getUserByUsername("testUser2");

		assertNotNull(user);
		assertEquals(userInfo.username(), user.publicInfo().username());
	}

	@Test
	public void testGetUserById() throws Exception {
		User.RegisterInfo userInfo = new User.RegisterInfo(
				new User.PublicInfo(0, "testUser3", "Test Nickname", Avatar.random()),
				"passwordHash", "test@example.com", "securityQuestion");

		User addedUser = userDB.addUser(userInfo);
		User user = userDB.getUserById(addedUser.getId());

		assertNotNull(user);
		assertEquals(userInfo.username(), user.publicInfo().username());
	}

	@Test
	public void testIsUsernameTaken() throws Exception {
		User.RegisterInfo userInfo = new User.RegisterInfo(
				new User.PublicInfo(0, "testUser4", "Test Nickname", Avatar.random()),
				"passwordHash", "test@example.com", "securityQuestion");

		userDB.addUser(userInfo);

		assertTrue(userDB.isUsernameTaken("testUser4"));
		assertFalse(userDB.isUsernameTaken("nonExistentUser"));
	}

	@Test
	public void testAddFriend() throws Exception {
		User.RegisterInfo userInfo1 = new User.RegisterInfo(
				new User.PublicInfo(0, "testUser5", "Test Nickname 5", Avatar.random()),
				"passwordHash", "test5@example.com", "securityQuestion");

		User.RegisterInfo userInfo2 = new User.RegisterInfo(
				new User.PublicInfo(0, "testUser6", "Test Nickname 6", Avatar.random()),
				"passwordHash", "test6@example.com", "securityQuestion");

		User u1 = userDB.addUser(userInfo1);
		User u2 = userDB.addUser(userInfo2);

		userDB.addFriendShip(u1.getId(), u2.getId());

		List<Long> friends = userDB.getFriendsIds(u1.getId());
		assertEquals(1, friends.size());
		assertEquals("testUser6", userDB.getUserById(friends.get(0)).publicInfo().username());
	}
	*/
}