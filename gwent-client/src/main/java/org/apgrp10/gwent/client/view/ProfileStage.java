package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.FieldValidator;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.model.AvatarView;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.apgrp10.gwent.client.controller.FieldValidator.*;

public class ProfileStage extends AbstractStage {

	private static ProfileStage INSTANCE;
	private boolean pass_edited;
	private MFXTextField email;
	private MFXPasswordField password;
	private MFXTextField nickname;
	private MFXTextField username;
	private AvatarView avatarView;

	private ProfileStage() {
		super("Profile", null);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of ProfileStage");
		initOwner(MainStage.getInstance());
		initModality(Modality.WINDOW_MODAL);
	}

	public static ProfileStage getInstance() {
		if (INSTANCE == null) INSTANCE = new ProfileStage();
		return INSTANCE;
	}

	@Override
	protected boolean onCreate() {
		setScene(R.scene.profile);

		setOnPressListener("#cancel", e -> close());
		setOnPressListener("#delete_acc", e -> showDialogAndWait(Dialogs.WARN(), "Delete Account",
				"Are you sure you want to delete your account?",
				Map.entry("#Yes", e1 -> UserController.deleteAccount(res -> {
					if (res.isOk()) {
						showAlert(Dialogs.INFO(), "Account Deleted!", "Your account has been deleted successfully!");
						Gwent.forEachStage(Stage::close);
						LoginStage.getInstance().start();
					} else
						showAlert(Dialogs.ERROR(), "Error!", "An error occurred while deleting your account!");
				})),
				Map.entry("#No", e1 -> {})));

		avatarView = lookup("#avatar");
		setOnPressListener("#change_avatar", e -> {
			Avatar newAvatar = new AvatarChooser(UserController.getCurrentUser().avatar()).chooseAvatar();
			if (newAvatar != null) {
				avatarView.setAvatar(newAvatar);
				((Button) lookup("#save")).setDisable(false);
			}
		});

		username = lookup("#username");
		setOnPressListener("#edit_usern", e -> {
			configureConstraints(username, lookup("#username_v"), nonEmpty(username),
					minimumLength(username, 4),
					maximumLength(username, 20),
					customRegex(username, "Must contain only letters, digits, and underscores",
							"^[a-zA-Z0-9_]*$"));
			openForEdit(username);
		});

		nickname = lookup("#nickname");

		setOnPressListener("#edit_nickn", e -> {
			configureConstraints(nickname, lookup("#nickname_v"), nonEmpty(nickname),
					haveNotForbiddenChar(nickname, "\"'/\\"));
			openForEdit(nickname);
		});

		password = lookup("#password");
		setOnPressListener("#edit_pass", e -> {
			String inputPass = FieldValidator.passwordCheckingDialog(this, "Enter your password");
			if (inputPass == null) return;
			if (!UserController.getCurrentUser().isPassHashCorrect(User.hashPassword(inputPass))) {
				showAlert(Dialogs.ERROR(), "Error!", "The entered password is incorrect!");
				return;
			}
			configureConstraints(password, lookup("#password_v"),
					nonEmpty(password),
					minimumLength(password, 8),
					maximumLength(password, 20),
					haveLowerUpperCase(password),
					haveNumericChar(password),
					haveSpecialChar(password),
					haveNotForbiddenChar(password, " "));
			pass_edited = true;
			password.setText("");
			openForEdit(password);
		});

		email = lookup("#email");
		setOnPressListener("#edit_email", e -> {
			configureConstraints(email, lookup("#email_v"), emailFormat(email));
			openForEdit(email);
		});

		setOnPressListener("#save", e -> {
			if (!username.getValidator().isValid() ||
			    !nickname.getValidator().isValid() ||
			    !password.getValidator().isValid() ||
			    !email.getValidator().isValid()) {
				showAlert(Dialogs.ERROR(), "Error!", "Your inputs are not valid or incomplete!");
				return;
			}

			User userObj = UserController.getCurrentUser();
			User.PublicInfo newInfo = new User.PublicInfo(userObj.id(), username.getText(), nickname.getText(), avatarView.getAvatar());
			UserController.updateUser(newInfo, res -> {
				if (res.isOk())
					showAlert(Dialogs.INFO(), "Public info updated!", "Public Info (Username, Nickname, Avatar) updated successfully!");
				else if (res.getStatus() == Response.CONFLICT)
					showAlert(Dialogs.ERROR(), "Error!", "Username already exists!");
				else
					showAlert(Dialogs.ERROR(), "Error!", "An error occurred while updating your public info!");
			});
			boolean wait = true;
			if (!email.getText().equals(userObj.email()))
				UserController.changeEmailRequest(email.getText(), res -> {
					if (res.isOk())
						showAlert(Dialogs.INFO(), "Email Change Requested!", "An email has been sent to your new email address, please verify it!");
					else
						showAlert(Dialogs.ERROR(), "Error!", "An error occurred while changing your email!");
					if (!pass_edited) close();
				});
			else if (!pass_edited) wait = false;
			if (pass_edited)
				UserController.changePassword(userObj, password.getText(), res -> {
					if (res.isOk())
						showAlert(Dialogs.INFO(), "Password Changed!", "Your password has been changed successfully!");
					else
						showAlert(Dialogs.ERROR(), "Error!", "An error occurred while changing your password!");
					close();
				});
			else wait = false;
			if (!wait) close();
		});

		updateInformation();
		return true;
	}

