package io.pivotal.springsamples.api;

import org.apache.http.entity.ContentType;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public abstract class FeatureTest {

    protected abstract String rootUrl();
    protected abstract Integer upcomingWindow();
    private LocalDate today = LocalDate.now();

    @Test
    public void mainWorkflow() throws Exception {

        String newEventUrl = given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .body("{\n" +
                        "  \"title\": \"Lunchtime Drawtime\",\n" +
                        "  \"date\": \"" + formatted(today) + "\"\n" +
                        "}")
                .when()
                .post(rootUrl() + "/api/events")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Lunchtime Drawtime"))
                .body("date", equalTo(formatted(today)))
                .extract()
                .header("Location");

        assertThat(newEventUrl, notNullValue());

        given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .when()
                .get(newEventUrl)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", equalTo("Lunchtime Drawtime"))
                .body("date", equalTo(formatted(today)));

        given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .body("{\n" +
                        "  \"title\": \"More Drawtime\",\n" +
                        "  \"date\": \"" + formatted(today.plusDays(upcomingWindow())) + "\"\n" +
                        "}")
                .when()
                .post(rootUrl() + "/api/events")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .body("{\n" +
                        "  \"title\": \"Even More Drawtime\",\n" +
                        "  \"date\": \"" + formatted(today.plusDays(upcomingWindow() + 1)) + "\"\n" +
                        "}")
                .when()
                .post(rootUrl() + "/api/events")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .when()
                .get(rootUrl() + "/api/events/upcoming")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(2));
    }

    @Test
    public void sadPaths() throws Exception {
        given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .when()
                .get(rootUrl() + "/api/events/nonexistent-id")
                .then()
                .statusCode(404);

        given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .body("{\n" +
                        "  \"title\": \"\",\n" +
                        "  \"date\": \"" + formatted(today.minusDays(1)) + "\"\n" +
                        "}")
                .when()
                .post(rootUrl() + "/api/events")
                .then()
                .statusCode(422)
                .body("errors[0].code", equalTo("missing_title"))
                .body("errors[0].description", equalTo("Title is a required field and must not be blank"))
                .body("errors[1].code", equalTo("date_is_past"))
                .body("errors[1].description", equalTo("The date must be today or later"))
        ;

        given()
                .contentType(ContentType.APPLICATION_JSON.toString())
                .body("{\n" +
                        "  \"title\": \"\",\n" +
                        "  \"date\": \"\"\n" +
                        "}")
                .when()
                .post(rootUrl() + "/api/events")
                .then()
                .statusCode(400)
                .body("errors[0].code", equalTo("invalid_date"))
                .body("errors[0].description", equalTo("The date must be in yyyy-mm-dd format"))
        ;
    }

    private static String formatted(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
