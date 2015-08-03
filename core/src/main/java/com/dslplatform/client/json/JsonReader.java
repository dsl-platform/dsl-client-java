package com.dslplatform.client.json;

import com.dslplatform.patterns.ServiceLocator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

public final class JsonReader {

	private static final boolean[] Whitespace = new boolean[256];

	static {
		Whitespace[9 + 128] = true;
		Whitespace[10 + 128] = true;
		Whitespace[11 + 128] = true;
		Whitespace[12 + 128] = true;
		Whitespace[13 + 128] = true;
		Whitespace[32 + 128] = true;
		Whitespace[-96 + 128] = true;
		Whitespace[-31 + 128] = true;
		Whitespace[-30 + 128] = true;
		Whitespace[-29 + 128] = true;
	}

	private final byte[] buffer;
	final int length;
	final ServiceLocator locator;
	private final char[] tmp;

	private int tokenStart;
	private int currentIndex = 0;
	private byte last = ' ';

	private JsonReader(final char[] tmp, final byte[] buffer, final int length, final ServiceLocator locator) {
		this.tmp = tmp;
		this.buffer = buffer;
		this.length = length;
		this.locator = locator;
	}

	public JsonReader(final byte[] buffer, final ServiceLocator locator) {
		this(new char[64], buffer, buffer.length, locator);
	}

	public JsonReader(final byte[] buffer, final ServiceLocator locator, final char[] tmp) {
		this(tmp, buffer, buffer.length, locator);
		if (tmp == null) {
			throw new NullPointerException("tmp buffer provided as null.");
		}
	}

	public JsonReader(final byte[] buffer, final int length, final ServiceLocator locator) throws IOException {
		this(buffer, length, locator, new char[64]);
	}

	public JsonReader(final byte[] buffer, final int length, final ServiceLocator locator, final char[] tmp) throws IOException {
		this(tmp, buffer, length, locator);
		if (tmp == null) {
			throw new NullPointerException("tmp buffer provided as null.");
		}
		if (length > buffer.length) {
			throw new IOException("length can't be longer than buffer.length");
		} else if (length < buffer.length) {
			buffer[length] = '\0';
		}
	}

	private final static Charset utf8 = Charset.forName("UTF-8");

	@Override
	public String toString() {
		return new String(buffer, 0, length, utf8);
	}

	public final byte read() throws IOException {
		if (currentIndex >= length) {
			throw new IOException("end of stream");
		}
		return last = buffer[currentIndex++];
	}

	boolean isEndOfStream() {
		return length == currentIndex;
	}

	public final byte last() {
		return last;
	}

	public final int getTokenStart() {
		return tokenStart;
	}

	public final int getCurrentIndex() {
		return currentIndex;
	}

	public final char[] readNumber() {
		tokenStart = currentIndex - 1;
		char ch = (char) last;
		tmp[0] = ch;
		int i = 1;
		int ci = currentIndex;
		while (i < tmp.length && ci < length) {
			ch = (char) buffer[ci++];
			if (ch == ',' || ch == '}' || ch == ']') break;
			tmp[i++] = ch;
		}
		currentIndex += i - 1;
		last = (byte) ch;
		return tmp;
	}

	public final String readSimpleString() throws IOException {
		if (last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		int i = 0;
		int ci = currentIndex;
		while (i < tmp.length && ci < length) {
			final char ch = (char) buffer[ci++];
			if (ch == '"') break;
			tmp[i++] = ch;
		}
		currentIndex = ci;
		return new String(tmp, 0, i);
	}

	public final char[] readSimpleQuote() throws IOException {
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		}
		int ci = tokenStart = currentIndex;
		for (int i = 0; i < tmp.length && ci < length; i++) {
			final char ch = (char) buffer[ci++];
			if (ch == '"') break;
			tmp[i] = ch;
		}
		currentIndex = ci;
		return tmp;
	}

