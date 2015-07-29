package com.dslplatform.client.json.Point;

import com.dslplatform.client.JsonSerialization;
import com.dslplatform.patterns.Bytes;
import java.io.IOException;

public class OneSetOfOnePointsDefaultValueTurtle {
    private static JsonSerialization jsonSerialization;

    @org.junit.BeforeClass
    public static void initializeJsonSerialization() throws IOException {
        jsonSerialization = com.dslplatform.client.StaticJson.getSerialization();
    }

    @org.junit.Test
    public void testDefaultValueEquality() throws IOException {
        final java.util.Set<java.awt.Point> defaultValue = new java.util.HashSet<java.awt.Point>(0);
        final Bytes defaultValueJsonSerialized = jsonSerialization.serialize(defaultValue);
        final java.util.List<java.awt.Point> deserializedTmpList = jsonSerialization.deserializeList(java.awt.Point.class, defaultValueJsonSerialized.content, defaultValueJsonSerialized.length);
        final java.util.Set<java.awt.Point> defaultValueJsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.Point>(deserializedTmpList);
        com.dslplatform.ocd.javaasserts.PointAsserts.assertOneSetOfOneEquals(defaultValue, defaultValueJsonDeserialized);
    }

    @org.junit.Test
    public void testBorderValue1Equality() throws IOException {
        final java.util.Set<java.awt.Point> borderValue1 = new java.util.HashSet<java.awt.Point>(java.util.Arrays.asList(new java.awt.Point()));
        final Bytes borderValue1JsonSerialized = jsonSerialization.serialize(borderValue1);
        final java.util.List<java.awt.Point> deserializedTmpList = jsonSerialization.deserializeList(java.awt.Point.class, borderValue1JsonSerialized.content, borderValue1JsonSerialized.length);
        final java.util.Set<java.awt.Point> borderValue1JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.Point>(deserializedTmpList);
        com.dslplatform.ocd.javaasserts.PointAsserts.assertOneSetOfOneEquals(borderValue1, borderValue1JsonDeserialized);
    }

    @org.junit.Test
    public void testBorderValue2Equality() throws IOException {
        final java.util.Set<java.awt.Point> borderValue2 = new java.util.HashSet<java.awt.Point>(java.util.Arrays.asList(new java.awt.Point(0, 1000000000)));
        final Bytes borderValue2JsonSerialized = jsonSerialization.serialize(borderValue2);
        final java.util.List<java.awt.Point> deserializedTmpList = jsonSerialization.deserializeList(java.awt.Point.class, borderValue2JsonSerialized.content, borderValue2JsonSerialized.length);
        final java.util.Set<java.awt.Point> borderValue2JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.Point>(deserializedTmpList);
        com.dslplatform.ocd.javaasserts.PointAsserts.assertOneSetOfOneEquals(borderValue2, borderValue2JsonDeserialized);
    }

    @org.junit.Test
    public void testBorderValue3Equality() throws IOException {
        final java.util.Set<java.awt.Point> borderValue3 = new java.util.HashSet<java.awt.Point>(java.util.Arrays.asList(new java.awt.Point(), new java.awt.Point(Integer.MIN_VALUE, Integer.MIN_VALUE), new java.awt.Point(Integer.MAX_VALUE, Integer.MAX_VALUE), new java.awt.Point(0, -1000000000), new java.awt.Point(0, 1000000000)));
        final Bytes borderValue3JsonSerialized = jsonSerialization.serialize(borderValue3);
        final java.util.List<java.awt.Point> deserializedTmpList = jsonSerialization.deserializeList(java.awt.Point.class, borderValue3JsonSerialized.content, borderValue3JsonSerialized.length);
        final java.util.Set<java.awt.Point> borderValue3JsonDeserialized = deserializedTmpList == null ? null : new java.util.HashSet<java.awt.Point>(deserializedTmpList);
        com.dslplatform.ocd.javaasserts.PointAsserts.assertOneSetOfOneEquals(borderValue3, borderValue3JsonDeserialized);
    }
}
