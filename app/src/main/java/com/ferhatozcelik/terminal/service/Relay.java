package com.ferhatozcelik.terminal.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import com.ferhatozcelik.terminal.util.IBM437;
import com.ferhatozcelik.terminal.transport.AbsTransport;

import android.text.AndroidCharacter;
import android.util.Log;
import com.ferhatozcelik.terminal.tools.terminal.vt320;

public class Relay implements Runnable {
	private static final String TAG = "CB.Relay";

	private static final int BUFFER_SIZE = 4096;

	private TerminalBridge bridge;

	private Charset currentCharset;
	private CharsetDecoder decoder;

	private AbsTransport transport;

	private vt320 buffer;

	private ByteBuffer byteBuffer;
	private CharBuffer charBuffer;

	private byte[] byteArray;
	private char[] charArray;

	public Relay(TerminalBridge bridge, AbsTransport transport, vt320 buffer, String encoding) {
		setCharset(encoding);
		this.bridge = bridge;
		this.transport = transport;
		this.buffer = buffer;
	}

	public void setCharset(String encoding) {
		Charset charset;
		if (encoding.equals("CP437")) {
			charset = new IBM437("IBM437",
					new String[] {"IBM437", "CP437"});
		} else {
			charset = Charset.forName(encoding);
		}

		if (charset == null || charset.equals(currentCharset)) {
			return;
		}

		CharsetDecoder newCd = charset.newDecoder();
		newCd.onUnmappableCharacter(CodingErrorAction.REPLACE);
		newCd.onMalformedInput(CodingErrorAction.REPLACE);

		currentCharset = charset;
		synchronized (this) {
			decoder = newCd;
		}
	}

	public Charset getCharset() {
		return currentCharset;
	}

	@Override
	public void run() {
		byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		charBuffer = CharBuffer.allocate(BUFFER_SIZE);

		/* for East Asian character widths */
		byte[] wideAttribute = new byte[BUFFER_SIZE];

		byteArray = byteBuffer.array();
		charArray = charBuffer.array();

		CoderResult result;

		int bytesRead = 0;
		byteBuffer.limit(0);
		int bytesToRead;
		int offset;

		try {
			while (true) {
				bytesToRead = byteBuffer.capacity() - byteBuffer.limit();
				offset = byteBuffer.arrayOffset() + byteBuffer.limit();
				bytesRead = transport.read(byteArray, offset, bytesToRead);

				if (bytesRead > 0) {
					byteBuffer.limit(byteBuffer.limit() + bytesRead);

					synchronized (this) {
						result = decoder.decode(byteBuffer, charBuffer, false);
					}

					if (result.isUnderflow() &&
							byteBuffer.limit() == byteBuffer.capacity()) {
						byteBuffer.compact();
						byteBuffer.limit(byteBuffer.position());
						byteBuffer.position(0);
					}

					offset = charBuffer.position();

					AndroidCharacter.getEastAsianWidths(charArray, 0, offset, wideAttribute);
					buffer.putString(charArray, wideAttribute, 0, charBuffer.position());
					bridge.propagateConsoleText(charArray, charBuffer.position());
					charBuffer.clear();
					bridge.redraw();
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Problem while handling incoming data in relay thread", e);
		}
	}
}
