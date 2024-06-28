package org.apgrp10.gwent;

import java.io.InputStream;
import java.net.URL;

public class R {
	public static URL get(String path) {
		return R.class.getResource(path);
	}
	
	public static InputStream getAsStream(String path) {
		return R.class.getResourceAsStream(path);
	}
}
