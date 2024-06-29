package org.apgrp10.gwent.client;

import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ClientMain {
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		
		Gwent.main(args);
	}
}
