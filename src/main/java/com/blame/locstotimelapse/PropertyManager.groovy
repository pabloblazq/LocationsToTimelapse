package com.blame.locstotimelapse

import java.util.Properties

class PropertyManager {

	static final PROPERTIES_FILENAME = "locs_to_timelapse.properties"
	static final SENSIBLE_PROPERTIES_FILENAME = "locs_to_timelapse.sensible.properties"
	
	Properties properties = null
	
	def PropertyManager() {
		properties = new Properties()
		properties.load(getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILENAME))
		properties.load(getClass().getClassLoader().getResourceAsStream(SENSIBLE_PROPERTIES_FILENAME))
	}
	
	String getPropertyAsString(key) {
		return properties.getProperty(key)
	}
	
	int getPropertyAsInt(key) {
		return Integer.parseInt(properties.getProperty(key))
	}
}
