import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, flushMicrotasks, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import QRCode from 'qrcode';
import {
  QuantityUnit,
  ShoppingItemDto,
  ShoppingItemStatus,
  ShoppingItemVisibility
} from '../../dtos/shopping-item';
import { ShoppingItemQrShareComponent } from './shopping-item-qr-share.component';

describe('ShoppingItemQrShareComponent', () => {
  let component: ShoppingItemQrShareComponent;
  let fixture: ComponentFixture<ShoppingItemQrShareComponent>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;

  const item: ShoppingItemDto = {
    id: 42,
    name: 'Coffee',
    quantityCount: 2,
    quantityAmount: 500,
    quantityUnit: QuantityUnit.GRAM,
    notes: 'Whole beans',
    status: ShoppingItemStatus.NEEDED,
    visibility: ShoppingItemVisibility.TARGETED,
    householdId: 1,
    createdById: 2,
    createdByEmail: 'buyer@example.com',
    createdByFirstName: 'Bean',
    createdByLastName: 'Buyer',
    targetUserId: 3,
    targetUserEmail: 'target@example.com',
    targetUserFirstName: 'Target',
    targetUserLastName: 'User',
    createdAt: '2026-06-01T10:00:00',
    updatedAt: '2026-06-01T10:00:00'
  };

  beforeEach(async () => {
    activeModal = jasmine.createSpyObj<NgbActiveModal>('NgbActiveModal', ['dismiss']);

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemQrShareComponent],
      providers: [{ provide: NgbActiveModal, useValue: activeModal }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemQrShareComponent);
    component = fixture.componentInstance;
  });

  it('should render generated QR image and quantity label', fakeAsync(() => {
    const toDataUrlSpy = spyOn(QRCode, 'toDataURL') as jasmine.Spy;
    toDataUrlSpy.and.returnValue(Promise.resolve('data:image/png;base64,qr'));

    component.item = item;
    flushMicrotasks();
    fixture.detectChanges();

    expect(component.qrDataUrl).toBe('data:image/png;base64,qr');
    expect(component.renderError).toBeFalse();
    expect(component.quantityLabel).toBe('2 × 500 g');
    expect(fixture.nativeElement.querySelector('[data-cy="qr-share-name"]').textContent).toContain('Coffee');
    expect(fixture.nativeElement.querySelector('[data-cy="qr-share-image"]').getAttribute('src')).toBe('data:image/png;base64,qr');
    expect(toDataUrlSpy).toHaveBeenCalledWith(
      jasmine.stringMatching('"visibility":"SHARED"'),
      { width: 240, margin: 2, errorCorrectionLevel: 'M' }
    );
  }));

  it('should show an error when QR rendering fails', fakeAsync(() => {
    const toDataUrlSpy = spyOn(QRCode, 'toDataURL') as jasmine.Spy;
    toDataUrlSpy.and.returnValue(Promise.reject(new Error('render failed')));

    component.item = item;
    flushMicrotasks();
    fixture.detectChanges();

    expect(component.renderError).toBeTrue();
    expect(component.qrDataUrl).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-cy="qr-share-error"]').textContent)
      .toContain('Could not generate');
  }));

  it('should tolerate missing item before input assignment and dismiss on close', () => {
    fixture.detectChanges();

    expect(component.item).toBeUndefined();
    expect(component.quantityLabel).toBe('');

    fixture.nativeElement.querySelector('[data-cy="qr-share-close"]').click();

    expect(activeModal.dismiss).toHaveBeenCalled();
  });
});
