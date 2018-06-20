package com.blame.locstotimelapse

import org.apache.logging.log4j.LogManager

import groovy.json.JsonSlurper

class StreetViewUtil {

	def private final logger
	def jsonSlurper
	def propManager
	def panoramaMetadataCache
	
	def StreetViewUtil() {
		jsonSlurper = new JsonSlurper()
		logger = LogManager.getLogger(StreetViewUtil.class)
		propManager = new PropertyManager()
		panoramaMetadataCache = [:]
	}

	def getNextPanorama(panoramaId, direction, previousPanoramaId, forbiddenPanoramas) {
		def panoramaMetadata = panoramaMetadataCache.remove(panoramaId)
		if(panoramaMetadata == null) {
			panoramaMetadata = getMetadataForPanoramaId(panoramaId);
		}
		
		def minAngleLinkIndex = null;
		def minAngleDiff = 180;
		def numberOfAllowedLinks = 0;
		for(def ilink = 0; ilink < panoramaMetadata.Links.size(); ilink++) {
		  def linkDirection = Float.parseFloat(panoramaMetadata.Links[ilink].yawDeg);
		  def angleDiff = MathUtil.angleDifference(direction, linkDirection); //TODO: correct this call
		  def linkPanoramaId = panoramaMetadata.Links[ilink].panoId;
		  if(linkPanoramaId != previousPanoramaId && forbiddenPanoramas.indexOf(linkPanoramaId) == -1) {
			numberOfAllowedLinks++;
			if(angleDiff < minAngleDiff) {
			  minAngleLinkIndex = ilink;
			  minAngleDiff = angleDiff;
			}
		  }
		}
		
		if(minAngleLinkIndex == null) {
		  return null;
		}
		
		def comesFromCross = false;
		if(numberOfAllowedLinks > 1) {
		  comesFromCross = true;
		}
		
		def nextPanoramaMetadata = getMetadataForPanoramaId(panoramaMetadata.Links[minAngleLinkIndex].panoId);
		panoramaMetadataCache[nextPanoramaMetadata.Location.panoId] = nextPanoramaMetadata
		
		return [
		  "panoId" : panoramaMetadata.Links[minAngleLinkIndex].panoId,
		  "direction" : panoramaMetadata.Links[minAngleLinkIndex].yawDeg,
		  "distance" : MathUtil.getDistanceForLocations(panoramaMetadata.Location, nextPanoramaMetadata.Location),
		  "location" : [
			"lat" : nextPanoramaMetadata.Location.lat,
			"lng" : nextPanoramaMetadata.Location.lng
		  ],
		  //"roadName" : panoramaMetadata.Location.description,
		  "comesFromCross" : comesFromCross
		];
	  }
	  
	  def getMetadataForPanoramaId(panoramaId) {
		def cbkAPIURL = "http://maps.google.com/cbk?output=json&panoid=" + panoramaId + "&cb_client=api&pm=0&ph=0&v=2";
		return jsonSlurper.parseText(new URL(cbkAPIURL).getText())
	  }
	  
	  def getPanoramaForLocation(location) {
		def streetViewAPIprefix = "https://maps.googleapis.com/maps/api/streetview/metadata?";
		
		def responseText = new URL(streetViewAPIprefix + "location=" + location.lat + "," + location.lng + "&key=" + propManager.getPropertyAsString("google.maps.apikey")).getText()
		def panorama = jsonSlurper.parseText(responseText)
		
		def metadata = getMetadataForPanoramaId(panorama.pano_id);
		return [
		  "panoId" : panorama.pano_id,
		  "location" : panorama.location,
		  "roadName" : metadata.Location.description
		];
	  }
	  
	  def getImageForPanorama(panoramaId, heading) {
		logger.info("Requesting image for panoramaId " + panoramaId + " and heading " + heading);
	  
		def streetViewAPIprefix = "https://maps.googleapis.com/maps/api/streetview?";
		def size="640x360";
		def fov="120";
		def pitch="0";
		def streetViewAPIURL = streetViewAPIprefix +
		  "pano=" + panoramaId + "&" +
		  "size=" + size + "&" +
		  "fov=" + fov + "&" +
		  "heading=" + heading + "&" +
		  "pitch=" + pitch + "&" +
		  "key=" + propManager.getPropertyAsString("google.maps.apikey");
		logger.info("Requesting image through URL: " + streetViewAPIURL);
		logger.info("Requesting image for panoramaId " + panoramaId + " and heading " + heading);
		
		// TODO: need to take the blob as in the code below
		def response = UrlFetchApp.fetch(streetViewAPIURL);
		logger.info("Image returned");
		return response.getBlob();
	  }
	  
	  def getImageForPanoramaBase64(panoramaId, heading) {
		def imageBlob = getImageForPanorama(panoramaId, heading);
		// TODO: need to use a base64 encoder
		return Utilities.base64Encode(imageBlob.getBytes());
	   }
	  
}
