package com.blame.locstotimelapse

import org.apache.logging.log4j.LogManager

import groovy.json.JsonSlurper

class PanoramaSeries {

	def private final logger
	def jsonSlurper
	def directionsUtil
	def streetViewUtil
	def propManager

	def PanoramaSeries() {
		jsonSlurper = new JsonSlurper()
		logger = LogManager.getLogger(PanoramaSeries.class)
		directionsUtil = new DirectionsUtil()
		streetViewUtil = new StreetViewUtil()
		propManager = new PropertyManager();
	}
	
	// TODO: make this one iterable, in a way that if there's no way to find the end_location
	// we should increment the factor in the def detectMovingAwayOnWrongWay() to be able to find a way
	def populateWithPanoramas(route) {
	  // find the first step with no panoramas
	  def istep = 0;
	  logger.info(route.steps.size())
	  for(; istep < route.steps.size(); istep++)
		if(route.steps[istep].panoramas == null)
		  break;
	
	  // all the steps have panoramas, so return
	  if(istep == route.steps.length)
		return route;
	  
	  def awayFactor = 0.5;
	  while(route.steps[istep].panoramas == null) {
		populateStepWithPanoramas(route, istep, awayFactor);
		awayFactor *= 2;
		if(awayFactor >= 50) {
		  route.steps[istep].panoramas = [];
		  break;
		}
		// TODO: need another stop condition to avoid infinite loop
	  }
	  return route;
	}
		
	def populateStepWithPanoramas(route, istep, awayFactor) {
	  logger.info("== STEP: " + istep + " ===============================================");
	  def step = route.steps[istep];
	  step.panoramas = [];
	  step.wrongWayPanoramas = [];
	  def panoramasAux = null;
	  logger.info(step);
	  logger.info("---------------------------------------------");
	
	  // get the fist panorama by a location, and store it
	  def panoramaInfo = directionsUtil.getPanoramaForLocation(step.start_location);
	  step.panoramas.push(panoramaInfo);
	  logger.info(panoramaInfo);
		
	  // get the last panoId of the previous step, to avoid revisit it
	  def previousStepPanoId = null;
	  if(istep > 0) {
		def previousStepPanoramas = route.steps[istep - 1].panoramas;
		previousStepPanoId = previousStepPanoramas[previousStepPanoramas.length - 1].panoId;
		// if the last panoId of the previous step is the same than the first of this
		// step, mark the penultimate of the previous step to avoid going through it
		if(previousStepPanoId == panoramaInfo.panoId)
		  previousStepPanoId = previousStepPanoramas[previousStepPanoramas.length - 2].panoId;
	  }
	  step.forbiddenPanoramas = [];
	  step.forbiddenPanoramas.push(previousStepPanoId);
		
	  // feed the forbidden panoramas with all the previous steps initial and end panoramas
	  for(def jstep = 0; jstep < istep; jstep++) {
		step.forbiddenPanoramas.push(route.steps[jstep].panoramas[0].panoId);
		step.forbiddenPanoramas.push(route.steps[jstep].panoramas[route.steps[jstep].panoramas.length - 1].panoId);
	  }
		
	  def minDistanceToStepEnd = MathUtil.getDistanceForLocations(panoramaInfo.location, step.end_location);
	  // loop to discover the rest of panoramas until it reaches the step.end_location
	  while(true) {
		// get the direction to take the next panorama
		def directionForNextPano = MathUtil.getDirectionForLocations(step.panoramas[step.panoramas.length -1].location, step.end_location);
		
		if(step.panoramas.length >= 2) {
		  previousStepPanoId = step.panoramas[step.panoramas.length -2].panoId
		}
		
		// get the next panorama by a direction
		panoramaInfo =
		  streetViewUtil.getNextPanorama(
			step.panoramas[step.panoramas.length -1].panoId,
			directionForNextPano,
			previousStepPanoId,
			step.forbiddenPanoramas
		  );
		step.panoramas.push(panoramaInfo);
		logger.info(panoramaInfo);
	
		// check if it got lost
		if(checkWrongWay(step, minDistanceToStepEnd, awayFactor)) {
		  // all options consumed detection
		  if(step.panoramas.length == 0) {
			logger.info("All options consumed.");
			// restore the panoramas if possible (if not it will be set to null)
			step.panoramas = panoramasAux;
			break;
		  }
		  minDistanceToStepEnd = MathUtil.getDistanceForLocations(step.panoramas[step.panoramas.length -1].location, step.end_location);
		  continue;
		}
	
		def distanceToStepEnd = MathUtil.getDistanceForLocations(panoramaInfo.location, step.end_location);
		logger.info(distanceToStepEnd);
		// condition to close the step panoramas: the iterations condition is to avoid infinite loop
		// when the look for panoramas get lost
		if(distanceToStepEnd < propManager.getScriptPropertyAsInt("END_STEP_CONDITION_DISTANCE") ||
			step.panoramas.length >= propManager.getScriptPropertyAsInt("END_STEP_CONDITION_MAX_PANORAMAS")) {
		  logger.info("End condition reached. Checking detours...");
		  // backup the panoramas
		  panoramasAux = step.panoramas.slice(); //TODO: replace this method by a valid one
		  if(detectDetour(step)) {
			// all options consumed detection
			if(step.panoramas.length == 0) {
			  logger.info("All options consumed.");
			  // restore the panoramas if possible (if not it will be set to null)
			  step.panoramas = panoramasAux;
			  break;
			}
			minDistanceToStepEnd = MathUtil.getDistanceForLocations(step.panoramas[step.panoramas.length -1].location, step.end_location);
			continue;
		  }
		  else {
			break;
		  }
		}
	
		if(distanceToStepEnd < minDistanceToStepEnd) {
		  minDistanceToStepEnd = distanceToStepEnd;
		}
	  }
	  
	  return route;
	}
	 
