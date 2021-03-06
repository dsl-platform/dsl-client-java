package com.dslplatform.client.json.Binary;

import com.dslplatform.client.JsonSerialization;
import com.dslplatform.patterns.Bytes;
import java.io.IOException;

public class OneArrayOfOneBinariesDefaultValueTurtle {
	private static JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = com.dslplatform.client.StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final byte[][] defaultValue = new byte[0][];
		final Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final byte[][] defaultValueJsonDeserialized = jsonSerialization.deserialize(byte[][].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		com.dslplatform.ocd.javaasserts.BinaryAsserts.assertOneArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final byte[][] borderValue1 = new byte[][] { new byte[0] };
		final Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final byte[][] borderValue1JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.BinaryAsserts.assertOneArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final byte[][] borderValue2 = new byte[][] { new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE } };
		final Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final byte[][] borderValue2JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.BinaryAsserts.assertOneArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final byte[][] borderValue3 = new byte[][] { new byte[0], new byte[] { Byte.MIN_VALUE }, new byte[] { Byte.MIN_VALUE, 0 }, new byte[] { Byte.MIN_VALUE, 0, Byte.MAX_VALUE } };
		final Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final byte[][] borderValue3JsonDeserialized = jsonSerialization.deserialize(byte[][].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.BinaryAsserts.assertOneArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
	}
}
