import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { HouseholdService } from '../../services/household.service';
import { AuthService } from '../../services/auth.service';
import { Roommate } from '../../dtos/roommate';
import { ShoppingItemCreateDto, ShoppingItemVisibility, QUANTITY_UNIT_OPTIONS, formatQuantity } from '../../dtos/shopping-item';
import { structuredQuantityValidator } from '../shopping-item-quantity.validator';

@Component({
  selector: 'app-shopping-item-create',
  templateUrl: './shopping-item-create.component.html',
  styleUrls: ['./shopping-item-create.component.scss'],
  standalone: false
})
export class ShoppingItemCreateComponent implements OnInit {

  itemForm: FormGroup;
  roommates: Roommate[] = [];
  submitted = false;
  submitting = false;
  error = false;
  errorMessage = '';

  ShoppingItemVisibility = ShoppingItemVisibility;
  quantityUnitOptions = QUANTITY_UNIT_OPTIONS;

  constructor(
    private formBuilder: FormBuilder,
    private shoppingItemService: ShoppingItemService,
    private householdService: HouseholdService,
    private authService: AuthService,
    private router: Router
  ) {
    this.itemForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      quantityCount: [null, [Validators.min(1)]],
      quantityAmount: [null, [Validators.min(0.001)]],
      quantityUnit: [null],
      notes: ['', [Validators.maxLength(2000)]],
      visibility: [ShoppingItemVisibility.PERSONAL, [Validators.required]],
      targetUserEmail: ['']
    }, { validators: structuredQuantityValidator });
  }

  ngOnInit(): void {
    this.loadRoommates();
  }

  loadRoommates() {
    const currentEmail = this.authService.getUserEmail();
    this.householdService.getMyHousehold().subscribe({
      next: (household) => {
        if (household && household.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: (members) => {
              // Filter out the current user — backend rejects self-target with 409;
              // hiding it in the UI makes the rule obvious.
              this.roommates = members.filter(m => m.email !== currentEmail);
            },
            error: (err) => console.error('Error fetching roommates', err)
          });
        }
      },
      error: (err) => console.error('Error fetching household', err)
    });
  }

  isTargeted(): boolean {
    return this.itemForm.get('visibility')?.value === ShoppingItemVisibility.TARGETED;
  }

  /** Live "5 × 3 L" preview of the structured quantity inputs. */
  get quantityPreview(): string {
    const v = this.itemForm.value;
    return formatQuantity({
      quantityCount: this.toNum(v.quantityCount),
      quantityAmount: this.toNum(v.quantityAmount),
      quantityUnit: v.quantityUnit || null
    });
  }

  /** Steps the "how many" count up/down; clears it when it drops below 1. */
  adjustCount(delta: number): void {
    const ctrl = this.itemForm.get('quantityCount');
    const next = (this.toNum(ctrl?.value) ?? 0) + delta;
    ctrl?.setValue(next < 1 ? null : next);
    ctrl?.markAsDirty();
  }

  private toNum(value: any): number | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }
    const n = Number(value);
    return isNaN(n) ? null : n;
  }

  onSubmit() {
    this.submitted = true;

    if (this.itemForm.invalid || this.submitting) {
      return;
    }
    if (this.isTargeted() && !this.itemForm.get('targetUserEmail')?.value) {
      return;
    }

    this.submitting = true;
    this.error = false;
    const raw = this.itemForm.value;
    const dto: ShoppingItemCreateDto = {
      name: raw.name,
      quantityCount: this.toNum(raw.quantityCount) ?? undefined,
      quantityAmount: this.toNum(raw.quantityAmount) ?? undefined,
      quantityUnit: raw.quantityUnit || undefined,
      notes: raw.notes || undefined,
      visibility: raw.visibility,
      targetUserEmail: raw.visibility === ShoppingItemVisibility.TARGETED
        ? raw.targetUserEmail
        : undefined
    };

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
            if (parts.length > 1 && ['name', 'quantityCount', 'quantityAmount', 'quantityUnit', 'notes', 'visibility', 'targetUserEmail'].includes(parts[0])) {
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
