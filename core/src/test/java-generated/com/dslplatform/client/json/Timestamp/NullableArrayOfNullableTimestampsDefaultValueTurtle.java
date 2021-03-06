package com.dslplatform.client.json.Timestamp;

import com.dslplatform.client.JsonSerialization;
import com.dslplatform.patterns.Bytes;
import java.io.IOException;

public class NullableArrayOfNullableTimestampsDefaultValueTurtle {
	private static JsonSerialization jsonSerialization;

	@org.junit.BeforeClass
	public static void initializeJsonSerialization() throws IOException {
		jsonSerialization = com.dslplatform.client.StaticJson.getSerialization();
	}

	@org.junit.Test
	public void testDefaultValueEquality() throws IOException {
		final org.joda.time.DateTime[] defaultValue = null;
		final Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
		final org.joda.time.DateTime[] defaultValueJsonDeserialized = jsonSerialization.deserialize(org.joda.time.DateTime[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
		com.dslplatform.ocd.javaasserts.TimestampAsserts.assertNullableArrayOfNullableEquals(defaultValue, defaultValueJsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue1Equality() throws IOException {
		final org.joda.time.DateTime[] borderValue1 = new org.joda.time.DateTime[] { null };
		final Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
		final org.joda.time.DateTime[] borderValue1JsonDeserialized = jsonSerialization.deserialize(org.joda.time.DateTime[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.TimestampAsserts.assertNullableArrayOfNullableEquals(borderValue1, borderValue1JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue2Equality() throws IOException {
		final org.joda.time.DateTime[] borderValue2 = new org.joda.time.DateTime[] { org.joda.time.DateTime.now() };
		final Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
		final org.joda.time.DateTime[] borderValue2JsonDeserialized = jsonSerialization.deserialize(org.joda.time.DateTime[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.TimestampAsserts.assertNullableArrayOfNullableEquals(borderValue2, borderValue2JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue3Equality() throws IOException {
		final org.joda.time.DateTime[] borderValue3 = new org.joda.time.DateTime[] { new org.joda.time.DateTime(Integer.MAX_VALUE * 1001L) };
		final Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
		final org.joda.time.DateTime[] borderValue3JsonDeserialized = jsonSerialization.deserialize(org.joda.time.DateTime[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.TimestampAsserts.assertNullableArrayOfNullableEquals(borderValue3, borderValue3JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue4Equality() throws IOException {
		final org.joda.time.DateTime[] borderValue4 = new org.joda.time.DateTime[] { org.joda.time.DateTime.now(), new org.joda.time.DateTime(0), new org.joda.time.DateTime(1, 1, 1, 0, 0, org.joda.time.DateTimeZone.UTC), new org.joda.time.DateTime(Integer.MAX_VALUE * 1001L) };
		final Bytes borderValue4JsonSerialized = jsonSerialization.serialize(borderValue4);
		final org.joda.time.DateTime[] borderValue4JsonDeserialized = jsonSerialization.deserialize(org.joda.time.DateTime[].class, borderValue4JsonSerialized.content, borderValue4JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.TimestampAsserts.assertNullableArrayOfNullableEquals(borderValue4, borderValue4JsonDeserialized);
	}

	@org.junit.Test
	public void testBorderValue5Equality() throws IOException {
		final org.joda.time.DateTime[] borderValue5 = new org.joda.time.DateTime[] { null, org.joda.time.DateTime.now(), new org.joda.time.DateTime(0), new org.joda.time.DateTime(1, 1, 1, 0, 0, org.joda.time.DateTimeZone.UTC), new org.joda.time.DateTime(Integer.MAX_VALUE * 1001L) };
		final Bytes borderValue5JsonSerialized = jsonSerialization.serialize(borderValue5);
		final org.joda.time.DateTime[] borderValue5JsonDeserialized = jsonSerialization.deserialize(org.joda.time.DateTime[].class, borderValue5JsonSerialized.content, borderValue5JsonSerialized.length);
		com.dslplatform.ocd.javaasserts.TimestampAsserts.assertNullableArrayOfNullableEquals(borderValue5, borderValue5JsonDeserialized);
	}
}
