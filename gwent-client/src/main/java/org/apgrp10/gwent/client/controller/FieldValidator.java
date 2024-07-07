package org.apgrp10.gwent.client.controller;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import io.github.palexdev.materialfx.enums.FloatMode;
import io.github.palexdev.materialfx.utils.StringUtils;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.Severity;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apgrp10.gwent.client.view.AbstractStage;
import org.apgrp10.gwent.client.view.Dialogs;

import java.util.List;

import static io.github.palexdev.materialfx.validation.Validated.INVALID_PSEUDO_CLASS;

public class FieldValidator {

	public static Constraint nonEmpty(TextField textField) {
		return new Constraint(Severity.ERROR, "Mustn't be blank!",
				Bindings.createBooleanBinding(() -> !textField.getText().isBlank(), textField.textProperty()));
	}

	public static Constraint minimumLength(TextField textField, int minLen) {
		return new Constraint(Severity.ERROR, "Must be at least " + minLen + " characters long",
				textField.textProperty().length().greaterThanOrEqualTo(minLen));
	}

	public static Constraint maximumLength(TextField textField, int maxLen) {
		return new Constraint(Severity.ERROR, "Must be at most " + maxLen + " characters long",
				textField.textProperty().length().lessThanOrEqualTo(maxLen));
	}

	public static Constraint haveLowerUpperCase(TextField textField) {
		return new Constraint(Severity.ERROR, "Must contain at least one lowercase and one uppercase characters",
				Bindings.createBooleanBinding(() -> textField.getText().matches(
						"(?=.*[a-z].*)(?=.*[A-Z].*)"
				), textField.textProperty()));
	}

	public static Constraint haveNumericChar(TextField textField) {
		return new Constraint(Severity.ERROR, "Must contain at least one digit",
				Bindings.createBooleanBinding(() -> textField.getText().matches(".*\\d.*"), textField.textProperty()));
	}

	public static Constraint haveSpecialChar(TextField textField) {
		return new Constraint(Severity.ERROR, "Must contain at least one special character",
				Bindings.createBooleanBinding(() -> textField.getText().matches(".*[!@#&()–{}:;',?/*~$^+=<>-].*"), textField.textProperty()));
	}

	public static Constraint haveNotForbiddenChar(TextField textField, String forbiddenChars) {
		return new Constraint(Severity.ERROR, "Mustn't contain any of : '" + forbiddenChars + "'",
				Bindings.createBooleanBinding(() -> !StringUtils.containsAny(textField.getText(), "", forbiddenChars.split("(?<=.)(?=.)")), textField.textProperty()));
	}

	public static Constraint customRegex(TextField textField, String errorMessage, String regex) {
		return new Constraint(Severity.ERROR, errorMessage,
				Bindings.createBooleanBinding(() -> textField.getText().matches(regex), textField.textProperty()));
	}

	public static Constraint emailFormat(TextField textField) {
		return new Constraint(Severity.ERROR, "Invalid email format",
				Bindings.createBooleanBinding(() -> textField.getText().matches(
						"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
				), textField.textProperty()));
	}

	public static void configureConstraints(MFXTextField textField, Label validationLbl, Constraint... constraints) {
		textField.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
		textField.validate().forEach(textField.getValidator()::removeConstraint);

		for (Constraint constraint : constraints)
			textField.getValidator().constraint(constraint);

		textField.getValidator().validProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				validationLbl.setVisible(false);
				textField.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
			}
		});

		textField.delegateFocusedProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue && !newValue) {
				List<Constraint> _constraints = textField.validate();
				if (!_constraints.isEmpty()) {
					textField.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, true);
					validationLbl.setText(_constraints.get(0).getMessage());
					validationLbl.setVisible(true);
				}
			}
		});
	}

	public static String makeRandomPassword() {
		// TODO: implement
		return "1234AA@aa";
	}

	public static String passwordCheckingDialog(AbstractStage stage, String title) {
		MFXPasswordField passwordConfirmationField = new MFXPasswordField();
		passwordConfirmationField.setFloatMode(FloatMode.BORDER);
		passwordConfirmationField.setFloatingText(title);
		passwordConfirmationField.setPrefColumnCount(20);

		boolean result = Dialogs.showConfirmDialog(stage, Dialogs.INFO, title,
				passwordConfirmationField, "OK", "Cancel");
		if (!result) return null;
		return passwordConfirmationField.getText();
	}
}
