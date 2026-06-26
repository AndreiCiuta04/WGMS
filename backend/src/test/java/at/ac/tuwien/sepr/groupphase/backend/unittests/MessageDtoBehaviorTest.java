package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.MessageInquiryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SimpleMessageDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageDtoBehaviorTest {

    @Test
    public void simpleMessageDtoBuilderPopulatesFieldsAndValueMethodsUseAllFields() {
        LocalDateTime publishedAt = LocalDateTime.of(2026, 6, 26, 12, 0);

        SimpleMessageDto first = SimpleMessageDto.SimpleMessageDtoBuilder.aSimpleMessageDto()
            .withId(1L)
            .withPublishedAt(publishedAt)
            .withTitle("Title")
            .withSummary("Summary")
            .build();
        SimpleMessageDto same = SimpleMessageDto.SimpleMessageDtoBuilder.aSimpleMessageDto()
            .withId(1L)
            .withPublishedAt(publishedAt)
            .withTitle("Title")
            .withSummary("Summary")
            .build();
        SimpleMessageDto different = SimpleMessageDto.SimpleMessageDtoBuilder.aSimpleMessageDto()
            .withId(2L)
            .withPublishedAt(publishedAt)
            .withTitle("Title")
            .withSummary("Summary")
            .build();

        assertAll(
            () -> assertEquals(1L, first.getId()),
            () -> assertEquals(publishedAt, first.getPublishedAt()),
            () -> assertEquals("Title", first.getTitle()),
            () -> assertEquals("Summary", first.getSummary()),
            () -> assertEquals(first, same),
            () -> assertEquals(first.hashCode(), same.hashCode()),
            () -> assertNotEquals(first, different),
            () -> assertNotEquals(first, "not a dto"),
            () -> assertTrue(first.toString().contains("Title"))
        );
    }

    @Test
    public void detailedMessageDtoBuilderIncludesInheritedFieldsAndTextInEquality() {
        LocalDateTime publishedAt = LocalDateTime.of(2026, 6, 26, 12, 30);

        DetailedMessageDto first = DetailedMessageDto.DetailedMessageDtoBuilder.aDetailedMessageDto()
            .withId(3L)
            .withPublishedAt(publishedAt)
            .withTitle("Detailed")
            .withSummary("Short")
            .withText("Long text")
            .build();
        DetailedMessageDto same = DetailedMessageDto.DetailedMessageDtoBuilder.aDetailedMessageDto()
            .withId(3L)
            .withPublishedAt(publishedAt)
            .withTitle("Detailed")
            .withSummary("Short")
            .withText("Long text")
            .build();
        DetailedMessageDto differentText = DetailedMessageDto.DetailedMessageDtoBuilder.aDetailedMessageDto()
            .withId(3L)
            .withPublishedAt(publishedAt)
            .withTitle("Detailed")
            .withSummary("Short")
            .withText("Other text")
            .build();

        assertAll(
            () -> assertEquals("Long text", first.getText()),
            () -> assertEquals("Detailed", first.getTitle()),
            () -> assertEquals(first, same),
            () -> assertEquals(first.hashCode(), same.hashCode()),
            () -> assertNotEquals(first, differentText),
            () -> assertFalse(first.equals(new SimpleMessageDto())),
            () -> assertTrue(first.toString().contains("Long text"))
        );
    }

    @Test
    public void messageInquiryDtoBuilderPopulatesFieldsAndValueMethodsUseAllFields() {
        MessageInquiryDto first = MessageInquiryDto.MessageInquiryDtoBuilder.aMessageInquiryDto()
            .withTitle("Question")
            .withSummary("Summary")
            .withText("Body")
            .build();
        MessageInquiryDto same = MessageInquiryDto.MessageInquiryDtoBuilder.aMessageInquiryDto()
            .withTitle("Question")
            .withSummary("Summary")
            .withText("Body")
            .build();
        MessageInquiryDto different = MessageInquiryDto.MessageInquiryDtoBuilder.aMessageInquiryDto()
            .withTitle("Question")
            .withSummary("Different")
            .withText("Body")
            .build();

        assertAll(
            () -> assertEquals("Question", first.getTitle()),
            () -> assertEquals("Summary", first.getSummary()),
            () -> assertEquals("Body", first.getText()),
            () -> assertEquals(first, same),
            () -> assertEquals(first.hashCode(), same.hashCode()),
            () -> assertNotEquals(first, different),
            () -> assertNotEquals(first, null),
            () -> assertTrue(first.toString().contains("Question"))
        );
    }
}
