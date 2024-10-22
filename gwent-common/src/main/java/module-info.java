open module gwent.common {
	requires com.google.gson;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.media;
	requires java.desktop;
	requires com.auth0.jwt;
	requires java.sql;
	exports org.apgrp10.gwent.model;
	exports org.apgrp10.gwent.utils;
	exports org.apgrp10.gwent.model.card;
	exports org.apgrp10.gwent.model.net;
	exports org.apgrp10.gwent.controller;
	exports org.apgrp10.gwent.view;
}
