package io.pivotal.springsamples.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.springsamples.CreateEvent;
import io.pivotal.springsamples.Event;
import io.pivotal.springsamples.FetchEvent;
import io.pivotal.springsamples.FetchUpcomingEvents;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EventsController {

    private final CreateEvent createEvent;
    private final FetchEvent fetchEvent;
    private final FetchUpcomingEvents fetchUpcomingEvents;

    @Autowired
    public EventsController(CreateEvent createEvent,
                            FetchEvent fetchEvent,
                            FetchUpcomingEvents fetchUpcomingEvents) {
        this.createEvent = createEvent;
        this.fetchEvent = fetchEvent;
        this.fetchUpcomingEvents = fetchUpcomingEvents;
    }

    @Getter
    private static class CreateEventRequest {
        private String title;
        private String date;
    }

    private static class ValidationErrorResponse {
        @JsonProperty
        private final List<ErrorMessage> errors;

        private static class ErrorMessage {
            @JsonProperty
            private final String code;

            @JsonProperty
            private final String description;

            private ErrorMessage(String code, String description) {
                this.code = code;
                this.description = description;
            }
        }

        private ValidationErrorResponse(List<ErrorMessage> errors) {
            this.errors = errors;
        }
    }

    @RequestMapping(value = "/api/events", method = RequestMethod.POST)
    public ResponseEntity createEvent(@RequestBody CreateEventRequest request, UriComponentsBuilder uriBuilder) {
        return createEvent.perform(
                request.getTitle(),
                LocalDate.parse(request.getDate()),
                LocalDateTime.now(),

                new CreateEvent.ResultHandler<ResponseEntity>() {
                    @Override
                    public ResponseEntity eventCreated(Event event) {
                        return ResponseEntity
                                .created(uriBuilder.path("/api/events/" + event.getId()).build().toUri())
                                .body(toJson(event));
                    }

                    @Override
                    public ResponseEntity eventNotCreatedDueToValidationErrors(List<CreateEvent.ValidationError> errors) {
                        return ResponseEntity
                                .unprocessableEntity()
                                .body(
                                        new ValidationErrorResponse(errors.stream()
                                                .map(error -> messageForError(error))
                                                .collect(Collectors.toList())
                                        )
                                );
                    }
                }
        );
    }

    private ValidationErrorResponse.ErrorMessage messageForError(CreateEvent.ValidationError error) {
        switch(error) {
            case TITLE_IS_REQUIRED:
                return new ValidationErrorResponse.ErrorMessage("missing_title", "Title is a required field and must not be blank");
            case DATE_MUST_NOT_BE_PAST:
                return new ValidationErrorResponse.ErrorMessage("date_is_past", "The date must be today or later");
        }

        throw new RuntimeException("No response representation for unknown validation error: " + error);
    }

    @RequestMapping(value = "/api/events/upcoming", method = RequestMethod.GET)
    public ResponseEntity getUpcomingEvents() {
        return fetchUpcomingEvents.perform(
                LocalDate.now(),
                events -> ResponseEntity.ok(
                        events.stream()
                                .map(EventsController::toJson)
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
                        return ResponseEntity.ok(toJson(event));
                    }

                    @Override
                    public ResponseEntity eventNotFound(String nonexistentEventId) {
                        return ResponseEntity.notFound().build();
                    }
                }
        );
    }

    private static class EventJson {
        @JsonProperty
        private String id;

        @JsonProperty
        private String title;

        @JsonProperty
        private String date;

        private EventJson(String id, String title, String date) {
            this.id = id;
            this.title = title;
            this.date = date;
        }
    }

    private static EventJson toJson(Event event) {
        return new EventJson(event.getId(), event.getTitle(), event.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
}
