package io.pivotal.springsamples.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.springsamples.CreateEvent;
import io.pivotal.springsamples.Event;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CreateEventController {
    private final CreateEvent createEvent;

    public CreateEventController(CreateEvent createEvent) {
        this.createEvent = createEvent;
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
                                .body(new EventJson(event));
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

    private ValidationErrorResponse.ErrorMessage messageForError(CreateEvent.ValidationError error) {
        switch(error) {
            case TITLE_IS_REQUIRED:
                return new ValidationErrorResponse.ErrorMessage("missing_title", "Title is a required field and must not be blank");
            case DATE_MUST_NOT_BE_PAST:
                return new ValidationErrorResponse.ErrorMessage("date_is_past", "The date must be today or later");
        }

        throw new RuntimeException("No response representation for unknown validation error: " + error);
    }
}
