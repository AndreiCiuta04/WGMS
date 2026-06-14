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

  it('should read a whole-euro total that has no decimal places', () => {
    const draft = service.parseReceiptText(`
      Spar
      Gesamt EUR 18
    `);

    expect(draft.totalAmount).toBe(18);
  });

  it('should parse a thousands-separated total without truncating it', () => {
    const draft = service.parseReceiptText(`
      Moebelhaus
      Summe 1.234,56
    `);

    expect(draft.totalAmount).toBe(1234.56);
  });

  it('should not mistake a date for the total amount', () => {
    const draft = service.parseReceiptText(`
      Billa
      07.06.2026
      Gesamt 9
    `);

    expect(draft.totalAmount).toBe(9);
    expect(draft.date).toBe('2026-06-07');
  });

  it('should pick the largest figure on a total line with several numbers', () => {
    const draft = service.parseReceiptText(`
      Restaurant
      TOTAL 15,00 EUR 3,00 Trinkgeld
    `);

    expect(draft.totalAmount).toBe(15);
  });

  it('should keep a merchant title that contains digits and not fall back to the date line', () => {
    const draft = service.parseReceiptText(`
      FR14 MARKET
      07.06.2026
      Total EUR 18,00
    `);

    expect(draft.description).toBe('FR14 MARKET');
    expect(draft.totalAmount).toBe(18);
    expect(draft.date).toBe('2026-06-07');
  });

  it('should understand a two-digit-year European date', () => {
    const draft = service.parseReceiptText(`
      Hofer
      07.06.26
      Betrag 4,20
    `);

    expect(draft.date).toBe('2026-06-07');
    expect(draft.totalAmount).toBe(4.2);
  });
});