	@Override
	protected void updateInformation() {
		avatarView.setAvatar(UserController.getCurrentUser().avatar());
		username.setText(UserController.getCurrentUser().username());
		nickname.setText(UserController.getCurrentUser().nickname());
		password.setText("●●●●●●●●");
		email.setText(UserController.getCurrentUser().email());
		closeField(username);
		closeField(nickname);
		closeField(password);
		closeField(email);
	}

	private void closeField(MFXTextField field) {
		field.setEditable(false);
		field.pseudoClassStateChanged(PseudoClass.getPseudoClass("editable"), false);
	}

	private void openForEdit(MFXTextField field) {
		field.setEditable(true);
		field.pseudoClassStateChanged(PseudoClass.getPseudoClass("editable"), true);
		field.setOnKeyReleased(ev -> {
			if (ev.getCode().getName().equals("Enter")) {
				field.setEditable(false);
				((Button) lookup("#save")).requestFocus();
			}
			((Button) lookup("#save")).setDisable(false);
		});
		field.requestFocus();
	}


	@Override
	protected void onCloseRequest(WindowEvent event) {
		((Button) lookup("#cancel")).fireEvent(emptyMouseEvent());
	}

	public static class AvatarChooser {
		private final ArrayList<Avatar> allAvatars;
		private final Pane paneRoot;
		private AvatarView selectedAvatarView;

		public AvatarChooser(Avatar initialAvatar) {
			paneRoot = (Pane) R.scene.avatar.getRoot();

			AvatarView avatarView = (AvatarView) paneRoot.lookup("#avatar");
			GridPane avatarGrid = (GridPane) ((MFXScrollPane) paneRoot.lookup("#avatarGridScrollPane")).getContent();
			MFXButton browseBtn = (MFXButton) paneRoot.lookup("#browseBtn");

			avatarView.setAvatar(initialAvatar);

			allAvatars = new ArrayList<>(Avatar.allAvatars);
			loadAllAvatars(avatarView, avatarGrid);

			initDragNDrop(avatarView, avatarGrid);
			initBrowsingButton(avatarView, avatarGrid, browseBtn);
		}

		private void initDragNDrop(AvatarView avatarView, GridPane avatarGrid) {
			avatarGrid.setOnDragOver(e -> {
				if (e.getDragboard().hasFiles()) {
					String path = e.getDragboard().getFiles().get(0).toURI().toString();
					if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
						e.acceptTransferModes(TransferMode.COPY);
						return;
					}
				}
				e.consume();
			});

			avatarGrid.setOnDragDropped(e -> {
				List<File> files = e.getDragboard().getFiles();
				if (files.size() != 1) return;
				String path = files.get(0).toURI().toString();
				//check extension
				if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
					Image image = new Image(path);
					avatarView.setAvatar(addAvatar(image));
					loadAllAvatars(avatarView, avatarGrid);
				} else
					e.consume();
			});
		}

		private void initBrowsingButton(AvatarView avatarView, GridPane avatarGrid, MFXButton browseBtn) {
			browseBtn.setOnMouseClicked(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
				File file = fileChooser.showOpenDialog(avatarGrid.getScene().getWindow());
				if (file != null) {
					Image image = new Image(file.toURI().toString());
					avatarView.setAvatar(addAvatar(image));
					loadAllAvatars(avatarView, avatarGrid);
				}
			});
		}

		private void loadAllAvatars(AvatarView mainAvatarView, GridPane avatarGrid) {
			avatarGrid.getChildren().clear();
			int column = 0;
			int row = 0;
			for (Avatar avatar : allAvatars) {
				avatarGrid.add(
						new AvatarView(avatar) {{
							setPrefWidth(avatarGrid.getPrefWidth() / 4 - 10);
							getStyleClass().add("mfx-button");
							if (avatar.equals(mainAvatarView.getAvatar())) {
								pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
								selectedAvatarView = this;
							}
							setOnMouseClicked(e -> {
								if (selectedAvatarView != null)
									selectedAvatarView.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
								selectedAvatarView = this;
								pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
								mainAvatarView.setAvatar(getAvatar());
							});
						}}, column, row);

				if (++column > 3) {
					column = 0;
					row++;
				}
			}
		}

		private Avatar addAvatar(Image image) {
			Avatar newAvatar = Avatar.fromImage(image);
			allAvatars.add(newAvatar);
			return newAvatar;
		}

		public Avatar chooseAvatar() {
			AtomicReference<Avatar> result = new AtomicReference<>();

			Dialogs.showDialogAndWait(ProfileStage.getInstance(), Dialogs.INFO(), "Choose Avatar", paneRoot, Orientation.HORIZONTAL,
					Map.entry("#OK", e -> result.set(selectedAvatarView.getAvatar())),
					Map.entry("#Cancel", e -> {}));

			return result.get();
		}
	}
}