	/**
	 * Move all the panoramas to a plain array of panoramas, not moving
	 * duplicates. It also normalizes the direction of each panorama, by
	 * making it dependent on the next panoramas position
	 */
	def extractPanoramasFromRoute(route) {
	  // move the panoramas of each step to a plain array of panoramas
	  def panoramas = [];
	  for(def istep = 0; istep < 4/*route.steps.length*/; istep++) {
		def step = route.steps[istep];
		for(def ipano = 0; ipano < step.panoramas.length; ipano++) {
		  def panorama = step.panoramas[ipano];
		  // check if it is already present
		  def existsPanorama = false;
		  for(def jpano = 0; jpano < panoramas.length; jpano++) {
			if(panoramas[jpano].panoId == panorama.panoId) {
			  existsPanorama = true;
			}
		  }
		  if(!existsPanorama) {
			panoramas.push(panorama)
		  }
		}
	  }
	  
	  // override the panorama direction by setting it to the direction between that panorama an the one 3 places after
	  for(def ipano = 0; ipano < panoramas.length -3; ipano++) {
		panoramas[ipano].direction = MathUtil.getDirectionForLocations(panoramas[ipano].location, panoramas[ipano + 3].location);
	  }
	  panoramas[panoramas.length - 3].direction = MathUtil.getDirectionForLocations(panoramas[panoramas.length - 3].location, panoramas[panoramas.length - 1].location);
	  panoramas[panoramas.length - 2].direction = MathUtil.getDirectionForLocations(panoramas[panoramas.length - 2].location, panoramas[panoramas.length - 1].location);
	  panoramas[panoramas.length - 1].direction = panoramas[panoramas.length - 2].direction;
	  
	  return panoramas;
	}
	
	/*
	 * def to detect it is going through a wrong way, because of:
	 * - taking a wrong direction in a cross, and going away
	 * - entering in a loop
	 * - reaching a dead road
	 * - detecting a detour
	 */
	def checkWrongWay(step, minDistanceToStepEnd, awayFactor) {
	  if(detectDeadRoad(step))
		return true;
	  else if(detectLoop(step))
		return true;
	  else if(detectMovingAwayOnWrongWay(step, minDistanceToStepEnd, awayFactor))
		return true;
	  //else if(detectDetour(step))
	  //  return true;
	}
	
