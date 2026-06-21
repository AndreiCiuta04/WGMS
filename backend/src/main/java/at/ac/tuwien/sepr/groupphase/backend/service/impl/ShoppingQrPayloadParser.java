package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Parses and validates the textual payload decoded from a scanned QR code (FR-18)
 * into a {@link ShoppingItemCreateDto}.
 *
 * <p>This is the backend trust boundary demanded by the project contract
 * (section 9.5: "scanned payloads must be validated on the backend before any
 * shopping item is created"). The frontend forwards the raw decoded string and
 * performs no authoritative validation of its own; everything below is enforced
 * server-side regardless of what the client sent.</p>
 *
 * <p>Expected payload format (compact JSON, UTF-8):</p>
 * <pre>
 * {
 *   "type": "wgms.shopping-item",
 *   "version": 1,
 *   "name": "Oat Milk",            // required, 1..100 chars
 *   "quantityCount": 2,             // optional, integer &gt;= 1
 *   "quantityAmount": 1,            // optional, &gt; 0, up to 7 integer + 3 fraction digits
 *   "quantityUnit": "LITER",        // optional, one of QuantityUnit; required iff quantityAmount is set
 *   "notes": "barista edition",     // optional, up to 2000 chars
 *   "visibility": "SHARED"          // optional, PERSONAL or SHARED (default SHARED); TARGETED is rejected
 * }
 * </pre>
 *
 * <p>The class is deliberately free of Spring wiring so it can be unit-tested in
 * isolation without a context. {@link ShoppingItemServiceImpl} holds a single
 * instance and delegates persistence to its normal create path.</p>
 */
public class ShoppingQrPayloadParser {

    /** Marker every WGMS shopping QR must carry, so unrelated codes (URLs, etc.) are rejected cleanly. */
    public static final String EXPECTED_TYPE = "wgms.shopping-item";
    /** Currently supported payload schema version. */
    public static final int SUPPORTED_VERSION = 1;

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_NOTES_LENGTH = 2000;
    private static final int MAX_INTEGER_DIGITS = 7;
    private static final int MAX_FRACTION_DIGITS = 3;
    private static final int MAX_PAYLOAD_LENGTH = 4096;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * Parses and validates a raw scanned payload.
     *
     * @param rawPayload the exact string decoded from the QR code
     * @return a validated, ready-to-persist {@link ShoppingItemCreateDto}
     *     (visibility is always PERSONAL or SHARED, targetUserEmail is always {@code null})
     * @throws ValidationException if the payload is absent, not WGMS shopping data,
     *     of an unsupported version, or violates any field constraint
     */
    public ShoppingItemCreateDto parse(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new ValidationException("Scanned QR code is empty.");
        }
        if (rawPayload.length() > MAX_PAYLOAD_LENGTH) {
            throw new ValidationException("Scanned QR code is too large to be a valid shopping item.");
        }

        ScanPayload payload;
        try {
            payload = JSON.readValue(rawPayload.trim(), ScanPayload.class);
        } catch (RuntimeException ex) {
            // Jackson 3 surfaces parse/binding problems as unchecked exceptions.
            throw new ValidationException("Scanned code is not a valid WGMS shopping item.");
        }
        if (payload == null) {
            throw new ValidationException("Scanned code is not a valid WGMS shopping item.");
        }

        requireWgmsShoppingType(payload.type());
        requireSupportedVersion(payload.version());

        String name = validateName(payload.name());
        Integer quantityCount = validateQuantityCount(payload.quantityCount());
        BigDecimal quantityAmount = validateQuantityAmount(payload.quantityAmount());
        QuantityUnit quantityUnit = validateQuantityUnit(payload.quantityUnit());
        requireAmountAndUnitTogether(quantityAmount, quantityUnit);
        String notes = validateNotes(payload.notes());
        ShoppingItemVisibility visibility = validateVisibility(payload.visibility());

        return new ShoppingItemCreateDto(
            name, quantityCount, quantityAmount, quantityUnit, notes, visibility, null);
    }

    private void requireWgmsShoppingType(String type) {
        if (type == null || !EXPECTED_TYPE.equals(type.trim())) {
            throw new ValidationException("This QR code is not a WGMS shopping item.");
        }
    }

    private void requireSupportedVersion(Integer version) {
        if (version == null || version != SUPPORTED_VERSION) {
            throw new ValidationException(
                "Unsupported WGMS shopping QR version. This app understands version " + SUPPORTED_VERSION + ".");
        }
    }

    private String validateName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new ValidationException("Scanned item is missing a name.");
        }
        String name = rawName.trim();
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Scanned item name must not exceed " + MAX_NAME_LENGTH + " characters.");
        }
        return name;
    }

    private Integer validateQuantityCount(Integer quantityCount) {
        if (quantityCount == null) {
            return null;
        }
        if (quantityCount < 1) {
            throw new ValidationException("Scanned item count must be at least 1.");
        }
        return quantityCount;
    }

    private BigDecimal validateQuantityAmount(BigDecimal quantityAmount) {
        if (quantityAmount == null) {
            return null;
        }
        if (quantityAmount.signum() <= 0) {
            throw new ValidationException("Scanned item amount must be greater than zero.");
        }
        if (quantityAmount.scale() > MAX_FRACTION_DIGITS
            || (quantityAmount.precision() - quantityAmount.scale()) > MAX_INTEGER_DIGITS) {
            throw new ValidationException(
                "Scanned item amount must have at most " + MAX_INTEGER_DIGITS
                    + " integer and " + MAX_FRACTION_DIGITS + " fraction digits.");
        }
        return quantityAmount;
    }

    private QuantityUnit validateQuantityUnit(String rawUnit) {
        if (rawUnit == null || rawUnit.isBlank()) {
            return null;
        }
        try {
            return QuantityUnit.valueOf(rawUnit.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Scanned item has an unknown measurement unit: " + rawUnit + ".");
        }
    }

    private void requireAmountAndUnitTogether(BigDecimal amount, QuantityUnit unit) {
        if ((amount == null) != (unit == null)) {
            throw new ValidationException("Scanned item must specify both a measure amount and a unit, or neither.");
        }
    }

    private String validateNotes(String notes) {
        if (notes == null) {
            return null;
        }
        if (notes.length() > MAX_NOTES_LENGTH) {
            throw new ValidationException("Scanned item notes must not exceed " + MAX_NOTES_LENGTH + " characters.");
        }
        return notes;
    }

    private ShoppingItemVisibility validateVisibility(String rawVisibility) {
        if (rawVisibility == null || rawVisibility.isBlank()) {
            return ShoppingItemVisibility.SHARED;
        }
        ShoppingItemVisibility visibility;
        try {
            visibility = ShoppingItemVisibility.valueOf(rawVisibility.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Scanned item has an unknown visibility: " + rawVisibility + ".");
        }
        if (visibility == ShoppingItemVisibility.TARGETED) {
            throw new ValidationException(
                "QR shopping codes can only create PERSONAL or SHARED items, not targeted requests.");
        }
        return visibility;
    }

    /**
     * Raw, untrusted shape bound straight from the QR JSON. Jackson 3 is lenient
     * about unknown properties by default, so extra keys are ignored rather than
     * rejected; every field here is validated explicitly in {@link #parse(String)}.
     */
    private record ScanPayload(
        String type,
        Integer version,
        String name,
        Integer quantityCount,
        BigDecimal quantityAmount,
        String quantityUnit,
        String notes,
        String visibility
    ) {}
}
