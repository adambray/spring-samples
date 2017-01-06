package io.pivotal.springsamples.api;

import io.pivotal.springsamples.Event;
import io.pivotal.springsamples.FetchEvent;
import io.pivotal.springsamples.FetchUpcomingEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestController
public class GetEventsController {

    private final FetchEvent fetchEvent;
    private final FetchUpcomingEvents fetchUpcomingEvents;

    @Autowired
    public GetEventsController(FetchEvent fetchEvent,
                               FetchUpcomingEvents fetchUpcomingEvents) {
        this.fetchEvent = fetchEvent;
        this.fetchUpcomingEvents = fetchUpcomingEvents;
    }

    @RequestMapping(value = "/api/events/upcoming", method = RequestMethod.GET)
    public ResponseEntity getUpcomingEvents() {
        return fetchUpcomingEvents.perform(
                LocalDate.now(),
                events -> ResponseEntity.ok(
                        events.stream()
                                .map(EventJson::new)
                                .collect(Collectors.toList())
                )
        );
    }

    @RequestMapping(value = "/api/events/{id}", method = RequestMethod.GET)
    public ResponseEntity getEvent(@PathVariable("id") String eventId) {
        return fetchEvent.perform(
                eventId,
                new FetchEvent.ResultHandler<ResponseEntity>() {
                    @Override
                    public ResponseEntity foundEvent(Event event) {
                        return ResponseEntity.ok(new EventJson(event));
                    }

                    @Override
                    public ResponseEntity eventNotFound(String nonexistentEventId) {
                        return ResponseEntity.notFound().build();
                    }
                }
        );
    }
}
