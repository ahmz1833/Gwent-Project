package org.apgrp10.gwent;

import org.apgrp10.gwent.model.Message;
import org.apgrp10.gwent.utils.MGson;
import org.junit.jupiter.api.Test;

public class MessageTest {
	@Test
	public void test() throws Exception {
		Message msg = Message.newTextMessage(666, "salam", 616, 420);
		assert msg.getId() == 666;
		msg.setText("lol");
		assert msg.getText().equals("lol");
		assert msg.getNumberOfReaction() == -1;
		assert msg.getUserId() == 616;
		assert msg.getReplyOn() == 420;
		assert msg.getType() == 0;
		assert MGson.fromJson(MGson.toJson(msg), Message.class).toString().equals(msg.toString());
	}
}
