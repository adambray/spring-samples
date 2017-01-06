package io.pivotal.springsamples;

import io.pivotal.springsamples.fakes.InMemoryEventRepository;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;

public class CreateEventTest {

    private EventRepository eventRepository = new InMemoryEventRepository();
    private CreateEvent createEvent = new CreateEvent(eventRepository);
    private LocalDateTime now = LocalDateTime.of(2016, 11, 5, 13, 36, 56);
    private LocalDate tomorrow = now.plusDays(1).toLocalDate();
    private LocalDate yesterday = now.minusDays(1).toLocalDate();

    @Test
    public void givenValidParameters_createsTheEvent() {
        Event createdEvent = createEvent.perform("Board Game Night", tomorrow, now, grabExpectedEvent);

        assertThat(createdEvent.getTitle(), equalTo("Board Game Night"));
        assertThat(createdEvent.getDate(), equalTo(tomorrow));

        assertThat(createdEvent.getId(), notNullValue());
        assertThat(eventRepository.find(createdEvent.getId()).isPresent(), equalTo(true));
    }

    @Test
    public void whenTitleIsBlank_reportsInvalid() {
        List<CreateEvent.ValidationError> errors = createEvent.perform(null, tomorrow, now, grabExpectedErrors);

        assertThat(errors, containsInAnyOrder(CreateEvent.ValidationError.TITLE_IS_REQUIRED));

        errors = createEvent.perform("", tomorrow, now, grabExpectedErrors);

        assertThat(errors, containsInAnyOrder(CreateEvent.ValidationError.TITLE_IS_REQUIRED));
    }

    @Test
    public void whenDateIsAlreadyPast_reportsInvalid() {
        List<CreateEvent.ValidationError> errors = createEvent.perform("Old Event", yesterday, now, grabExpectedErrors);

        assertThat(errors, containsInAnyOrder(CreateEvent.ValidationError.DATE_MUST_NOT_BE_PAST));

    }

    @Test
    public void whenMultipleValidationsFail_reportsAllFailures() {
        List<CreateEvent.ValidationError> errors = createEvent.perform(null, yesterday, now, grabExpectedErrors);

        assertThat(errors, containsInAnyOrder(
                CreateEvent.ValidationError.DATE_MUST_NOT_BE_PAST,
                CreateEvent.ValidationError.TITLE_IS_REQUIRED)
        );
    }

    private CreateEvent.ResultHandler<List<CreateEvent.ValidationError>> grabExpectedErrors = new CreateEvent.ResultHandler<List<CreateEvent.ValidationError>>() {
        @Override
        public List<CreateEvent.ValidationError> eventCreated(Event event) {
            fail("Expected event to be invalid, but was created");
            return null;
        }

        @Override
        public List<CreateEvent.ValidationError> eventNotCreatedDueToValidationErrors(List<CreateEvent.ValidationError> errors) {
            return errors;
        }
    };

    private CreateEvent.ResultHandler<Event> grabExpectedEvent = new CreateEvent.ResultHandler<Event>() {
        @Override
        public Event eventCreated(Event event) {
            return event;
        }

        @Override
        public Event eventNotCreatedDueToValidationErrors(List<CreateEvent.ValidationError> errors) {
            fail("Expected event to be created, but was invalid");
            return null;
        }
    };
}