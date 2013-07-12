package com.dslplatform.client;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.patterns.ServiceLocator;
import com.fasterxml.jackson.databind.JavaType;

public class HttpClient {
    private static final Logger logger; static {
        logger = LoggerFactory.getLogger(HttpClient.class);
    }

 // -----------------------------------------------------------------------------

    public static String encode(final String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        }
        catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

// -----------------------------------------------------------------------------

    private static final SchemeRegistry schemeRegistry;
    private static final DefaultHttpClient httpClient;
    static {
        try {
            final String storeType = KeyStore.getDefaultType();
            final KeyStore truststore = KeyStore.getInstance(storeType);
            truststore.load(
                HttpClient.class.getResourceAsStream("common-cas." + storeType),
                "common-cas".toCharArray());

            schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", 443, new AdditionalKeyStoresSSLSocketFactory(truststore) ));

            @SuppressWarnings("deprecation")
            final ClientConnectionManager cm =
                new org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager(schemeRegistry);

            httpClient = new DefaultHttpClient(cm);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

//-----------------------------------------------------------------------------

    private final ServiceLocator locator;
    private final JsonSerialization json;

    private final String domainPrefix;
    private final int domainPrefixLength;

    private final ExecutorService executorService;

    public HttpClient(
            final ProjectSettings project,
            final ServiceLocator locator,
            final JsonSerialization json,
            final ExecutorService executorService) throws IOException {
        this.locator = locator;
        this.json = json;
        this.executorService = executorService;

        this.remoteUrl = project.get("api-url");
        this.domainPrefix = project.get("package-name");
        this.domainPrefixLength = domainPrefix.length() + 1;

        logger.debug("Initialized with: \n    username [{}] \n    api: [{}] \n    pid: [{}]"
            , project.get("username")
            , project.get("api-url")
            , project.get("project-id")
            );

        final String token = project.get("username") +':'+ project.get("project-id");
        basicAuth = "Basic " + new String(encodeBase64(token.getBytes("UTF-8")));
    }

    private final String remoteUrl;
    private final String basicAuth;
    private static final String MIME_TYPE = "application/json";

    static class Response {
      public final int code;
      public final byte[] body;

      public Response(final int code, final byte[] body) {
        this.code = code;
        this.body = body;
      }

      public String bodyToString() {
        return (body == null) ? "" : new String(body, java.nio.charset.Charset.forName("UTF-8"));
      }
    }

    private Response transmit(
            final String service,
            final Map<String, String> headers,
            final String method,
            final byte[] payload) throws IOException {

        final String url = remoteUrl + service;
        logger.debug("{} to URL: [{}]", method, url);

        final HttpUriRequest req;
        if (method == "POST") {
            final HttpPost post = new HttpPost(url);
            if(payload != null) {
                post.setEntity(new ByteArrayEntity(payload));
                logger.debug("____ payload _____");
                logger.debug("{}", IOUtils.toString(post.getEntity().getContent()));
                logger.debug("¯¯¯¯ payload ¯¯¯¯¯");
            }
            req = post;
        } else if (method == "PUT"){
            final HttpPut put = new HttpPut(url);
            if(payload != null) {
                put.setEntity(new ByteArrayEntity(payload));
            }
            req = put;
        } else if (method == "DELETE") {
            req = new HttpDelete(url);
        } else {
            req = new HttpGet(url);
        }

        req.setHeader("Accept", MIME_TYPE);
        req.setHeader("Content-Type", MIME_TYPE);
        req.setHeader("Authorization", basicAuth);
        for(final Map.Entry<String, String> h: headers.entrySet()) {
          req.setHeader(h.getKey(), h.getValue());
        }

        logger.debug("____ headers ____");
        for(final Header h: req.getAllHeaders()) {
          logger.debug("{}:{}", h.getName(), h.getValue());
        }

        logger.debug("¯¯¯¯ headers ¯¯¯¯");
        final HttpResponse response = httpClient.execute(req);

        final int code = response.getStatusLine().getStatusCode();
        final byte[] body = EntityUtils.toByteArray(response.getEntity());

        return new Response(code, body);
    }

    public String getDslName(final Class<?> clazz) {
        final String domainObjectName = clazz.getName();
        if(domainObjectName.startsWith(domainPrefix))
            return domainObjectName.substring(domainPrefixLength);
        throw new RuntimeException(domainObjectName + " is not defined for package " + domainPrefix);
    }

    private static boolean contains(final int[] array, final int v) {
        for (final int e : array) if (e == v) return true;
        return false;
    }
  //-----------------------------------------------------------------------------

  private <TArgument> Response doRawRequest(
      final String service,
      final Map<String, String> headers,
      final String method,
      final TArgument content,
      final int[] expected,
      final long start)
      throws UnsupportedEncodingException, IOException {
    final byte[] body;

        if (content == null) {
            body = null;
            logger.debug("Sending request [{}]: {}, no content", method, service);
        }
        else {
            final String jsonBody = json.serialize(content);
            body = jsonBody.getBytes("UTF-8");
            logger.debug("Sending request [{}]: {}, content: {}", method, service, jsonBody);
            logger.trace("Sending request body: {} ", new String(body, "UTF-8"));
        }


        final Response response = transmit(service, headers, method, body);

        final long time = System.currentTimeMillis() - start;
        logger.debug("Received response [{}, {} bytes] in {} ms", response.code, response.body.length, time);
        logger.trace("Received response body: {}", response.bodyToString());

        if (expected != null && !contains(expected, response.code)) {
            throw new IOException("Unexpected return code: " + response.code + ", response: " + response.bodyToString());
        }
        else if(expected == null && response.code >= 300) {
            throw new IOException(response.bodyToString());
        }

        return response;
  }

  private static final Map<String, String> emptyHeaders = new java.util.HashMap<String, String>();

    public <TArgument> Future<byte[]> sendRawRequest(
            final String service,
            final String method,
            final TArgument content,
            final Map<String, String> headers,
            final int[] expected) {

        final long start = System.currentTimeMillis();

        return
            executorService.submit(new Callable<byte[]>() {
                @Override
                public byte[] call() throws IOException {
                    return doRawRequest(service, headers, method, content, expected, start).body;
                }
            });
    }

    public <TArgument, TResult> Future<TResult> sendRequest(
            final JavaType type,
            final String service,
            final String method,
            final TArgument content,
            final int[] expected) {

        final long start = System.currentTimeMillis();

        return
            executorService.submit(new Callable<TResult>() {
                @SuppressWarnings("unchecked")
                @Override
                public TResult call() throws IOException {
                    final Response response = doRawRequest(service, emptyHeaders, method, content, expected, start);

                    return type != null
                        ? (TResult)json.deserialize(type, response.bodyToString(), locator)
                        : null;
                }
            });
    }
}