	public final String readString() throws IOException {

		final int startIndex = currentIndex;
		// At this point, buffer cannot be empty or null, it is safe to read first character
		if (last != '"') {
			//TODO: count special chars in separate counter
			throw new IOException("JSON string must start with a double quote at: " + currentIndex);
		}

		byte bb = 0;
		int ci = currentIndex;
		for (int i = 0; i < tmp.length && ci < length; i++) {
			bb = buffer[ci++];
			if (bb == '"') {
				currentIndex = ci;
				return new String(tmp, 0, i);
			}
			// If we encounter a backslash, which is a beginning of an escape sequence
			// or a high bit was set - indicating an UTF-8 encoded multibyte character,
			// there is no chance that we can decode the string without instantiating
			// a temporary buffer, so quit this loop
			if ((bb ^ '\\') < 1) break;
			tmp[i] = (char) bb;
		}
		if (ci >= length) {
			throw new IOException("JSON string was not closed with a double quote at: " + ci);
		}

		// If the buffer contains an ASCII string (no high bit set) without any escape codes "\n", "\t", etc...,
		// there is no need to instantiate any temporary buffers, we just decode the original buffer directly
		// via ISO-8859-1 encoding since it is the fastest encoding which is guaranteed to retain all ASCII characters
		while (ci < buffer.length) {
			// If we encounter a backslash, which is a beginning of an escape sequence
			// or a high bit was set - indicating an UTF-8 encoded multibyte character,
			// there is no chance that we can decode the string without instantiating
			// a temporary buffer, so quit this loop
			if ((bb ^ '\\') < 1) break;
			bb = buffer[ci++];
			if (bb == '"') {
				currentIndex = ci;
				return new String(buffer, startIndex, currentIndex - startIndex - 1, "ISO-8859-1");
			}
		}
		currentIndex = ci;
		if (currentIndex >= length) {
			throw new IOException("JSON string was not closed with a double quote at: " + ci);
		}

		// temporary buffer, will resize if need be
		int soFar = --currentIndex - startIndex;
		char[] chars = new char[soFar + 256];

		// copy all the ASCII characters so far
		for (int i = soFar - 1; i >= 0; i--) {
			chars[i] = (char) buffer[startIndex + i];
		}

		while (currentIndex < length) {
			int bc = buffer[currentIndex++];
			if (bc == '"') {
				return new String(chars, 0, soFar);
			}

			// if we're running out of space, double the buffer capacity
			if (soFar >= chars.length - 3) {
				final char[] newChars = new char[chars.length << 1];
				System.arraycopy(chars, 0, newChars, 0, soFar);
				chars = newChars;
			}

			if (bc == '\\') {
				bc = buffer[currentIndex++];

				switch (bc) {
					case 'b':
						bc = '\b';
						break;
					case 't':
						bc = '\t';
						break;
					case 'n':
						bc = '\n';
						break;
					case 'f':
						bc = '\f';
						break;
					case 'r':
						bc = '\r';
						break;
					case '"':
					case '/':
					case '\\':
						break;
					case 'u':
						bc =
								(hexToInt(buffer[currentIndex++]) << 12) +
										(hexToInt(buffer[currentIndex++]) << 8) +
										(hexToInt(buffer[currentIndex++]) << 4) +
										hexToInt(buffer[currentIndex++]);
						break;

					default:
						throw new IOException("Could not parse String, got invalid escape combination '\\" + bc + "'");
				}
			} else if ((bc & 0x80) != 0) {
				final int u2 = buffer[currentIndex++];
				if ((bc & 0xE0) == 0xC0) {
					bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
				} else {
					final int u3 = buffer[currentIndex++];
					if ((bc & 0xF0) == 0xE0) {
						bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
					} else {
						final int u4 = buffer[currentIndex++];
						if ((bc & 0xF8) == 0xF0) {
							bc = ((bc & 0x07) << 18) + ((u2 & 0x3F) << 12) + ((u3 & 0x3F) << 6) + (u4 & 0x3F);
						} else {
							// there are legal 5 & 6 byte combinations, but none are _valid_
							throw new IOException();
						}

						if (bc >= 0x10000) {
							// check if valid unicode
							if (bc >= 0x110000) throw new IOException();

							// split surrogates
							final int sup = bc - 0x10000;
							chars[soFar++] = (char) ((sup >>> 10) + 0xd800);
							chars[soFar++] = (char) ((sup & 0x3ff) + 0xdc00);
						}
					}
				}
			}

			chars[soFar++] = (char) bc;
		}
		throw new IOException("JSON string was not closed with a double quote!");
	}

	private static int hexToInt(final byte value) throws IOException {
		if (value >= '0' && value <= '9') return value - 0x30;
		if (value >= 'A' && value <= 'F') return value - 0x37;
		if (value >= 'a' && value <= 'f') return value - 0x57;
		throw new IOException("Could not parse unicode escape, expected a hexadecimal digit, got '" + value + "'");
	}

