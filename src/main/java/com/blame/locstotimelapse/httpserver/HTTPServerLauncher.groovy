package com.blame.locstotimelapse.httpserver

import java.util.concurrent.Executors

import com.blame.locstotimelapse.httpserver.handlers.ReverseHandler
import com.sun.net.httpserver.HttpServer

class HTTPServerLauncher {

	static mainOriginal(args) {
		//configuring a Java 6 HttpServer
		InetSocketAddress addr = new InetSocketAddress(8081)
		def httpServer = com.sun.net.httpserver.HttpServer.create(addr, 0)
		httpServer.with {
			createContext('/', new ReverseHandler())
			setExecutor(Executors.newCachedThreadPool())
			System.out.println("Starting server...")
			start()
			System.out.println("Server started!")
		}
	}
	
	static main(args) {
		Properties properties = new Properties()
		properties.load(HTTPServerLauncher.class.getClassLoader().getResourceAsStream("httpserver/actions"))

		InetSocketAddress addr = new InetSocketAddress(8081)
		def httpServer = HttpServer.create(addr, 0)

		properties.each { context, handlerClass ->
			httpServer.createContext("/"+ context.split("\\.")[1] + "/", Class.forName(handlerClass).newInstance())
		}
		
		httpServer.setExecutor(Executors.newCachedThreadPool())
		System.out.println("Starting server...")
		httpServer.start()
		System.out.println("Server started!")
	}

}
