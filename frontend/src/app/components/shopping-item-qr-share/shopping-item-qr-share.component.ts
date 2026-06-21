import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import QRCode from 'qrcode';
import { ShoppingItemDto, buildShoppingItemQrPayload, formatQuantity } from '../../dtos/shopping-item';

/**
 * Modal that renders a scannable QR code for a shopping item, so one roommate can
 * share an item and another can add it with the FR-18 scanner. The encoded
 * payload is exactly what the backend ShoppingQrPayloadParser validates.
 *
 * The QR is generated from the {@code item} input setter (the standard ng-bootstrap
 * pattern of assigning {@code componentInstance.item} after opening the modal), so
 * there is no dependency on view-init timing.
 */
@Component({
  selector: 'app-shopping-item-qr-share',
  templateUrl: './shopping-item-qr-share.component.html',
  styleUrls: ['./shopping-item-qr-share.component.scss'],
  standalone: false
})
export class ShoppingItemQrShareComponent {

  qrDataUrl: string | null = null;
  renderError = false;

  private internalItem?: ShoppingItemDto;

  constructor(public activeModal: NgbActiveModal) { }

  @Input()
  set item(value: ShoppingItemDto) {
    this.internalItem = value;
    this.generate();
  }

  get item(): ShoppingItemDto | undefined {
    return this.internalItem;
  }

  get quantityLabel(): string {
    return this.internalItem ? formatQuantity(this.internalItem) : '';
  }

  private generate(): void {
    if (!this.internalItem) {
      return;
    }
    const payload = buildShoppingItemQrPayload(this.internalItem);
    QRCode.toDataURL(payload, { width: 240, margin: 2, errorCorrectionLevel: 'M' })
      .then(url => {
        this.qrDataUrl = url;
        this.renderError = false;
      })
      .catch(() => {
        this.renderError = true;
      });
  }
}
