package com.blame.locstotimelapse

import org.apache.logging.log4j.LogManager

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class DirectionsUtil {
	def private final logger
	def jsonSlurper
	def propManager

	def DirectionsUtil() {
		jsonSlurper = new JsonSlurper()
		logger = LogManager.getLogger(DirectionsUtil.class)
		propManager = new PropertyManager()
	}
	
	def getRouteForLocations(origin, destination) {
		logger.info("Requesting route for origin " + origin + " and destination " + destination)
		def directionsAPIprefix = "https://maps.googleapis.com/maps/api/directions/json?"

		//def splittedOrigin = sOrigin.split(",");
		//def splittedDestination = sDestination.split(",");
		//def origin = {"lat" : splittedOrigin[0], "lng" : splittedOrigin[1]}
		//def destination = {"lat" : splittedDestination[0], "lng" : splittedDestination[1]}

		def directionsURL = directionsAPIprefix +
				"origin=" + origin.lat + "," + origin.lng + "&" +
				"destination=" + destination.lat + "," + destination.lng + "&" +
				"key="
		def apiKey = propManager.getPropertyAsString("google.maps.apikey")
		directionsURL = directionsURL + apiKey

		logger.info("Requesting route through URL: " + directionsURL)
		def responseText = new URL(directionsURL).getText()
		def directions = jsonSlurper.parseText(responseText)
		def route = directions.routes[0]

		// iterate over each leg
		def output = [:]
		output.origin = origin
		output.origin.place_id = directions.geocoded_waypoints[0].place_id
		output.destination = destination
		output.destination.place_id = directions.geocoded_waypoints[1].place_id
		output.steps = []

		logger.info("number of legs " + route.legs.size())
		for(def ileg = 0; ileg < route.legs.size(); ileg++) {
			def leg = route.legs[ileg]
			//logger.info(leg)
			
			logger.info("number of steps " + leg.steps.size())
			// iterate over each step
			for(def istep = 0; istep < leg.steps.size(); istep++) {
				def step = leg.steps[istep];
				//logger.info(step)
				
				// add it into a transformed more simple step object:
				// {distance:... , start_location:{lat: ... , lng: ...}, end_location:{lat: ... , lng: ...}, instructions}
				output.steps.add([
					"distance" : step.distance.value,
					"start_location" : step.start_location,
					"end_location" : step.end_location
				])
			}
		}

		logger.info("Returning route as : " + output)
		return output
	}

}
