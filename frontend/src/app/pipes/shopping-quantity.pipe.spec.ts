import { ShoppingQuantityPipe } from './shopping-quantity.pipe';
import { QuantityUnit } from '../dtos/shopping-item';

describe('ShoppingQuantityPipe', () => {
  const pipe = new ShoppingQuantityPipe();

  it('formats count + amount + unit as "5 × 3 L"', () => {
    expect(pipe.transform({ quantityCount: 5, quantityAmount: 3, quantityUnit: QuantityUnit.LITER })).toBe('5 × 3 L');
  });

  it('formats amount + unit as "3 L"', () => {
    expect(pipe.transform({ quantityAmount: 3, quantityUnit: QuantityUnit.LITER })).toBe('3 L');
  });

  it('formats grams as "500 g"', () => {
    expect(pipe.transform({ quantityAmount: 500, quantityUnit: QuantityUnit.GRAM })).toBe('500 g');
  });

  it('formats count only as "5×"', () => {
    expect(pipe.transform({ quantityCount: 5 })).toBe('5×');
  });

  it('formats a bare amount without unit', () => {
    expect(pipe.transform({ quantityAmount: 2 })).toBe('2');
  });

  it('returns empty string when there is no quantity', () => {
    expect(pipe.transform({})).toBe('');
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
  });
});
