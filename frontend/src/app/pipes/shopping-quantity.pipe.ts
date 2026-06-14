import { Pipe, PipeTransform } from '@angular/core';
import { formatQuantity, StructuredQuantity } from '../dtos/shopping-item';

/** Renders a shopping item's structured quantity (count + amount + unit) as a single string, e.g. "5 × 3 L". */
@Pipe({
  name: 'shoppingQuantity',
  standalone: false
})
export class ShoppingQuantityPipe implements PipeTransform {
  transform(item: StructuredQuantity | null | undefined): string {
    return formatQuantity(item);
  }
}
