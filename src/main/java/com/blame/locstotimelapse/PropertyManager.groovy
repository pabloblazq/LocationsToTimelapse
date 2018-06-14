package com.blame.locstotimelapse

import java.util.Properties

class PropertyManager {

	static final PROPERTIES_FILENAME = "locs_to_timelapse.properties"
	Properties properties = null
	
	def PropertyManager() {
		properties = new Properties()
		ClassLoader cl = getClass().getClassLoader()
		InputStream input = cl.getResourceAsStream(PROPERTIES_FILENAME)
		//InputStream input = PropertyManager.class.getClassLoader().getResourceAsStream(filename)
		properties.load(input)
	}
	
	String getPropertyAsString(key) {
		return properties.getProperty(key)
	}
	
	int getPropertyAsInt(key) {
		return Integer.parseInt(properties.getProperty(key))
	}
}
