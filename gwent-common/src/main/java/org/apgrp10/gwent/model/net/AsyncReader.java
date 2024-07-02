package org.apgrp10.gwent.model.net;

import java.io.IOException;
import java.io.InputStream;

public class AsyncReader implements Runnable {
	private InputStream stream;
	private Callback receive;
	private FailureCallback failure;
	private byte buf[] = new byte[32];
	private int size = 0;
	public AsyncReader(InputStream stream, Callback onReceive, FailureCallback onFailure) {
		this.stream = stream;
		this.receive = onReceive;
		this.failure = onFailure;
	}

	public static byte[] intToBytes(int x) {
		byte ans[] = new byte[4];
		ans[0] = (byte) ((x >> 0) & 0xff);
		ans[1] = (byte) ((x >> 8) & 0xff);
		ans[2] = (byte) ((x >> 16) & 0xff);
		ans[3] = (byte) ((x >> 24) & 0xff);
		return ans;
	}

	// TODO: move these two to a better place

	public static int bytesToInt(byte[] b) {
		int x = 0;
		x ^= (int) b[0] << 0;
		x ^= (int) b[1] << 8;
		x ^= (int) b[2] << 16;
		x ^= (int) b[3] << 24;
		return x;
	}

	private void append(byte b[], int len) {
		while (size + len > buf.length) {
			byte tmp[] = new byte[buf.length * 2];
			System.arraycopy(buf, 0, tmp, 0, size);
			buf = tmp;
		}
		System.arraycopy(b, 0, buf, size, len);
		size += len;
	}

	// TODO: we can optimize this by using an offset for buf
	private void skip(int count) {
		System.arraycopy(buf, count, buf, 0, size - count);
		size -= count;
	}

	private byte[] part(int start, int len) {
		byte ans[] = new byte[len];
		System.arraycopy(buf, start, ans, 0, len);
		return ans;
	}

	public void setOnReceive(Callback cb) {receive = cb;}

	public void setOnFailure(FailureCallback cb) {failure = cb;}

	@Override
	public void run() {
		if (failure == null)
			return; // return so that the possible error isn't lost if we don't have an failure callback
		try {
			int available = stream.available();
			if (available == 0)
				return;
			byte data[] = new byte[available];

			int k = stream.read(data);
			if (k <= 0)
				throw new IOException("read failed");

			append(data, k);

			if (receive == null)
				return;

			while (size >= 4) {
				int len = bytesToInt(buf);
				if (len < 0)
					len = 0;
				if (len + 4 > size)
					break;

				if (len > 0)
					receive.call(part(4, len));

				skip(4 + len);
			}
		} catch (Exception e) {
			failure.call(e);
		}
	}

	public static interface Callback {
		public void call(byte[] data);
	}

	public static interface FailureCallback {
		public void call(Exception e);
	}
}
