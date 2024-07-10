package org.apgrp10.gwent;

import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.utils.ANSI;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class ANSITest {
	@Test
	public void testAllColors() {
		System.out.print(ANSI.fg(Color.black) + "H");
		System.out.print(ANSI.BLUE.bd() + "H");
		System.out.print(ANSI.BLUE.setBg(Color.GREEN) + "H");
		System.out.print(ANSI.fg(Color.black).toString() + "H");
		System.out.print(ANSI.fg(Color.black).append(ANSI.bg(Color.gray)) + "H");
		ANSI.log("salam");
		ANSI.log("salam", ANSI.BLUE, true);
		Exception e = new Exception();
		ANSI.log(51, System.err, "H", ANSI.CYAN,
				e.getStackTrace());
		ANSI.log(51, System.err, "H",
				e.getStackTrace());
		ANSI.logError(System.err, "", e);
		try {
			ANSI.printErrorResponse("H", ANSI.createErrorResponse(new Request("H"), "s", e));
		}catch (Exception ignored){}

	}
}
