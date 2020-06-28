// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.Iterator;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

	events = onlyRequestedEvents(events, request);
    events = orderEventsbyStartTimes(events);
    events = checkForOverlaps(events);

    // Verifying Events 
    // for(Event e : events){
    //     System.out.println(e.toString());
    // }

    return everyAttendeeIsConsidered(events, request);
  }
  
  /**
   * Only adds events of the requested attendees 
   */
  private Collection<Event> onlyRequestedEvents(Collection<Event> events, MeetingRequest request) {
    // Test cases: ignoresPeopleNotAttending()
	Collection<Event> eventsOfRequested = new ArrayList<>();
	for(String p : request.getAttendees()) {
	  for(Event e : events) {
		if(e.getAttendees().contains(p)) {
          eventsOfRequested.add(e);	
        }
	  }
	}
	return eventsOfRequested;
  }

  /**
   * Orders Events by start time 
   */
  public Collection<Event> orderEventsbyStartTimes (Collection<Event> events) {
    // TreeMap already sorts by keys. Key -> Value: startTime -> Events 
    if(events.size() == 1) {
      return events;
    } 
    TreeMap<Integer, Event> tm = new TreeMap<>();
    for(Event e : events) {
       tm.put(e.getWhen().start(),e);
    }
    return tm.values();
  }
  
  /**
   * Combines nested or overlapping events as a single event 
   */
  public Collection<Event> checkForOverlaps(Collection<Event> events) {
    // Test cases: nestedEvents(), overlappingEvents(), doubleBookedPeople()
	Collection<Event> orderedEvents = new ArrayList<>();

    // eo is the event before e1
	Event e0 = null;
	Event e1 = null;

	Event customEvent = null;
	int i = 0;

	for(Event e: events) {
	  if(i == 0) {
	    e1 = e;
	    orderedEvents.add(e);
      // Verifying if the past event is a combined event and if it overlaps with another event
      } else if(e0 == customEvent && customEvent != null 
			        && e1.getWhen().overlaps(e0.getWhen()) ) {
		e1 = e;
	    orderedEvents.remove(e0);
	    customEvent = combineEvents(orderedEvents, e0, e1);
	  } else {
	      if (e0 != customEvent || customEvent == null) {
            e0 = e1;
          } 
	      e1 = e;
	      if( e1.getWhen().overlaps(e0.getWhen()) ) {
	        orderedEvents.remove(e0);
	        customEvent = combineEvents(orderedEvents, e0, e1);
	        e0 = customEvent;
	      // No events overlaps
	      } else {
	        orderedEvents.add(e);
	      }
	  }
      i++;
    }
	return orderedEvents;
  }
  
public Event combineEvents(Collection<Event> orderedEvents, Event e0, Event e1) {
  // Used to store smaller start and latest end of the events
  int smallerStart = 0;
  int biggerEnd = 0;
  int compResult = 0;
    
  Event newEvent = null;
  TimeRange combinedTimes = null;

  compResult = TimeRange.ORDER_BY_START.compare(e0.getWhen(), e1.getWhen());
  switch(compResult) {
    // e0 > e1
    case 1:
      smallerStart = e1.getWhen().start(); 
      break;
    // e0 < e1	
    case -1:
      smallerStart = e0.getWhen().start();
      break;
    // e0 == e1
    default:
      smallerStart = e0.getWhen().start();
      break;
  }
    	
  compResult = TimeRange.ORDER_BY_END.reversed().compare(e0.getWhen(), e1.getWhen());
  switch(compResult) {
    // e0 > e1
    case 1:
      biggerEnd = e1.getWhen().end();
      break;
    // e0 < e1	
    case -1:
      biggerEnd = e0.getWhen().end();
      break;
    // e0 == e1
    default:
      biggerEnd = e0.getWhen().end();
      break;
    }
    
    combinedTimes = TimeRange.fromStartEnd(smallerStart, biggerEnd, false);
    newEvent = new Event("Custom Event", combinedTimes, e0.getAttendees());
    orderedEvents.add(newEvent);
    return newEvent;
  }
  
  /**
   * Returns available times as an arrayList of TimeRanges 
   */
  public List<TimeRange> everyAttendeeIsConsidered(Collection<Event> events, MeetingRequest request) {
    // Test case:  noOptionsForTooLongOfARequest()
    if(request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    // Test cases: optionsForNoAttendees(), noConflicts()
    if(request.getAttendees().isEmpty() || events.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    // List of times to return
	List<TimeRange> availableTimes = new ArrayList<>();
	Iterator<Event> eventIterator = events.iterator();
	TimeRange currEventTime = (TimeRange) eventIterator.next().getWhen();
	TimeRange runnerEventTime = null;
	TimeRange freeTime = null;
    int size = events.size();

    // Test cases: everyAttendeeIsConsidered(), justEnoughRoom(), notEnoughRoom()
	// Before first Event
	if(currEventTime.start() != TimeRange.START_OF_DAY) {
	  freeTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, currEventTime.start(), false);
	  if(doesRequestFitInFreeTime(freeTime.duration(), request.getDuration())) {
	    availableTimes.add(freeTime); 	
	  }
	}
	if(size != 1) {
	  runnerEventTime = currEventTime;
	  currEventTime = (TimeRange) eventIterator.next().getWhen();
	  size--; 
    } 
    // In between events
	while (size != 0) {
                                           // Checks for |--A--|--B--|
	  if( (size != events.size()) && (runnerEventTime.end() != currEventTime.start()) ) {
	    freeTime = TimeRange.fromStartEnd(runnerEventTime.end(), currEventTime.start(), false);
        
	    if(doesRequestFitInFreeTime(freeTime.duration(), request.getDuration())) {
	      availableTimes.add(freeTime); 	
	    }
	  }
	  runnerEventTime = currEventTime;
	  // Prevents currEventTime reaching null
	  if(size != 1) {
        currEventTime = (TimeRange) eventIterator.next().getWhen();
      } 
	  size--;
    }
	
	// After last Event
	if(runnerEventTime.end()-1 != TimeRange.END_OF_DAY) {
	  freeTime = TimeRange.fromStartEnd(runnerEventTime.end(), TimeRange.END_OF_DAY, true);
	  if(doesRequestFitInFreeTime(freeTime.duration(), request.getDuration())) {
	    availableTimes.add(freeTime); 	
	  }
	}
	return availableTimes;
  }

  /**
   * Checks if requested time fits in with free time
   * Test case: justEnoughRoom(), notEnoughRoom() 
   */
  private boolean doesRequestFitInFreeTime(int freeTime, long requestedTime) {
    return freeTime >= requestedTime;
  }

}

