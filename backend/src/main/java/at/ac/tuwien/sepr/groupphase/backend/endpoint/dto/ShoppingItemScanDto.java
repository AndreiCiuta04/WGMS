package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for FR-18 one-click shopping entry via QR scan.
 *
 * <p>Carries the raw string the client decoded from a QR code. The client does
 * not pre-parse or trust it; the backend ({@code ShoppingQrPayloadParser}) is the
 * sole authority that validates the payload and turns it into a shopping item,
 * as required by section 9.5 of the project contract.</p>
 *
 * @param payload the exact text decoded from the scanned QR code
 */
public record ShoppingItemScanDto(
    @NotBlank
    @Size(max = 4096)
    String payload
) {}
