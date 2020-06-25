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
    // Query conditions 
    List<TimeRange> noAttendees = optionsForNoAttendees(request);
    List<TimeRange> eventTooLong = noOptionsForTooLongOfARequest(request);
    List<TimeRange> eventAvailability = eventSplitsRestriction(events);
    
    if(events.size()>1) events = orderStartTimes(events);
    List<TimeRange> attendeeAvailability = everyAttendeeIsConsidered(events, request);
    
    // Add conditions here
    List<List<TimeRange>> conditions = new ArrayList<>();
    conditions.add(noAttendees);
    conditions.add(eventTooLong);
    conditions.add(eventAvailability);
    conditions.add(attendeeAvailability);

    return verifyAll(conditions);
  }

  /** Returns the List that's not null */
  public List<TimeRange> verifyAll(List<List<TimeRange>> conditions) {
    for(List<TimeRange> list : conditions) {
      if(list != null) return list;
    }
    return null;
  }

  /** Orders Events by start time */
  public Collection<Event> orderStartTimes (Collection<Event> events) {
    // TreeMap already sorts by keys. Key-Value: startTime->Events startTimesToEvents
    TreeMap<Integer, Event> tm = new TreeMap<>();
    int eventStart = 0;
    int startOfDay = TimeRange.START_OF_DAY;
    for(Event e : events) {
      eventStart = e.getWhen().start();
      tm.put(eventStart,e);
    }
    // Insert ordered Events to new collection
    Collection<Event> orderedEvents = new ArrayList<>();
    for(Event e: tm.values()) {
        orderedEvents.add(e);
    }
    return orderedEvents;
  }

  /** Condition 1 */
  public List<TimeRange> optionsForNoAttendees(MeetingRequest request) {
    if(request.getAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    return null;
  }

 /** Condition 2 */
  public List<TimeRange> noOptionsForTooLongOfARequest(MeetingRequest request) {
    if(request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    return null;
  }

 /** Condition 3: Consider one Event? */
  public List<TimeRange> eventSplitsRestriction(Collection<Event> events) {
    if(events.size() == 1) {
      int firstEventStart = events.iterator().next().getWhen().start();
      int firstEventEnd = events.iterator().next().getWhen().end();
      return Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstEventStart, false),
            TimeRange.fromStartEnd(firstEventEnd, TimeRange.END_OF_DAY, true));
    }
    return null;
  }

 /** Condition 4: Assuming the events are in order 

    Case 1:
     Events  :       |--A--|     |--B--|
     Day     : |-----------------------------|
     Options : |--1--|     |--2--|     |--3--|
      
    Case 2:
     Events  :       |--A--||--B--|
     Day     : |------------------------|
     Options : |--1--|            |--2--|

    Case 3:
     Events  : |--A--||--B--|
     Day     : |------------------------|
     Options :              |-----1-----|

    Case 4:
     Events  :             |--A--||--B--|
     Day     : |------------------------|
     Options : |-----1-----|

    Case 5:
     Events  : |--A--|            |--B--|
     Day     : |------------------------|
     Options :       |------1-----|

    Case 6:
     Events  : |--A--|      |--B--|
     Day     : |------------------------|
     Options :       |--1---|     |--2--|

    Case 7:
     Events  :        |--A--|     |--B--|
     Day     : |------------------------|
     Options : |---1--|     |--2--|

    */
  public List<TimeRange> everyAttendeeIsConsidered(Collection<Event> events, MeetingRequest request) {
    if(events.size() <= 1 || checkOverlappingEvents(events)) return null;

    List<TimeRange> availableTimes = new ArrayList<>();
    Iterator<Event> eventIterator = events.iterator();

    int size = events.size();
    TimeRange currEventTime = (TimeRange) eventIterator.next().getWhen();
    TimeRange runnerEventTime = null;

    // Before first Event
    TimeRange freeTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, currEventTime.start(), false);
    availableTimes.add(freeTime); 
    
    runnerEventTime = currEventTime;
    currEventTime = (TimeRange) eventIterator.next().getWhen();
    size--; 
    
    while (size != 0) {
     // In between events
     if(size != events.size()) {
        freeTime = TimeRange.fromStartEnd(runnerEventTime.end(), currEventTime.start(), false);
        availableTimes.add(freeTime); 
      }

      runnerEventTime = currEventTime;
      if(size != 1) currEventTime = (TimeRange) eventIterator.next().getWhen();
      size--;
    }
    // After last Event
    freeTime = TimeRange.fromStartEnd(runnerEventTime.end(), TimeRange.END_OF_DAY, true);
    availableTimes.add(freeTime); 
    return availableTimes;
  }

  public boolean checkOverlappingEvents(Collection<Event> events) {
    Iterator<Event> eventIterator = events.iterator();
    Event currEvent = (Event) eventIterator.next();
    Event nextEvent = (Event) eventIterator.next();
    while (eventIterator.hasNext()) {
      if (currEvent.getWhen().overlaps(nextEvent.getWhen())) return true;
      currEvent = nextEvent;
      nextEvent = eventIterator.next();
    }
      return false;
  }

}

