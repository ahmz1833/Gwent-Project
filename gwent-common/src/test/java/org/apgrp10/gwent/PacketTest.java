package org.apgrp10.gwent;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apgrp10.gwent.model.net.PacketHandler;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.MGson;
import org.junit.jupiter.api.Test;

public class PacketTest {
	private static class FakeSocket extends Socket {
		private PipedInputStream in = new PipedInputStream();
		private PipedOutputStream out = new PipedOutputStream();
		private boolean closed;
		private FakeSocket peer;

		@Override public void close() { closed = true; }
		@Override public InetAddress getInetAddress() {
			byte addr[] = {127, 0, 0, 1};
			try {
				return InetAddress.getByAddress(addr);
			} catch (Exception e) {
				assert false;
				return null;
			}
		}
		@Override public InputStream getInputStream() { return in; }
		@Override public OutputStream getOutputStream() { return out; }
		@Override public boolean isClosed() { return peer != null && (peer.closed || closed); }
		@Override public boolean isConnected() { return peer != null; }

		public void connectFake(FakeSocket s) {
			try {
				peer = s;
				s.peer = this;
				in.connect(s.out);
				out.connect(s.in);
			} catch (Exception e) {
				assert false;
			}
		}
	}

	private int cnt;

	@Test
	public void test() {
		FakeSocket s1 = new FakeSocket();
		FakeSocket s2 = new FakeSocket();
		s1.connectFake(s2);

		PacketHandler ph1 = new PacketHandler(s1);
		PacketHandler ph2 = new PacketHandler(s2);

		cnt = 0;

		ph1.setListener("salam", req -> {
			if (req.getBody().get("value").getAsInt() == 66)
				return req.response(Response.OK_NO_CONTENT);
			else
				return req.response(Response.BAD_REQUEST);
		});
		ph2.send(new Request("salam", MGson.makeJsonObject("value", 67)), res -> {
			cnt += res.isOk()? 1: 0;
		});
		ph2.send(new Request("salam", MGson.makeJsonObject("value", 66)), res -> {
			cnt += res.isOk()? 1: 0;
		});
		ph2.ping(() -> cnt += 1);

		for (int i = 0; i < 10; i++) {
			ph1.run();
			ph2.run();
		}

		assert cnt == 2;
	}
}
