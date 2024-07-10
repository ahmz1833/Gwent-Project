package org.apgrp10.gwent;

import org.apgrp10.gwent.utils.WaitExec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class WETest {
	@Test
	public void waitTest(){
		AtomicBoolean a = new AtomicBoolean(true);
		new WaitExec(true).run(300, () -> a.set(false));
		Assertions.assertFalse(a.get());
	}
}
