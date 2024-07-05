package org.apgrp10.gwent.client;

import java.util.Locale;

import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.utils.ANSI;

public class ClientMain {
	public static void main(String[] args) {
		// Locale.setDefault(Locale.ENGLISH);
		// ANSI.log("Trying to connect to server", ANSI.LYELLOW, false);
		// Server.connect();

		//while (true) {
		//	Server.instance().sendRequest(new Request("hello"), res -> {
		//		System.out.println(res.getBody().get("msg").getAsString());
		//	});
		//	try {Thread.sleep(2000);} catch (Exception e) {}
		//}

		Gwent.main(args);
	}
}