	def detectDetour(step, completeDetection) {
	  def lastCrossIndex = null;
	  for(def hpano = step.panoramas.length -1; hpano > 0; hpano-- ) {
		// check if the last panorama came from a cross and get that cross
		if(step.panoramas[hpano].comesFromCross) {
		  lastCrossIndex = hpano -1;
		}
		// go back to iterate over the previous crosses
		def crossesChecked = 0;
		for(def ipano = lastCrossIndex; ipano > 0; ipano--) {
		  if(step.panoramas[ipano].comesFromCross) {
			def crossIndex = ipano -1;
			
			// from this point the variables lastCross and cross are the panoramas that we need to analyze
			
			// get the distance traveled through the panoramas between the two crosses
			def distanceStraight = MathUtil.getDistanceForLocations(step.panoramas[crossIndex].location, step.panoramas[lastCrossIndex].location);
			def distanceTravelled = 0;
			for(def jpano = crossIndex + 1; jpano <= lastCrossIndex; jpano++) {
			  distanceTravelled += step.panoramas[jpano].distance;
			}
			if(distanceTravelled > distanceStraight * 1.5) {
			  logger.info("Detour detection. Going back to the initial cross of the detour...");
			  def wrongWayIndex = step.wrongWayPanoramas.length;
			  step.wrongWayPanoramas[wrongWayIndex] = [];
			  def panoramaRemoved = step.panoramas.pop();
			  if(step.panoramas.length > crossIndex && step.panoramas.length < lastCrossIndex) {
				step.wrongWayPanoramas[wrongWayIndex].push(panoramaRemoved);
			  }
			  while(true) {
				panoramaRemoved = step.panoramas.pop();
				if(step.panoramas.length > crossIndex && step.panoramas.length < lastCrossIndex) {
				  step.wrongWayPanoramas[wrongWayIndex].push(panoramaRemoved)
				}
				if(step.panoramas.length - 1 == crossIndex) {
				  step.forbiddenPanoramas.push(panoramaRemoved.panoId);
				  logger.info(panoramaRemoved.panoId + " marked as forbidden.");
				  logger.info("----------------------------------------------");
				  return true;
				}
			  }
			}
		  }
		}
	  }
	  
	  return false;
	}
	
	def detectDeadRoad(step) {
	  if(step.panoramas[step.panoramas.length - 1] == null) {
		// we have a dead road: remove panoramas until the previous cross and mark the panorama taken from there as forbidden
		logger.info("Dead road detection. Going back to the previous cross...");
		def wrongWayIndex = step.wrongWayPanoramas.length;
		step.wrongWayPanoramas[wrongWayIndex] = [];
		step.panoramas.pop();
		while(step.panoramas.length > 0) {
		  def panoramaRemoved = step.panoramas.pop();
		  step.wrongWayPanoramas[wrongWayIndex].push(panoramaRemoved);
		  if(panoramaRemoved.comesFromCross) {
			step.forbiddenPanoramas.push(panoramaRemoved.panoId);
			logger.info(panoramaRemoved.panoId + " marked as forbidden.");
			logger.info("----------------------------------------------");
			break;
		  }
		}
		return true;
	  }
	  return false;
	}
	
	def detectMovingAwayOnWrongWay(step, minDistanceToStepEnd, awayFactor) {
	  // it took a wrong direction in a previous cross when the distance
	  // to the step end location has grown a 20% more than the initial distance
	  def distanceToStepEnd = MathUtil.getDistanceForLocations(step.panoramas[step.panoramas.length - 1].location, step.end_location);
	  if(distanceToStepEnd > minDistanceToStepEnd * (1 + awayFactor)) {
		// remove panoramas until the previous cross and mark the panorama taken from there as forbidden
		logger.info("Wrong way detection. Going back to the previous cross...");
		def wrongWayIndex = step.wrongWayPanoramas.length;
		step.wrongWayPanoramas[wrongWayIndex] = [];
		while(step.panoramas.length > 0) {
		  def panoramaRemoved = step.panoramas.pop();
		  step.wrongWayPanoramas[wrongWayIndex].push(panoramaRemoved);
		  if(panoramaRemoved.comesFromCross) {
			step.forbiddenPanoramas.push(panoramaRemoved.panoId);
			logger.info(panoramaRemoved.panoId + " marked as forbidden.");
			logger.info("----------------------------------------------");
			break;
		  }
		}
		return true;
	  }
	  return false;
	}
	
	def detectLoop(step) {
	  for(def i = 0; i < step.panoramas.length -1; i++) {
		if(step.panoramas[i].panoId == step.panoramas[step.panoramas.length -1].panoId) {
		  // we have a loop: we will remove the last panorama and mark it as forbidden
		  logger.info("Loop detection. Cutting the way to avoid completing the loop...");
		  def panoramaRemoved = step.panoramas.pop();
		  def wrongWayIndex = step.wrongWayPanoramas.length;
		  step.wrongWayPanoramas[wrongWayIndex] = [];
		  step.wrongWayPanoramas[wrongWayIndex].push(panoramaRemoved);
		  step.forbiddenPanoramas.push(panoramaRemoved.panoId);
		  logger.info(panoramaRemoved.panoId + " marked as forbidden.");
		  logger.info("----------------------------------------------");
		  return true;
		}
	  }
	  return false;
	}
	
}
