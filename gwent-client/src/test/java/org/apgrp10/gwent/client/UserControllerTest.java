package org.apgrp10.gwent.client;

import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.WaitExec;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserControllerTest {

	@Test
	public void userInFirstIsNull() {
		assert UserController.getCurrentUser() == null;
	}

	@Test
	public void disconnect() {
		UserController.onDisconnect();
		assert UserController.getCurrentUser() == null;
	}

	@Test
	public void loadJWT() {
		String newFilePath = (Gwent.APP_DATA + "jwt.txt");
		File f = new File(newFilePath);
		if (f.exists() && !f.isDirectory()) {
			Assertions.assertNotNull(UserController.loadJWTFromFile());
		} else {
			Assertions.assertNull(UserController.loadJWTFromFile());
		}
	}

	@Test
	public void saveJWT() {
		String newFilePath = (Gwent.APP_DATA + "jwt.txt");
		File f = new File(newFilePath);
		if (f.exists() && !f.isDirectory()) {
			assert true;
		} else {
			assert false;
		}
	}

	@Test
	public void authenticate() {
		UserController.performAuthentication(false);
		String newFilePath = (Gwent.APP_DATA + "jwt.txt");
		File f = new File(newFilePath);
		if (f.exists() && !f.isDirectory()) {
			Assertions.assertNull(UserController.getCurrentUser());
		} else {
			Assertions.assertNotNull(UserController.getCurrentUser());
		}
	}

	@Test
	public void updateInfo() {
		//should make exception in null values
		try {
			UserController.updateUser(null, null);
			assert false;
		} catch (Exception e) {
			assert true;
		}
	}

	@Test
	public void registerTest() {
		//should not make exception in null values
		UserController.sendRegisterRequest("new", "new", "new", "new", null, "m", null);
		assert true;
	}

	@Test
	public void LoginTest() {
		//should not make exception in null values
		UserController.sendLoginRequest("new", "new", null);
		UserController.verifyLogin("2121", false, null);
		UserController.verifyLogin("2121", true, null);
		assert true;
	}

	@Test
	public void forgotPass() {
		//incorrect seQ
		UserController.requestForgetPasswordVerifyCode("new", "new", "where is your favorite city", "tehran", null);
		UserController.verifyForgetPassword("2121", null);
		UserController.resetPassword("new Pass", null);
		if (UserController.getCurrentUser() != null)
			Assertions.assertNotEquals(UserController.getCurrentUser().passwordHash(), "new Pass");

	}

	@Test
	public void logout() {
		UserController.logout();
		if (UserController.getCurrentUser() == null)
			assert true;
		else
			assert false;
	}

	@Test
	public void changeInfo() {
		try {
			UserController.changePassword(null, "", null);
			Assertions.fail();
		} catch (Exception e) {}
		if (UserController.getCurrentUser() != null)
			UserController.changeEmailRequest("new email", null);
		assert true;
	}
}
