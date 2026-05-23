import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { ShoppingItemUpdateDto } from '../../dtos/shopping-item';

@Component({
  selector: 'app-shopping-item-edit',
  templateUrl: './shopping-item-edit.component.html',
  styleUrls: ['./shopping-item-edit.component.scss'],
  standalone: false
})
export class ShoppingItemEditComponent implements OnInit {

  itemId!: number;
  itemForm: FormGroup;
  submitted = false;
  submitting = false;
  error = false;
  errorMessage = '';
  loading = true;

  constructor(
    private formBuilder: FormBuilder,
    private shoppingItemService: ShoppingItemService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.itemForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      quantity: ['', [Validators.maxLength(50)]],
      notes: ['', [Validators.maxLength(2000)]]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.itemId = Number(idParam);
      this.loadItem();
    } else {
      this.router.navigate(['/shopping-items']);
    }
  }

  loadItem() {
    this.shoppingItemService.findOne(this.itemId).subscribe({
      next: (item) => {
        this.itemForm.patchValue({
          name: item.name,
          quantity: item.quantity || '',
          notes: item.notes || ''
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching shopping item', err);
        this.error = true;
        this.errorMessage = 'Failed to load the shopping item.';
        this.loading = false;
      }
    });
  }

  onSubmit() {
    this.submitted = true;
    if (this.itemForm.invalid || this.submitting) {
      return;
    }

    this.submitting = true;
    this.error = false;
    const dto: ShoppingItemUpdateDto = this.itemForm.value;

    this.shoppingItemService.update(this.itemId, dto).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/shopping-items']);
      },
      error: (err) => {
        this.submitting = false;
        console.error('Error updating shopping item', err);
        this.error = true;

        let errorMsg = err.error?.message
          || 'An error occurred while updating the item. Please try again.';

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
