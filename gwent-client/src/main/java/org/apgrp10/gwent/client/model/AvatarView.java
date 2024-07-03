package org.apgrp10.gwent.client.model;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.model.Avatar;

public class AvatarView extends StackPane
{
	private final ImageView imageView;
	private Avatar avatar;
	private final Circle clip;
	
	public AvatarView(Avatar avatar)
	{
		this.avatar = avatar;
		imageView = new ImageView();
		imageView.setImage(avatar.getViewableImage());
		bindDimensions();
		clip = new Circle();
		imageView.setClip(clip);
		initializeClipBindings();
		imageView.setPreserveRatio(true);
		imageView.fitWidthProperty().setValue(50);
		this.getChildren().add(imageView);
		this.getStyleClass().add("avatar-view");
	}
	
	public AvatarView()
	{
		this(Avatar.random());
	}
	
	private void bindDimensions()
	{
		this.prefWidthProperty().bindBidirectional(imageView.fitWidthProperty());
		this.prefHeightProperty().bindBidirectional(this.prefWidthProperty());
		this.minWidthProperty().bindBidirectional(this.prefWidthProperty());
		this.minHeightProperty().bindBidirectional(this.prefHeightProperty());
		this.maxWidthProperty().bindBidirectional(this.prefWidthProperty());
		this.maxHeightProperty().bindBidirectional(this.prefHeightProperty());
	}
	
	private void initializeClipBindings()
	{
		imageView.fitHeightProperty().bindBidirectional(imageView.fitWidthProperty());
		clip.radiusProperty().bind(imageView.fitWidthProperty().divide(2));
		clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
		clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2));
	}
	
	public Avatar getAvatar()
	{
		return avatar;
	}
	
	public void setAvatar(Avatar avatar)
	{
		this.avatar = avatar;
		if (avatar == null) imageView.setImage(R.getImage("ic_profile.png"));
		else imageView.setImage(avatar.getViewableImage());
	}
}