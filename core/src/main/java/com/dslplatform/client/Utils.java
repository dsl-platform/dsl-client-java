package com.dslplatform.client;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Utils {
	public static final DateTime MIN_DATE_TIME = DateTime.parse("0001-01-01T00:00:00Z");
	public static final LocalDate MIN_LOCAL_DATE = new LocalDate(1, 1, 1);
	public static final UUID MIN_UUID = new java.util.UUID(0L, 0L);
	public static final byte[] EMPTY_BINARY = new byte[0];
	public static final BigDecimal ZERO_0 = BigDecimal.ZERO.setScale(0);
	public static final BigDecimal ZERO_1 = BigDecimal.ZERO.setScale(1);
	public static final BigDecimal ZERO_2 = BigDecimal.ZERO.setScale(2);
	public static final BigDecimal ZERO_3 = BigDecimal.ZERO.setScale(3);
	public static final BigDecimal ZERO_4 = BigDecimal.ZERO.setScale(4);
	public static final InetAddress LOOPBACK;

	static {
		InetAddress localhost = null;
		try {
			if (InetAddress.getLocalHost() instanceof Inet4Address) {
				localhost = InetAddress.getByName("127.0.0.1");
			} else {
				localhost = InetAddress.getByName("::1");
			}
		} catch (UnknownHostException ignore) {
		}
		LOOPBACK = localhost;
	}

	static <T> ArrayList<T> toArrayList(final Iterable<T> iterable) {
		final ArrayList<T> copy = new ArrayList<T>();
		for (final T t : iterable) {
			copy.add(t);
		}
		return copy;
	}

	@SuppressWarnings("unchecked")
	static List<Map.Entry<String, String>> acceptAs(final String mimeType) {
		return Collections.singletonList((Map.Entry<String, String>) new AbstractMap.SimpleEntry<String, String>(
				"Accept",
				mimeType));
	}

	private static void appendOrder(
			final StringBuilder sb,
			final Iterable<Map.Entry<String, Boolean>> order,
			final boolean isFirst) {
		if (order != null && order.iterator().hasNext()) {
			sb.append(isFirst ? "?order=" : "&order=");
			for (final Map.Entry<String, Boolean> el : order) {
				// null Boolean object will default to true
				if (el.getValue() == Boolean.FALSE) {
					sb.append('-');
				}
				sb.append(el.getKey()).append(',');
			}
			sb.setLength(sb.length() - 1);
		}
	}

	public static String appendLimitOffsetOrder(
			final String url,
			final Integer limit,
			final Integer offset,
			final Iterable<Map.Entry<String, Boolean>> order,
			final Boolean isFirst) {
		final StringBuilder sB = new StringBuilder(url);
		final boolean limitnull = limit == null;
		final boolean offsetnull = offset == null;
		final boolean orderFirst = limitnull && offsetnull && isFirst;

		if (!limitnull) {
			sB.append(isFirst ? "?limit=" : "&limit=").append(limit);
		}
		if (!offsetnull) {
			sB.append(limitnull && isFirst ? "?offset=" : "&offset=").append(offset);
		}

		appendOrder(sB, order, orderFirst);
		return sB.toString();
	}

	static String buildOlapArguments(
			final Iterable<String> dimensions,
			final Iterable<String> facts,
			final Iterable<Map.Entry<String, Boolean>> order,
			final String specificationName) {

		final StringBuilder query = new StringBuilder();

		if (order != null && order.iterator().hasNext() && !contains(dimensions, order) && !contains(facts, order))
			throw new IllegalArgumentException("Order must be an element of dimensions or facts!");

		appendUrlParam(query, "dimensions", dimensions);
		appendUrlParam(query, "facts", facts);

		if (query.length() == 0) throw new IllegalArgumentException("At least one dimension or fact is required");

		appendUrlParam(query, "specification", specificationName);
		appendOrder(query, order, false);

		return query.toString();
	}

	static String buildOlapArguments(
			final Iterable<String> dimensions,
			final Iterable<String> facts,
			final Iterable<Map.Entry<String, Boolean>> order) {
		final StringBuilder query = new StringBuilder();

		if (order != null && order.iterator().hasNext() && !contains(dimensions, order) && !contains(facts, order))
			throw new IllegalArgumentException("Order must be an element of dimensions or facts!");

		appendUrlParam(query, "dimensions", dimensions);
		appendUrlParam(query, "facts", facts);

		if (query.length() == 0) throw new IllegalArgumentException("At least one dimension or fact is required");

		appendOrder(query, order, false);

		return query.toString();
	}

	private static Boolean contains(final Iterable<String> source, final Iterable<Map.Entry<String, Boolean>> orders) {
		if (source == null) return false;
		for (final Map.Entry<String, Boolean> ord : orders) {
			if (!contains(source, ord.getKey())) return false;
		}
		return true;
	}

	private static void appendUrlParam(final StringBuilder sb, final String param, final Iterable<String> args) {
		if (args == null) return;

		final Iterator<String> aI = args.iterator();
		if (!aI.hasNext()) return;

		sb.append(sb.length() == 0 ? "?" : "&").append(param).append('=').append(aI.next());
		while (aI.hasNext()) {
			sb.append(',').append(aI.next());
		}
	}

	private static void appendUrlParam(final StringBuilder sB, final String param, final String arg) {
		if (arg == null) return;
		sB.append(sB.length() == 0 ? "?" : "&").append(param).append('=').append(arg);
	}

	private static Boolean contains(final Iterable<String> source, final String orders) {
		for (final String src : source) {
			if (orders.equals(src)) return true;
		}
		return false;
	}

	static final JsonSerialization STATIC_JSON;

	static String serialize(final Object value) throws IOException {
		return STATIC_JSON.serialize(value).toUtf8();
	}

	public static final boolean IS_ANDROID;
	public static final boolean HAS_JACKSON;

	static {
		IS_ANDROID = System.getProperty("java.runtime.name").toLowerCase(Locale.ENGLISH).contains("android");
		boolean foundJackson = false;
		try {
			Class.forName("com.fasterxml.jackson.databind.ObjectMapper", false, Thread.currentThread().getContextClassLoader());
			foundJackson = true;
		} catch (Exception ignore) {
		}
		HAS_JACKSON = foundJackson;
		STATIC_JSON = new DslJsonSerialization(null);
	}

	static class AndroidEncoding {
		private static String toBase64(final byte[] body) {
			return android.util.Base64.encodeToString(body, android.util.Base64.NO_WRAP);
		}
	}

	static class JavaEncoding {
		private static String toBase64(final byte[] body) {
			return javax.xml.bind.DatatypeConverter.printBase64Binary(body);
		}
	}

	static String base64Encode(final byte[] body) {
		if (IS_ANDROID) {
			return AndroidEncoding.toBase64(body);
		} else {
			return JavaEncoding.toBase64(body);
		}
	}
}
