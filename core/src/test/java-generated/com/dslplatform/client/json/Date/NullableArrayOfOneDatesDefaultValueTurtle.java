package com.dslplatform.client.json.Date;

import com.dslplatform.client.JsonSerialization;
import com.dslplatform.patterns.Bytes;
import java.io.IOException;

public class NullableArrayOfOneDatesDefaultValueTurtle {
    private static JsonSerialization jsonSerialization;

    @org.junit.BeforeClass
    public static void initializeJsonSerialization() throws IOException {
        jsonSerialization = com.dslplatform.client.StaticJson.getSerialization();
    }

    @org.junit.Test
    public void testDefaultValueEquality() throws IOException {
        final org.joda.time.LocalDate[] defaultValue = null;
        final Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
        final org.joda.time.LocalDate[] defaultValueJsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate[].class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
        com.dslplatform.ocd.javaasserts.DateAsserts.assertNullableArrayOfOneEquals(defaultValue, defaultValueJsonDeserialized);
    }

    @org.junit.Test
    public void testBorderValue1Equality() throws IOException {
        final org.joda.time.LocalDate[] borderValue1 = new org.joda.time.LocalDate[] { org.joda.time.LocalDate.now() };
        final Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
        final org.joda.time.LocalDate[] borderValue1JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate[].class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
        com.dslplatform.ocd.javaasserts.DateAsserts.assertNullableArrayOfOneEquals(borderValue1, borderValue1JsonDeserialized);
    }

    @org.junit.Test
    public void testBorderValue2Equality() throws IOException {
        final org.joda.time.LocalDate[] borderValue2 = new org.joda.time.LocalDate[] { new org.joda.time.LocalDate(9999, 12, 31) };
        final Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
        final org.joda.time.LocalDate[] borderValue2JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate[].class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
        com.dslplatform.ocd.javaasserts.DateAsserts.assertNullableArrayOfOneEquals(borderValue2, borderValue2JsonDeserialized);
    }

    @org.junit.Test
    public void testBorderValue3Equality() throws IOException {
        final org.joda.time.LocalDate[] borderValue3 = new org.joda.time.LocalDate[] { org.joda.time.LocalDate.now(), new org.joda.time.LocalDate(1, 2, 3), new org.joda.time.LocalDate(1, 1, 1), new org.joda.time.LocalDate(0), new org.joda.time.LocalDate(Integer.MAX_VALUE * 1001L), new org.joda.time.LocalDate(9999, 12, 31) };
        final Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
        final org.joda.time.LocalDate[] borderValue3JsonDeserialized = jsonSerialization.deserialize(org.joda.time.LocalDate[].class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
        com.dslplatform.ocd.javaasserts.DateAsserts.assertNullableArrayOfOneEquals(borderValue3, borderValue3JsonDeserialized);
    }
}
