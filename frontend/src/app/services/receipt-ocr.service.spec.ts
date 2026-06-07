import { TestBed } from '@angular/core/testing';
import { ReceiptOcrService } from './receipt-ocr.service';

describe('ReceiptOcrService', () => {
  let service: ReceiptOcrService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReceiptOcrService);
  });

  it('should parse title, European date, and explicit total from OCR text', () => {
    const draft = service.parseReceiptText(`
      Billa Praterstern
      07.06.2026
      Milk 2,49
      Bread 1,79
      TOTAL EUR 18,90
    `);

    expect(draft).toEqual({
      description: 'Billa Praterstern',
      totalAmount: 18.9,
      date: '2026-06-07'
    });
  });

  it('should use the largest amount when no total label exists', () => {
    const draft = service.parseReceiptText(`
      Market Receipt
      2026-06-07
      Item A 3.20
      Card 12.40
    `);

    expect(draft.description).toBe('Market Receipt');
    expect(draft.totalAmount).toBe(12.4);
    expect(draft.date).toBe('2026-06-07');
  });

  it('should return null amount when OCR text contains no money value', () => {
    const draft = service.parseReceiptText('Unreadable receipt');

    expect(draft.description).toBe('Unreadable receipt');
    expect(draft.totalAmount).toBeNull();
  });
});
