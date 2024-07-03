open module gwent.server {
	requires com.google.gson;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.media;
	requires java.desktop;
	requires gwent.common;
	requires com.auth0.jwt;
	requires java.sql;
	requires jdk.httpserver;
	exports org.apgrp10.gwent.server;
}