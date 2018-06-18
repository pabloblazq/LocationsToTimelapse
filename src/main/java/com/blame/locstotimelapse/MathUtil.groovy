package com.blame.locstotimelapse

class MathUtil {
	static angleDifference(angle1, angle2) {
		def a = Math.abs(angle1 - angle2);
		return Math.abs((a + 180) % 360 - 180);
	}

	static getDirectionForLocations(originLocation, destinationLocation) {
		def lat1 = toRadians(normalizeToFloat(originLocation.lat));
		def lng1 = toRadians(normalizeToFloat(originLocation.lng));
		def lat2 = toRadians(normalizeToFloat(destinationLocation.lat));
		def lng2 = toRadians(normalizeToFloat(destinationLocation.lng));

		def y = Math.sin(lng2-lng1) * Math.cos(lat2);
		def x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(lng2-lng1);
		return toDegrees(Math.atan2(y, x));
	}

	static getDistanceForLocations(originLocation, destinationLocation) {
		def R = 6371000; // metres
		def lat1 = toRadians(normalizeToFloat(originLocation.lat));
		def lat2 = toRadians(normalizeToFloat(destinationLocation.lat));
		def lng1 = toRadians(normalizeToFloat(originLocation.lng));
		def lng2 = toRadians(normalizeToFloat(destinationLocation.lng));
		def deltaLat = lat2-lat1;
		def deltaLng = lng2-lng1;
		def a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
				Math.cos(lat1) * Math.cos(lat2) *
				Math.sin(deltaLng/2) * Math.sin(deltaLng/2);
		def c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return R * c;
	}

	static toDegrees(radians) {
		return radians * 180 / Math.PI;
	}

	static toRadians(degrees) {
		return degrees * Math.PI / 180;
	}

	static normalizeToFloat(number) {
		if(number instanceof String)
			return Float.parseFloat(number);
		else if(number instanceof BigDecimal)
			return number.floatValue();
		else return number;		
	}
}
