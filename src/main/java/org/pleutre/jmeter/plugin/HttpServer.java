package org.pleutre.jmeter.plugin;

import fi.iki.elonen.NanoHTTPD;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jayway.jsonpath.JsonPath;

public class HttpServer extends NanoHTTPD {

	private static final Logger LOG = LoggingManager.getLoggerForClass();

	private static final ConcurrentHashMap<String, CompletableFuture<String>> results = new ConcurrentHashMap<String, CompletableFuture<String>>();

	private static final Pattern PATTERN_NOTIF = Pattern
			.compile(".*FunctionalIdentifier>([+\\d]+)</.*StatusMessage>([\\w_]+)</.*");

	/**
	 * Start a HTTP Server on port 8080
	 */
	public HttpServer() {
		super(6666);

		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
			LOG.info("NanoHTTPD started");
		} catch (IOException e) {
			LOG.info("Can not start NanoHTTPD", e);
		}

	}

	/**
	 * Method called when HTTP server receive a request
	 *
	 * In my sample, the received request is XML
	 * 
	 * @param session
	 * @return a HTTP response
	 */
	@Override
	public Response serve(IHTTPSession session) {
		String body = "";

		try {
			// In my sample, the received request always contains a content-length.
			// See on internet for other way to close a HTTP response
			String length = session.getHeaders().get("content-length");
			if (length != null) {

				final HashMap<String, String> map = new HashMap<String, String>();
				session.parseBody(map);
				body = map.get("postData");
				LOG.info("request =" + body);

				try {
					String content = JsonPath.read(body, "$.content.content");
					// Notify the JMeter sample that response is received for this identifier
					LOG.info("content: " + content);
					CompletableFuture<String> waiter = results.get(content);
					waiter.complete(content);
				} catch (Exception e) {
					LOG.error("could not parse the response", e);
				}

			}
		} catch (IOException e) {
			LOG.info("Can not serve response = " + session + body, e);
		} catch (ResponseException e1) {
			LOG.info("Can not serve response = " + session + body, e1);
		}

		// constant response
		return newFixedLengthResponse(NanoHTTPD.Response.Status.ACCEPTED, "application/json", body);

	}

	public static ConcurrentHashMap<String, CompletableFuture<String>> getResults() {
		return results;
	}

}
