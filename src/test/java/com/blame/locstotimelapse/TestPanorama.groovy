package com.blame.locstotimelapse

class TestPanorama {

	static testDirections() {
		def du = new DirectionsUtil();
		du.getRouteForLocations(["lat":"40.452167","lng":"-3.681109"], ["lat":"40.443993","lng":"-3.685777"]);
	}
	
	static void main(String[] args) {
		TestPanorama.testDirections();
	}
}
