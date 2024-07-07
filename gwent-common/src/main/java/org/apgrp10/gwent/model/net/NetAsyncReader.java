package org.apgrp10.gwent.model.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apgrp10.gwent.model.AbstractAsyncReader;
import org.apgrp10.gwent.utils.ANSI;

public class NetAsyncReader extends AbstractAsyncReader {
	// TODO: move these two to a better place

	public static byte[] intToBytes(int x) {
		byte ans[] = new byte[4];
		ans[0] = (byte) ((x >> 0) & 0xff);
		ans[1] = (byte) ((x >> 8) & 0xff);
		ans[2] = (byte) ((x >> 16) & 0xff);
		ans[3] = (byte) ((x >> 24) & 0xff);
		return ans;
	}

	public static int bytesToInt(byte[] b) {
		int x = 0;
		x ^= ((int) b[0] & 0xff) << 0;
		x ^= ((int) b[1] & 0xff) << 8;
		x ^= ((int) b[2] & 0xff) << 16;
		x ^= ((int) b[3] & 0xff) << 24;
		return x;
	}

	public NetAsyncReader(InputStream stream, Consumer<byte[]> onReceive, Consumer<Exception> onFailure) {
		super(stream, onReceive, onFailure);
	}

	@Override
	public void run() {
		if (!read())
			return;
		if (receive == null)
			return;

		List<byte[]> data = new ArrayList<>();

		try {
			while (size >= 4) {
				int len = bytesToInt(buf);
				if (len < 0)
					throw new IOException("invalid length");
				if (len + 4 > size)
					break;

				if (len > 0)
					data.add(part(4, len));

				skip(4 + len);
			}
		} catch (Exception e) {
			ANSI.logError(System.err, "NetAsyncReader failure", e);
			failure.accept(e);
		}

		// this must be last because some callbacks might indirectly call run while we are working
		for (byte b[] : data)
			receive.accept(b);
	}
}
