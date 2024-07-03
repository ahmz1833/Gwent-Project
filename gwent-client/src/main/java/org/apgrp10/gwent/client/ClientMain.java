package org.apgrp10.gwent.client;

import java.util.Locale;

import org.apgrp10.gwent.model.net.Request;

public class ClientMain {
	public static void main(String[] args) {
		//Locale.setDefault(Locale.ENGLISH);
		//if (!Server.connect())
		//	System.exit(1);
		//Server.instance().addOnClose(() -> System.exit(1));
		//Server.instance().startThread();

		//while (true) {
		//	Server.instance().sendRequest(new Request("hello"), res -> {
		//		System.out.println(res.getBody().get("msg").getAsString());
		//	});
		//	try {Thread.sleep(2000);} catch (Exception e) {}
		//}

		Gwent.main(args);
	}
}
