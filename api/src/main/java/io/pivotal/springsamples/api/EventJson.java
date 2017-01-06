package io.pivotal.springsamples.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.springsamples.Event;

import java.time.format.DateTimeFormatter;

class EventJson {
    @JsonProperty
    private final String id;

    @JsonProperty
    private final String title;

    @JsonProperty
    private final String date;

    EventJson(String id, String title, String date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    EventJson(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.date = event.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
