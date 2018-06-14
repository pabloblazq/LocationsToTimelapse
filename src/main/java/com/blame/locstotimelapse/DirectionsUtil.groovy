package com.blame.locstotimelapse

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import groovy.json.JsonSlurper

class DirectionsUtil {
	def private final logger
	def jsonSlurper

	def DirectionsUtil() {
		jsonSlurper = new JsonSlurper()
		logger = LogManager.getLogger(DirectionsUtil.class)
	}
	
	def getRouteForLocations(origin, destination) {
		logger.info("Requesting route for origin " + origin + " and destination " + destination)
		def directionsAPIprefix = "https://maps.googleapis.com/maps/api/directions/json?"

		//def splittedOrigin = sOrigin.split(",");
		//def splittedDestination = sDestination.split(",");
		//def origin = {"lat" : splittedOrigin[0], "lng" : splittedOrigin[1]}
		//def destination = {"lat" : splittedDestination[0], "lng" : splittedDestination[1]}

		def propManager = new PropertyManager();
		def directionsURL = directionsAPIprefix +
				"origin=" + origin.lat + "," + origin.lng + "&" +
				"destination=" + destination.lat + "," + destination.lng + "&" +
				"key=";
		def apiKey = propManager.getPropertyAsString("google.maps.apikey")
		directionsURL = directionsURL + apiKey

		logger.info("Requesting route through URL: " + directionsURL)
		def responseText = new URL(directionsURL).getText()
		def directions = jsonSlurper.parseText(responseText)
		def route = directions.routes[0];

		// iterate over each leg
		logger.info(directions["geocoded_waypoints"][0]["place_id"])
		def output = [];
		output.add(["origin" : origin]);
		output["origin"].add(["place_id" : directions["geocoded_waypoints"][0]["place_id"]]) ;
		output.add(["destination" : destination]);
		output["destination"].add(["place_id" : directions["geocoded_waypoints"][1]["place_id"]]) ;
		output.add(["steps" : []]);

		//TODO: continue the transformation from here
		for(int ileg = 0; ileg < route["legs"].length; ileg++) {
			def leg = route.legs[ileg];

			// iterate over each step
			for(def istep = 0; istep < leg.steps.length; istep++) {
				def step = leg.steps[istep];

				// add it into a transformed more simple step object:
				// {distance:... , start_location:{lat: ... , lng: ...}, end_location:{lat: ... , lng: ...}, instructions}
				output.steps.add([
					"distance" : step.distance.value,
					"start_location" : step.start_location,
					"end_location" : step.end_location
				]);
			}
		}

		logger.info("Returning route as : " + output);
		return output;
	}

}
