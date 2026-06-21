package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShoppingQrPayloadParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ShoppingQrPayloadParser} — the FR-18 backend validation
 * boundary. These exercise the parser directly, with no Spring context, so the
 * contract-9.5 rules are pinned down independently of persistence and security.
 */
class ShoppingQrPayloadParserTest {

    private final ShoppingQrPayloadParser parser = new ShoppingQrPayloadParser();

    private static String payload(String body) {
        return "{\"type\":\"wgms.shopping-item\",\"version\":1," + body + "}";
    }

    @Test
    void parsesFullValidPayloadIntoCreateDto() {
        ShoppingItemCreateDto dto = parser.parse(payload(
            "\"name\":\"Oat Milk\",\"quantityCount\":2,\"quantityAmount\":1,"
                + "\"quantityUnit\":\"LITER\",\"notes\":\"barista edition\",\"visibility\":\"SHARED\""));

        assertEquals("Oat Milk", dto.name());
        assertEquals(2, dto.quantityCount());
        assertEquals(0, new BigDecimal("1").compareTo(dto.quantityAmount()));
        assertEquals(QuantityUnit.LITER, dto.quantityUnit());
        assertEquals("barista edition", dto.notes());
        assertEquals(ShoppingItemVisibility.SHARED, dto.visibility());
        assertNull(dto.targetUserEmail());
    }

    @Test
    void minimalPayloadDefaultsVisibilityToShared() {
        ShoppingItemCreateDto dto = parser.parse(payload("\"name\":\"Bread\""));

        assertEquals("Bread", dto.name());
        assertNull(dto.quantityCount());
        assertNull(dto.quantityAmount());
        assertNull(dto.quantityUnit());
        assertEquals(ShoppingItemVisibility.SHARED, dto.visibility());
    }

    @Test
    void trimsNameAndAcceptsLowercaseUnitAndVisibility() {
        ShoppingItemCreateDto dto = parser.parse(payload(
            "\"name\":\"  Apples  \",\"quantityAmount\":1.5,\"quantityUnit\":\"kilogram\",\"visibility\":\"personal\""));

        assertEquals("Apples", dto.name());
        assertEquals(QuantityUnit.KILOGRAM, dto.quantityUnit());
        assertEquals(ShoppingItemVisibility.PERSONAL, dto.visibility());
    }

    @Test
    void ignoresUnknownExtraKeys() {
        ShoppingItemCreateDto dto = parser.parse(payload("\"name\":\"Eggs\",\"somethingElse\":\"x\""));
        assertEquals("Eggs", dto.name());
    }

    @Test
    void rejectsBlankPayload() {
        assertThrows(ValidationException.class, () -> parser.parse("   "));
        assertThrows(ValidationException.class, () -> parser.parse(null));
    }

    @Test
    void rejectsNonJsonPayload() {
        assertThrows(ValidationException.class, () -> parser.parse("https://example.com/not-a-wgms-code"));
    }

    @Test
    void rejectsForeignTypeTag() {
        String foreign = "{\"type\":\"other.app\",\"version\":1,\"name\":\"Milk\"}";
        ValidationException ex = assertThrows(ValidationException.class, () -> parser.parse(foreign));
        assertTrue(ex.getMessage().toLowerCase().contains("wgms"));
    }

    @Test
    void rejectsUnsupportedVersion() {
        String future = "{\"type\":\"wgms.shopping-item\",\"version\":2,\"name\":\"Milk\"}";
        assertThrows(ValidationException.class, () -> parser.parse(future));
    }

    @Test
    void rejectsMissingName() {
        assertThrows(ValidationException.class, () -> parser.parse(payload("\"visibility\":\"SHARED\"")));
        assertThrows(ValidationException.class, () -> parser.parse(payload("\"name\":\"   \"")));
    }

    @Test
    void rejectsTooLongName() {
        String longName = "x".repeat(101);
        assertThrows(ValidationException.class, () -> parser.parse(payload("\"name\":\"" + longName + "\"")));
    }

    @Test
    void rejectsNonPositiveCount() {
        assertThrows(ValidationException.class, () -> parser.parse(payload("\"name\":\"Milk\",\"quantityCount\":0")));
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThrows(ValidationException.class,
            () -> parser.parse(payload("\"name\":\"Milk\",\"quantityAmount\":0,\"quantityUnit\":\"LITER\"")));
    }

    @Test
    void rejectsAmountWithoutUnit() {
        assertThrows(ValidationException.class, () -> parser.parse(payload("\"name\":\"Milk\",\"quantityAmount\":2")));
    }

    @Test
    void rejectsUnitWithoutAmount() {
        assertThrows(ValidationException.class, () -> parser.parse(payload("\"name\":\"Milk\",\"quantityUnit\":\"LITER\"")));
    }

    @Test
    void rejectsUnknownUnit() {
        assertThrows(ValidationException.class,
            () -> parser.parse(payload("\"name\":\"Milk\",\"quantityAmount\":1,\"quantityUnit\":\"GALLON\"")));
    }

    @Test
    void rejectsTargetedVisibility() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> parser.parse(payload("\"name\":\"Milk\",\"visibility\":\"TARGETED\"")));
        assertTrue(ex.getMessage().toLowerCase().contains("targeted"));
    }

    @Test
    void rejectsUnknownVisibility() {
        assertThrows(ValidationException.class,
            () -> parser.parse(payload("\"name\":\"Milk\",\"visibility\":\"EVERYONE\"")));
    }

    @Test
    void rejectsTooLongNotes() {
        String longNotes = "n".repeat(2001);
        assertThrows(ValidationException.class,
            () -> parser.parse(payload("\"name\":\"Milk\",\"notes\":\"" + longNotes + "\"")));
    }

    @Test
    void rejectsOversizedPayload() {
        String huge = "{\"type\":\"wgms.shopping-item\",\"version\":1,\"name\":\"Milk\",\"notes\":\""
            + "n".repeat(5000) + "\"}";
        assertThrows(ValidationException.class, () -> parser.parse(huge));
    }
}
