package at.ac.tuwien.sepr.groupphase.backend.basetest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface TestData {

    Long ID = 1L;

    String TEST_NEWS_TITLE = "Title";
    String TEST_NEWS_SUMMARY = "Summary";
    String TEST_NEWS_TEXT = "TestMessageText";
    LocalDateTime TEST_NEWS_PUBLISHED_AT =
        LocalDateTime.of(2019, 11, 13, 12, 15, 0, 0);

    String BASE_URI = "/api/v1";
    String MESSAGE_BASE_URI = BASE_URI + "/messages";
    String TASK_BASE_URI = BASE_URI + "/tasks";

    String ADMIN_USER = "admin@email.com";
    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
            add("ROLE_USER");
        }
    };

    String DEFAULT_USER = "user@email.com";
    List<String> USER_ROLES = new ArrayList<>() {
        {
            add("ROLE_USER");
        }
    };

    String MAIN_TENANT_EMAIL = "main.tenant@test.com";
    String ROOMMATE_EMAIL = "roommate@test.com";
    String SECOND_ROOMMATE_EMAIL = "second.roommate@test.com";
    String OUTSIDER_EMAIL = "outsider@test.com";

    String TEST_HOUSEHOLD_NAME = "QA Test WG";
    String OTHER_HOUSEHOLD_NAME = "Other Test WG";

    String TEST_TASK_TITLE = "Clean the kitchen";
    String TEST_TASK_DESCRIPTION = "Clean the kitchen surfaces, sink, and floor";
    String UPDATED_TASK_TITLE = "Clean the bathroom";
    String UPDATED_TASK_DESCRIPTION = "Clean the bathroom sink, mirror, and floor";
    LocalDate TEST_TASK_DUE_DATE = LocalDate.now().plusDays(3);
    LocalDate UPDATED_TASK_DUE_DATE = LocalDate.now().plusDays(5);
}