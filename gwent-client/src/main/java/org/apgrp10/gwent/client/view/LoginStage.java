package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.Severity;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.Random;

import static io.github.palexdev.mfxcore.validation.Validated.INVALID_PSEUDO_CLASS;
import static org.apgrp10.gwent.client.controller.FieldValidator.*;

public class LoginStage extends AbstractStage {
	private static LoginStage INSTANCE;
	private Label username_v, password_v, nickname_v, email_v, sec_v, code_v;
	private MFXButton passRand, btnLeft, btnRight, btnBelow;
	private MFXComboBox<String> secQ;
	private MFXTextField username, password, nickname, email, secAns, code;
	private HBox passPane, secPane;
	private CheckBox stayLogged;

	private LoginStage() {
		super("Login Gwent", null);  // TODO: icon
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of LoginStage");
	}

	public static LoginStage getInstance() {
		if (INSTANCE == null) INSTANCE = new LoginStage();
		return INSTANCE;
	}

	private void resetField(MFXTextField field, Label validation) {
		field.setText("");
		validation.setText("");
		field.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
	}

	private void setNodesEnabled(boolean enable, Node... nodes) {
		for (Node node : nodes) {
			node.setManaged(enable);
			node.setVisible(enable);
			node.setDisable(!enable);
			if (node.equals(passRand)) password.setPrefWidth(enable ? 300 : 400);
		}
	}

	private void setNext(MFXTextField field, Node nextField) {
		field.setOnKeyReleased(e -> {
			if (e.getCode().getName().equals("Enter") && field.getValidator().isValid())
				nextField.requestFocus();
		});
	}

	private void resetAllFields() {
		resetField(password, password_v);
		resetField(nickname, nickname_v);
		resetField(email, email_v);
		resetField(secAns, sec_v);
		resetField(code, code_v);
	}

	private void prepareViews() {
		username = lookup("#username");
		username_v = lookup("#username_v");
		configureConstraints(username, username_v, nonEmpty(username),
				minimumLength(username, 4),
				maximumLength(username, 20),
				customRegex(username, "Must contain only letters, digits, and underscores",
						"^[a-zA-Z0-9_]*$"));
		/////////////////////////////////////
		passPane = lookup("#passPane");
		password = lookup("#password");
		password_v = lookup("#password_v");
		passRand = lookup("#passRand");
		setOnPressListener(passRand, event -> {
			password.setText(makeRandomPassword());
			((MFXPasswordField) password).setShowPassword(true);
		});
		configureConstraints(password, password_v, nonEmpty(password),
				minimumLength(password, 8),
				maximumLength(password, 20),
				haveLowerUpperCase(password),
				haveNumericChar(password),
				haveSpecialChar(password),
				haveNotForbiddenChar(password, " "));
		/////////////////////////////////////
		nickname = lookup("#nickname");
		nickname_v = lookup("#nickname_v");
		configureConstraints(nickname, nickname_v, nonEmpty(nickname),
				haveNotForbiddenChar(nickname, "\"'/\\"));
		/////////////////////////////////////
		email = lookup("#email");
		email_v = lookup("#email_v");
		configureConstraints(email, email_v, emailFormat(email));
		/////////////////////////////////////
		secPane = lookup("#secPane");
		secQ = lookup("#secQ");
		secAns = lookup("#secAns");
		sec_v = lookup("#sec_v");
		secQ.setItems(FXCollections.observableArrayList(
				"Favorite color?",
				"Favorite food?",
				"Your Cousin Name?",
				"Birth Town?"));
		configureConstraints(secAns, sec_v, nonEmpty(secAns),
				new Constraint(Severity.ERROR, "Select a security question",
						Bindings.createBooleanBinding(() -> secQ.getValue() != null, secQ.valueProperty())));
		/////////////////////////////////////
		code = lookup("#code");
		code_v = lookup("#code_v");
		configureConstraints(code, code_v, nonEmpty(code),
				minimumLength(code, 6),
				maximumLength(code, 6));
		/////////////////////////////////////
		btnLeft = lookup("#btnLeft");
		btnRight = lookup("#btnRight");
		btnBelow = lookup("#btnBelow");
		stayLogged = lookup("#remember");
	}

	private void initLogin() {
		resetAllFields();
		btnLeft.setText("Login");
		btnRight.setText("Register");
		btnBelow.setText("Forgot Password?");

		setNodesEnabled(true, username, username_v, passPane, password, password_v, stayLogged, btnBelow);
		setNodesEnabled(false, passRand, nickname, nickname_v, email, email_v, secPane, secQ, secAns, sec_v, code, code_v);

		setNext(username, password);
		setNext(password, btnLeft);
		username.requestFocus();

		setOnPressListener(btnLeft, event -> {
			// Validate and send login request
			boolean valid = !username.getText().isBlank() && !password.getText().isBlank();
			if (valid) {
				// Send login request
				UserController.sendLoginRequest(username.getText(), password.getText(), res -> {
					if (res.isOk())
						initVerify();
					else if (res.getStatus() == Response.NOT_FOUND)
						showAlert(Dialogs.ERROR(), "User not found", "The username you entered does not exist!");
					else if (res.getStatus() == Response.UNAUTHORIZED)
						showAlert(Dialogs.ERROR(), "Incorrect Password", "The password you entered is incorrect!");
					else
						showAlert(Dialogs.ERROR(), "Server Exception", "An error occurred while trying to login!\n" + res.getBody().get("message").getAsString());
				});
			} else {
				showAlert(Dialogs.ERROR(), "Invalid Inputs", "Your inputs are not valid!");
			}
		});

		setOnPressListener(btnRight, event -> {
			// Register
			initRegister();
		});

		setOnPressListener(btnBelow, event -> {
			// Forgot Password
			initForgot();
		});
	}

