package com.blame.locstotimelapse

import org.apache.logging.log4j.LogManager

class TestPanorama {

	static testDirections() {
		def du = new DirectionsUtil();
		du.getRouteForLocations(["lat":"40.452167","lng":"-3.681109"], ["lat":"40.443993","lng":"-3.685777"]);
	}

	static testStreetView() {
		def svu = new StreetViewUtil();
		def test = svu.getMetadataForPanoramaId("16lQjQaqI8Wge8QyZf009A");

		//logger.info(getPanoramaIdForLocation("40.462004","-3.646267"));
		//def test = getImageForPanoramaBase64("FYQlEOpn_k_3lSgPZqCE8Q", "235");
	}

	static void main(String[] args) {
		//TestPanorama.testDirections();
		TestPanorama.testStreetView();
	}

	static testMath() {
		def origin = ["lat":"40.462004","lng":"-3.646267"];
		def destination = ["lat":"40.393757","lng":"-3.675925"];

		def logger = LogManager.getLogger(TestPanorama.class)

		logger.info(getDirectionForLocations(origin, destination));
		logger.info(getDirectionForLocations(destination, origin));
		logger.info(getDistanceForLocations(origin, destination));
		logger.info(getDistanceForLocations(destination, origin));

		/*
		 logger.info(angleDifference(10, 60));
		 logger.info(angleDifference(10, 150));
		 logger.info(angleDifference(10, 240));
		 logger.info(angleDifference(10, 330));
		 logger.info("------------");
		 logger.info(angleDifference(110, 150));
		 logger.info(angleDifference(110, 240));
		 logger.info(angleDifference(110, 330));
		 logger.info("------------");
		 logger.info(angleDifference(210, 240));
		 logger.info(angleDifference(210, 330));
		 logger.info("------------");
		 logger.info(angleDifference(310, 330));
		 */
		/*
		 logger.info(angleDifference(-10, 60));
		 logger.info(angleDifference(-10, 150));
		 logger.info(angleDifference(-10, 240));
		 logger.info(angleDifference(-10, 330));
		 logger.info("------------");
		 logger.info(angleDifference(-110, 150));
		 logger.info(angleDifference(-110, 240));
		 logger.info(angleDifference(-110, 330));
		 logger.info("------------");
		 logger.info(angleDifference(-210, 240));
		 logger.info(angleDifference(-210, 330));
		 logger.info("------------");
		 logger.info(angleDifference(-310, 330));
		 */
	}

	def testPanorama() {
		// casa, trabajo
		//buildPanoramaSeries({"lat":"40.462004","lng":"-3.646267"}, {"lat":"40.393757","lng":"-3.675925"}, true);
		// cruce arturo soria plaza, trabajo
		//buildPanoramaSeries({"lat":"40.450361","lng":"-3.648706"}, {"lat":"40.393757","lng":"-3.675925"});

		//40.462004, -3.646267
		//40.393757, -3.675925
		def du = new DirectionsUtil();
		def ps = new PanoramaSeries();
		def logger = LogManager.getLogger(TestPanorama.class)

		def route = du.getRouteForLocations(["lat":"40.462004","lng":"-3.646267"], ["lat":"40.393757","lng":"-3.675925"]);
		route = ps.populateWithPanoramas(route);

		//40.452167, -3.681109
		//40.443993, -3.685777
		//var route = getRouteForLocations({"lat":"40.452167","lng":"-3.681109"}, {"lat":"40.443993","lng":"-3.685777"});
		//route = populateWithPanoramas(route);
		//route = populateWithPanoramas(route);

		logger.info("fin");
	}
}
