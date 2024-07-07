package org.apgrp10.gwent.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.apgrp10.gwent.utils.ANSI;

public class AbstractAsyncReader implements Runnable {
	private InputStream stream;
	protected Consumer<byte[]> receive;
	protected Consumer<Exception> failure;
	protected byte buf[] = new byte[32];
	protected int size = 0;

	public AbstractAsyncReader(InputStream stream, Consumer<byte[]> onReceive, Consumer<Exception> onFailure) {
		this.stream = stream;
		this.receive = onReceive;
		this.failure = onFailure;
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

	// TODO: we can optimize this by using a ring buffer
	protected void skip(int count) {
		System.arraycopy(buf, count, buf, 0, size - count);
		size -= count;
	}

	protected byte[] part(int start, int len) {
		byte ans[] = new byte[len];
		System.arraycopy(buf, start, ans, 0, len);
		return ans;
	}

	public void setOnFailure(Consumer<Exception> cb) {failure = cb;}

	public void setOnReceive(Consumer<byte[]> cb) {receive = cb;}

	protected boolean read() {
		if (failure == null)
			return false; // return so that the possible error isn't lost when we don't have a failure callback
		try {
			int available = stream.available();
			if (available == 0)
				return false;
			byte data[] = new byte[available];

			int k = stream.read(data);
			if (k <= 0)
				throw new IOException("read failed");

			append(data, k);
		} catch (Exception e) {
			ANSI.logError(System.err, "AbstractAsyncReader failure", e);
			failure.accept(e);
			return false;
		}
		return true;
	}

	@Override
	public void run() { read(); }
}