	private void initVerify() {
		btnLeft.setText("Verify");
		btnRight.setText("Resend Code");
		btnBelow.setText("Cancel");

		setNodesEnabled(false, passPane, nickname, nickname_v, email, email_v, secPane, sec_v);
		setNodesEnabled(true, code, code_v);

		username.setDisable(true);
		code.requestFocus();
		setNext(code, btnLeft);

		setOnPressListener(btnLeft, event -> {
			// Validate and send verification code
			boolean valid = code.getValidator().isValid();
			if (valid) {
				// Send verification code
				UserController.completeLogin(code.getText(), stayLogged.isSelected(), res -> {
					if (res.isOk()) {
						MainStage.getInstance().start();
						close();
					} else if (res.getStatus() == Response.UNAUTHORIZED)
						showAlert(Dialogs.ERROR(), "Incorrect Code", "The code you entered is incorrect!");
					else
						showAlert(Dialogs.ERROR(), "Server Exception", "An error occurred while trying to verify!\n"
						                                               + res.getBody().get("message").getAsString());
				});
			} else {
				showAlert(Dialogs.ERROR(), "Invalid Code", "The code you entered is not valid!");
			}
		});

		setOnPressListener(btnRight, event -> {
			// Resend Code
			UserController.sendLoginRequest(username.getText(), password.getText(), res -> {
				if (res.isOk())
					showAlert(Dialogs.INFO(), "Code Sent", "A new verification code has been sent to your email!");
				else
					showAlert(Dialogs.ERROR(), "Server Exception", "An error occurred while trying to resend code!\n"
					                                               + res.getBody().get("message").getAsString());
			});
		});

		setOnPressListener(btnBelow, event -> {
			// Cancel
			initLogin();
		});
	}

	private void initForgot() {
		resetAllFields();
		btnLeft.setText("Send Code");
		btnRight.setText("Reset Password");
		btnBelow.setText("Cancel");
		btnBelow.setManaged(true);
		btnBelow.setVisible(true);
	}

	private void initReset() {
		btnLeft.setText("Reset");
		btnRight.setText("Cancel");
		btnBelow.setManaged(false);
		btnBelow.setVisible(false);
	}

	private void initRegister() {
		resetAllFields();
		btnLeft.setText("Register");
		btnRight.setText("Cancel");


		setNodesEnabled(true, username, username_v, passPane, password, password_v, passRand,
				nickname, nickname_v, email, email_v, secPane, secQ, secAns, sec_v);
		setNodesEnabled(false, stayLogged, code, code_v, btnBelow);

		username.requestFocus();
		setNext(username, password);
		setNext(password, nickname);
		setNext(nickname, email);
		setNext(email, secQ);
		setNext(secAns, btnLeft);

		setOnPressListener(btnLeft, event -> {
			// Validate and send register request
			boolean valid = username.getValidator().isValid() && password.getValidator().isValid()
			                && nickname.getValidator().isValid() && email.getValidator().isValid()
			                && secAns.getValidator().isValid();
			if (valid) {
				// Send register request
				UserController.sendRegisterRequest(username.getText(), password.getText(), nickname.getText(),
						email.getText(), secQ.getValue(), secAns.getText(), res -> {
							if (res.isOk()) {
								showAlert(Dialogs.INFO(), "Registered Successfully",
										"An email has been sent to you with a verification link!");
								initLogin();
							} else if (res.getStatus() == Response.CONFLICT) {
								// show a conflict dialog
								String newRandom = username.getText() + "_" + Integer.toHexString(Random.nextInt(0, 256));
								boolean result = showConfirmDialog(Dialogs.WARN(), "Username Conflict",
										("The username '%s' is already taken!\n" +
										 "Do you want to try use '%s'?").formatted(username.getText(), newRandom),
										"Yes", "No");

								if (result) {
									// Register with new random username
									username.setText(newRandom);
									Platform.runLater(() -> btnLeft.getOnMouseClicked().handle(emptyMouseEvent()));
								}
							} else
								showAlert(Dialogs.ERROR(), "Server Exception", "An error occurred while trying to register!\n"
								                                               + res.getBody().get("message").getAsString());
						});
			} else {
				showAlert(Dialogs.ERROR(), "Invalid Inputs", "Your inputs are not valid!");
			}
		});

		setOnPressListener(btnRight, event -> {
			// Cancel
			initLogin();
		});

		setOnPressListener(btnBelow, null);
	}

	@Override
	protected boolean onCreate() {
		setScene(R.scene.login);
		prepareViews();
		resetField(username, username_v);
		resetAllFields();
		initLogin();
		return true;
	}
}
