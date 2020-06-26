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
    // Orders events and removes smaller overlapping events
    events = orderEventsbyStartTimes(events);
    
    return everyAttendeeIsConsidered(events, request);
  }

  /** Orders Events by start time */
  public Collection<Event> orderEventsbyStartTimes (Collection<Event> events) {
    // TreeMap already sorts by keys. Key-Value: startTime->Events startTimesToEvents
    if(events.size() == 1) return events;
    TreeMap<Integer, Event> tm = new TreeMap<>();
    for(Event e : events) {
      tm.put(e.getWhen().start(),e);
    }
    // Test cases: nestedEvents()
    // Insert ordered Events to new collection and ignore smaller overlapping events.
    Collection<Event> orderedEvents = new ArrayList<>();
    Event e0 = null;
    Event e1 = null;
    int smallerStart = 0;
    int biggerEnd = 0;
    TimeRange combinedEvents = null;
    // Used to keep track of two events
    int i = 0;
    for(Event e: tm.values()) {
        if(i == 0) {
          e1 = e;
          orderedEvents.add(e);
        }
        else {
          e0 = e1;
          e1 = e;
          if(e1.getWhen().overlaps(e0.getWhen())) {
              // Checking which starts first and which ends last
            orderedEvents.remove(e0);
        	if(e1.getWhen().start() <= e0.getWhen().start()) {
              smallerStart = e1.getWhen().start();
        	}
        	else if (e0.getWhen().start() < e1.getWhen().start()) {
              smallerStart = e0.getWhen().start();
        	}
        	if(e1.getWhen().end() >= e0.getWhen().end()) {
        	  biggerEnd = e1.getWhen().end();
         	}
         	else if (e0.getWhen().end() > e1.getWhen().end()) {
         	  biggerEnd = e0.getWhen().end();
         	}
            combinedEvents = TimeRange.fromStartEnd(smallerStart, biggerEnd, false);
            orderedEvents.add( new Event("Custom Event",combinedEvents, e0.getAttendees()) );
          } 
          // No events overlaps
          else {
            orderedEvents.add(e);
          }
        }

        i++;
    }
    return orderedEvents;
  }


 /** Returns available times as an arrayList of TimeRanges */
  
  public List<TimeRange> everyAttendeeIsConsidered(Collection<Event> events, MeetingRequest request) {
    // Test case:  noOptionsForTooLongOfARequest()
    if(request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    // Test cases: optionsForNoAttendees(), ignoresPeopleNotAttending(), noConflicts()
    if(request.getAttendees().isEmpty() || events.size() == 0
         || peopleNotAttending(events,request)) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    // List of times to return
	List<TimeRange> availableTimes = new ArrayList<>();
	Iterator<Event> eventIterator = events.iterator();
	TimeRange currEventTime = (TimeRange) eventIterator.next().getWhen();
	TimeRange runnerEventTime = null;
	TimeRange freeTime = null;
    int size = events.size();

    // Test cases: everyAttendeeIsConsidered(), justEnoughRoom(), notEnoughRoom(), doubleBookedPeople()
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
	  if(size != 1) currEventTime = (TimeRange) eventIterator.next().getWhen();
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
  

  private boolean peopleNotAttending(Collection<Event> events, MeetingRequest request) {
	for(String p : request.getAttendees()) {
		for(Event e : events) {
		  if(e.getAttendees().contains(p)) return false;	
		}
	}

	return true;
  }

  // Test case: justEnoughRoom(), notEnoughRoom()
  private boolean doesRequestFitInFreeTime(int freeTime, long requestedTime) {
	  return freeTime >= requestedTime;
  }

}

