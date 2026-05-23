import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { ShoppingItemCreateDto } from '../../dtos/shopping-item';

@Component({
  selector: 'app-shopping-item-create',
  templateUrl: './shopping-item-create.component.html',
  styleUrls: ['./shopping-item-create.component.scss'],
  standalone: false
})
export class ShoppingItemCreateComponent {

  itemForm: FormGroup;
  submitted = false;
  submitting = false;
  error = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private shoppingItemService: ShoppingItemService,
    private router: Router
  ) {
    this.itemForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      quantity: ['', [Validators.maxLength(50)]],
      notes: ['', [Validators.maxLength(2000)]]
    });
  }

  onSubmit() {
    this.submitted = true;
    if (this.itemForm.invalid || this.submitting) {
      return;
    }

    this.submitting = true;
    this.error = false;
    const dto: ShoppingItemCreateDto = this.itemForm.value;

    this.shoppingItemService.create(dto).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/shopping-items']);
      },
      error: (err) => {
        this.submitting = false;
        console.error('Error creating shopping item', err);
        this.error = true;

        let errorMsg = err.error?.message
          || 'An error occurred while adding the item. Please try again.';

        let errorsArray: string[] = [];
        if (err.error?.['Validation errors']) {
          errorsArray = err.error['Validation errors'];
        } else if (typeof err.error === 'string' && err.error.includes('Validation errors=')) {
          const match = err.error.match(/Validation errors=\[(.*?)\]/);
          if (match && match[1]) {
            errorsArray = match[1].split(', ');
          }
        }

        if (errorsArray.length > 0) {
          errorMsg = errorsArray.map((e: string) => {
            const parts = e.split(' ');
            if (parts.length > 1 && (parts[0] === 'name' || parts[0] === 'quantity' || parts[0] === 'notes')) {
              return parts.slice(1).join(' ');
            }
            return e;
          }).join(', ');
        }

        this.errorMessage = errorMsg;
      }
    });
  }
}
