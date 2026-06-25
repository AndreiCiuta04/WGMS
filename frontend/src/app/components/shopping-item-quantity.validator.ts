import { AbstractControl, ValidationErrors } from '@angular/forms';

export function structuredQuantityValidator(control: AbstractControl): ValidationErrors | null {
  const amount = control.get('quantityAmount')?.value;
  const unit = control.get('quantityUnit')?.value;

  const hasAmount = amount !== null && amount !== undefined && amount !== '';
  const hasUnit = unit !== null && unit !== undefined && unit !== '';

  return hasAmount === hasUnit ? null : { quantityMeasureIncomplete: true };
}