	private boolean wasWhiteSpace() {
		switch (last) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 32:
			case -96:
				return true;
			case -31:
				if (currentIndex + 1 < length && buffer[currentIndex] == -102 && buffer[currentIndex + 1] == -128) {
					currentIndex += 2;
					last = ' ';
					return true;
				}
				return false;
			case -30:
				if (currentIndex + 1 < length) {
					final byte b1 = buffer[currentIndex];
					final byte b2 = buffer[currentIndex + 1];
					if (b1 == -127 && b2 == -97) {
						currentIndex += 2;
						last = ' ';
						return true;
					}
					if (b1 != -128) return false;
					switch (b2) {
						case -128:
						case -127:
						case -126:
						case -125:
						case -124:
						case -123:
						case -122:
						case -121:
						case -120:
						case -119:
						case -118:
						case -88:
						case -87:
						case -81:
							currentIndex += 2;
							last = ' ';
							return true;
						default:
							return false;
					}
				} else {
					return false;
				}
			case -29:
				if (currentIndex + 1 < length && buffer[currentIndex] == -128 && buffer[currentIndex + 1] == -128) {
					currentIndex += 2;
					last = ' ';
					return true;
				}
				return false;
			default:
				return false;
		}
	}

	public final byte getNextToken() throws IOException {
		read();
		if (Whitespace[last + 128]) {
			while (wasWhiteSpace()) {
				read();
			}
		}
		return last;
	}

	public final long positionInStream() {
		return currentIndex;
	}

	public final int fillName() throws IOException {
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		}
		tokenStart = currentIndex;
		int ci = currentIndex;
		long hash = 0x811c9dc5;
		while (ci < buffer.length) {
			final byte b = buffer[ci++];
			if (b == '"') break;
			hash ^= b;
			hash *= 0x1000193;
		}
		currentIndex = ci;
		if (read() != ':') {
			if (!wasWhiteSpace() || getNextToken() != ':') {
				throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) last);
			}
		}
		return (int) hash;
	}

	public final int calcHash() throws IOException {
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) last);
		}
		tokenStart = currentIndex;
		int ci = currentIndex;
		long hash = 0x811c9dc5;
		while (ci < buffer.length) {
			final byte b = buffer[ci++];
			if (b == '"') break;
			hash ^= b;
			hash *= 0x1000193;
		}
		currentIndex = ci;
		return (int) hash;
	}

	public final boolean wasLastName(final String name) {
		if (name.length() != currentIndex - tokenStart) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) != buffer[tokenStart + i]) {
				return false;
			}
		}
		return true;
	}

	public final String getLastName() throws IOException {
		return new String(buffer, tokenStart, currentIndex - tokenStart - 1, "ISO-8859-1");
	}

	private byte skipString() throws IOException {
		byte c = read();
		byte prev = c;
		while (c != '"' || prev == '\\') {
			prev = c;
			c = read();
		}
		return getNextToken();
	}

	public final byte skip() throws IOException {
		if (last == '"') return skipString();
		else if (last == '{') {
			byte nextToken = getNextToken();
			if (nextToken == '}') return getNextToken();
			if (nextToken == '"') nextToken = skipString();
			else
				throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
			if (nextToken != ':')
				throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
			getNextToken();
			nextToken = skip();
			while (nextToken == ',') {
				nextToken = getNextToken();
				if (nextToken == '"') nextToken = skipString();
				else
					throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
				if (nextToken != ':')
					throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
				getNextToken();
				nextToken = skip();
			}
			if (nextToken != '}')
				throw new IOException("Expecting '}' at position " + positionInStream() + ". Found " + (char) nextToken);
			return getNextToken();
		} else if (last == '[') {
			getNextToken();
			byte nextToken = skip();
			while (nextToken == ',') {
				getNextToken();
				nextToken = skip();
			}
			if (nextToken != ']')
				throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) nextToken);
			return getNextToken();
		} else {
			while (last != ',' && last != '}' && last != ']')
				read();
			return last;
		}
	}

	public final String readNext() throws IOException {
		final int start = currentIndex - 1;
		skip();
		return new String(buffer, start, currentIndex - start - 1, "UTF-8");
	}

	public final byte[] readBase64() throws IOException {
		if (last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + " at base64 start. Found " + (char) last);
		final int start = currentIndex;
		currentIndex = Base64.findEnd(buffer, start);
		last = buffer[currentIndex++];
		if (last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + " at base64 end. Found " + (char) last);
		}
		return Base64.decodeFast(buffer, start, currentIndex - 1);
	}

	interface ReadObject<T> {
		T read(JsonReader reader) throws IOException;
	}

	public interface ReadJsonObject<T extends JsonObject> {
		T deserialize(JsonReader reader, ServiceLocator locator) throws IOException;
	}

	public final boolean wasNull() throws IOException {
		if (last == 'n') {
			if (currentIndex + 2 < length && buffer[currentIndex] == 'u'
					&& buffer[currentIndex + 1] == 'l' && buffer[currentIndex + 2] == 'l') {
				currentIndex += 3;
				last = 'l';
				return true;
			}
			throw new IOException("Invalid null value found at: " + currentIndex);
		}
		return false;
	}

	public final boolean wasTrue() throws IOException {
		if (last == 't') {
			if (currentIndex + 2 < length && buffer[currentIndex] == 'r'
					&& buffer[currentIndex + 1] == 'u' && buffer[currentIndex + 2] == 'e') {
				currentIndex += 3;
				last = 'e';
				return true;
			}
			throw new IOException("Invalid boolean value found at: " + currentIndex);
		}
		return false;
	}

	public final boolean wasFalse() throws IOException {
		if (last == 'f') {
			if (currentIndex + 3 < length && buffer[currentIndex] == 'a'
					&& buffer[currentIndex + 1] == 'l' && buffer[currentIndex + 2] == 's'
					&& buffer[currentIndex + 3] == 'e') {
				currentIndex += 4;
				last = 'e';
				return true;
			}
			throw new IOException("Invalid boolean value found at: " + currentIndex);
		}
		return false;
	}

	public final void checkArrayEnd() throws IOException {
		if (last != ']') {
			if (currentIndex >= length) throw new IOException("Unexpected end of JSON in collection.");
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) last);
		}
	}

	public final <T> ArrayList<T> deserializeCollection(final ReadObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
		deserializeCollection(readObject, res);
		return res;
	}

	public final <T> void deserializeCollection(final ReadObject<T> readObject, final Collection<T> res) throws IOException {
		res.add(readObject.read(this));
		while (getNextToken() == ',') {
			getNextToken();
			res.add(readObject.read(this));
		}
		checkArrayEnd();
	}

	public final <T> ArrayList<T> deserializeNullableCollection(final ReadObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
		deserializeNullableCollection(readObject, res);
		return res;
	}

	public final <T> void deserializeNullableCollection(final ReadObject<T> readObject, final Collection<T> res) throws IOException {
		if (wasNull()) {
			res.add(null);
		} else {
			res.add(readObject.read(this));
		}
		while (getNextToken() == ',') {
			getNextToken();
			if (wasNull()) {
				res.add(null);
			} else {
				res.add(readObject.read(this));
			}
		}
		checkArrayEnd();
	}

	public final <T extends JsonObject> ArrayList<T> deserializeCollection(final ReadJsonObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
		deserializeCollection(readObject, res);
		return res;
	}

	public final <T extends JsonObject> void deserializeCollection(final ReadJsonObject<T> readObject, final Collection<T> res) throws IOException {
		if (last == '{') {
			getNextToken();
			res.add(readObject.deserialize(this, locator));
		} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		while (getNextToken() == ',') {
			if (getNextToken() == '{') {
				getNextToken();
				res.add(readObject.deserialize(this, locator));
			} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		}
		checkArrayEnd();
	}

	public final <T extends JsonObject> ArrayList<T> deserializeNullableCollection(final ReadJsonObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>(4);
		deserializeNullableCollection(readObject, res);
		return res;
	}

	public final <T extends JsonObject> void deserializeNullableCollection(final ReadJsonObject<T> readObject, final Collection<T> res) throws IOException {
		if (last == '{') {
			getNextToken();
			res.add(readObject.deserialize(this, locator));
		} else if (wasNull()) {
			res.add(null);
		} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		while (getNextToken() == ',') {
			if (getNextToken() == '{') {
				getNextToken();
				res.add(readObject.deserialize(this, locator));
			} else if (wasNull()) {
				res.add(null);
			} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) last);
		}
		checkArrayEnd();
	}
}
