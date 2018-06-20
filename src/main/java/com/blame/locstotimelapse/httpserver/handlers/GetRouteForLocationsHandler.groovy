package com.blame.locstotimelapse.httpserver.handlers

import java.io.IOException

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class GetRouteForLocationsHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange httpExchange) throws IOException
	{
		String requestMethod = httpExchange.getRequestMethod();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "application/json");
		OutputStream responseBody = httpExchange.getResponseBody();

		final String query = httpExchange.getRequestURI().getRawQuery();
		if (query == null || !query.contains("string")) {
			httpExchange.sendResponseHeaders(400, 0);
			responseBody.close();
			return;
		}

		final String[] param = query.split("=");
		assert param.length == 2 && param[0].equals("string");

		httpExchange.sendResponseHeaders(200, 0);
		responseBody.write(new StringBuffer(param[1]).reverse().toString().getBytes());
		responseBody.close();
	}
}
