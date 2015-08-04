package com.dslplatform.client.json.StringWithMaxLengthOf9;

import com.dslplatform.client.JsonSerialization;
import com.dslplatform.patterns.Bytes;
import java.io.IOException;

public class OneStringWithMaxLengthOf9DefaultValueTurtle {
	private static JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = com.dslplatform.client.StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final String defaultValue = "";
		final Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final String defaultValueJsonDeserialized = jsonSerialization.deserialize(String.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		com.dslplatform.ocd.javaasserts.StringWithMaxLengthOf9Asserts.assertOneEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final String borderValue1 = "\"";
		final Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final String borderValue1JsonDeserialized = jsonSerialization.deserialize(String.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.StringWithMaxLengthOf9Asserts.assertOneEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final String borderValue2 = "'/\\[](){}";
		final Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final String borderValue2JsonDeserialized = jsonSerialization.deserialize(String.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.StringWithMaxLengthOf9Asserts.assertOneEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final String borderValue3 = "\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t";
		final Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final String borderValue3JsonDeserialized = jsonSerialization.deserialize(String.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.StringWithMaxLengthOf9Asserts.assertOneEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final String borderValue4 = "xxxxxxxxx";
		final Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final String borderValue4JsonDeserialized = jsonSerialization.deserialize(String.class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.StringWithMaxLengthOf9Asserts.assertOneEquals(borderValue4, borderValue4JsonDeserialized);
	}
}
