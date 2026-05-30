import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { HouseholdService } from '../../services/household.service';
import { AuthService } from '../../services/auth.service';
import { Roommate } from '../../dtos/roommate';
import { ShoppingItemUpdateDto, ShoppingItemVisibility } from '../../dtos/shopping-item';

@Component({
  selector: 'app-shopping-item-edit',
  templateUrl: './shopping-item-edit.component.html',
  styleUrls: ['./shopping-item-edit.component.scss'],
  standalone: false
})
export class ShoppingItemEditComponent implements OnInit {

  itemId!: number;
  itemForm: FormGroup;
  roommates: Roommate[] = [];
  submitted = false;
  submitting = false;
  error = false;
  errorMessage = '';
  loading = true;

  ShoppingItemVisibility = ShoppingItemVisibility;

  constructor(
    private formBuilder: FormBuilder,
    private shoppingItemService: ShoppingItemService,
    private householdService: HouseholdService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.itemForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      quantity: ['', [Validators.maxLength(50)]],
      notes: ['', [Validators.maxLength(2000)]],
      visibility: [ShoppingItemVisibility.PERSONAL, [Validators.required]],
      targetUserEmail: ['']
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.itemId = Number(idParam);
      this.loadRoommatesAndItem();
    } else {
      this.router.navigate(['/shopping-items']);
    }
  }

  loadRoommatesAndItem() {
    this.householdService.getMyHousehold().subscribe({
      next: (household) => {
        if (household && household.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: (members) => {
              const currentEmail = this.authService.getUserEmail();
              this.roommates = members.filter(m => m.email !== currentEmail);
              this.loadItem();
            },
            error: (err) => {
              console.error('Error fetching roommates', err);
              this.error = true;
              this.errorMessage = 'Failed to load household members.';
              this.loading = false;
            }
          });
        }
      },
      error: (err) => {
        console.error('Error fetching household', err);
        this.error = true;
        this.errorMessage = 'Failed to load household.';
        this.loading = false;
      }
    });
  }

  loadItem() {
    this.shoppingItemService.findOne(this.itemId).subscribe({
      next: (item) => {
        this.itemForm.patchValue({
          name: item.name,
          quantity: item.quantity || '',
          notes: item.notes || '',
          visibility: item.visibility,
          targetUserEmail: item.targetUserEmail || ''
        });

        // Dynamically append the current (inactive) target user to the dropdown selection list
        // if they are not present in the active roommate list, ensuring the dropdown is not blank.
        if (item.visibility === ShoppingItemVisibility.TARGETED && item.targetUserEmail) {
          const hasTarget = this.roommates.some(m => m.email === item.targetUserEmail);
          if (!hasTarget) {
            const inactiveRoommate: Roommate = {
              id: item.targetUserId || 0,
              userId: item.targetUserId || 0,
              firstName: item.targetUserFirstName || 'Inactive',
              lastName: item.targetUserLastName || 'User',
              email: item.targetUserEmail,
              role: 'ROOMMATE',
              joinedAt: ''
            };
            this.roommates.push(inactiveRoommate);
          }
        }

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

  isTargeted(): boolean {
    return this.itemForm.get('visibility')?.value === ShoppingItemVisibility.TARGETED;
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
    const dto: ShoppingItemUpdateDto = {
      name: raw.name,
      quantity: raw.quantity || undefined,
      notes: raw.notes || undefined,
      visibility: raw.visibility,
      targetUserEmail: raw.visibility === ShoppingItemVisibility.TARGETED
        ? raw.targetUserEmail
        : undefined
    };

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
            if (parts.length > 1 && ['name', 'quantity', 'notes', 'visibility', 'targetUserEmail'].includes(parts[0])) {
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
