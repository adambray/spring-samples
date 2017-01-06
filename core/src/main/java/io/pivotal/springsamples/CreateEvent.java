package io.pivotal.springsamples;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateEvent {
    private EventRepository eventRepository;

    public CreateEvent(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public <T> T perform(String title, LocalDate date, LocalDateTime currentTime, ResultHandler<T> resultHandler) {

        List<ValidationError> errors = validationErrors(title, date, currentTime.toLocalDate());
        if(!errors.isEmpty()) {
            return resultHandler.eventNotCreatedDueToValidationErrors(errors);
        }

        Event savedEvent = eventRepository.save(new Event(title, date));
        return resultHandler.eventCreated(savedEvent);
    }

    public enum ValidationError {
        TITLE_IS_REQUIRED,
        DATE_MUST_NOT_BE_PAST
    }

    public interface ResultHandler<T> {
        T eventCreated(Event event);
        T eventNotCreatedDueToValidationErrors(List<ValidationError> errors);
    }

    private List<ValidationError> validationErrors(String title, LocalDate date, LocalDate currentDate) {
        List<ValidationError> errors = new ArrayList<>();

        if(title == null || "".equals(title)) {
            errors.add(ValidationError.TITLE_IS_REQUIRED);
        }

        if(date.isBefore(currentDate)) {
            errors.add(ValidationError.DATE_MUST_NOT_BE_PAST);
        }

        return errors;
    }
}
