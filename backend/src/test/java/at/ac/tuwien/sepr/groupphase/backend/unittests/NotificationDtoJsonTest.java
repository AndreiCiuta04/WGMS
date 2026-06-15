package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.NotificationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotificationDtoJsonTest {

    @Test
    public void serializesReadFlagAsIsRead() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setId(1L);
        dto.setMessage("test");
        dto.setType(NotificationType.TASK_ASSIGNED);
        dto.setRead(false);
        dto.setCreatedAt(LocalDateTime.now());

        String json = JsonMapper.builder().build().writeValueAsString(dto);
        assertTrue(json.contains("\"isRead\":"), "Expected isRead property in JSON but was: " + json);
    }
}
