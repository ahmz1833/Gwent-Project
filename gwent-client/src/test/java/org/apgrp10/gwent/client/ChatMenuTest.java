package org.apgrp10.gwent.client;

import org.apgrp10.gwent.client.controller.ChatMenuController;
import org.apgrp10.gwent.client.view.PreGameMenu;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class ChatMenuTest {
	@Test
	public void instanceCheck(){
		ChatMenuController ins = null, ins2 = null;
		try{
				ins = ChatMenuController.getInstance();
		}
		catch (Error ignored){}
		try{
			ChatMenuController.getInstance();
		}
		catch (Error ignored){}
		try{
			ins2 = ChatMenuController.getInstance();
		}
		catch (Error ignored){}
		Assertions.assertEquals(ins2, ins);
	}
	@Test
	public void checkSingleInstance(){
		Class<ChatMenuController> clazz = ChatMenuController.class;
		try {
			 clazz.getDeclaredConstructor().setAccessible(true);
			 clazz.getDeclaredConstructor().newInstance();
			 assert false;
		} catch (Exception ignored){
			assert true;
		}
	}
}
