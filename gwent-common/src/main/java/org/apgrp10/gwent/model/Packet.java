package org.apgrp10.gwent.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public record Packet() {
	
	public static Packet read(InputStream inputStream) {
//		DataInputStream dataInputStream = new DataInputStream(inputStream);
//		// TODO: String type = dataInputStream.readUTF();
		return new Packet();
	}
	
	public void write(OutputStream outputStream) throws Exception {
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		//TODO: dataOutputStream.writeUTF()
	}
}
