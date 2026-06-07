import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShoppingItemEditComponent } from './shopping-item-edit.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { HouseholdService } from '../../services/household.service';
import { AuthService } from '../../services/auth.service';
import { ShoppingItemDto, ShoppingItemStatus, ShoppingItemVisibility, QuantityUnit } from '../../dtos/shopping-item';
import { Roommate } from '../../dtos/roommate';

describe('ShoppingItemEditComponent', () => {
  let component: ShoppingItemEditComponent;
  let fixture: ComponentFixture<ShoppingItemEditComponent>;
  let shoppingSpy: jasmine.SpyObj<ShoppingItemService>;
  let householdSpy: jasmine.SpyObj<HouseholdService>;
  let authSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const existing: ShoppingItemDto = {
    id: 42, name: 'Milk', quantityCount: null, quantityAmount: 2, quantityUnit: QuantityUnit.LITER, notes: 'whole',
    status: ShoppingItemStatus.NEEDED, visibility: ShoppingItemVisibility.TARGETED,
    householdId: 10, createdById: 100,
    createdByEmail: 'me@test.com', createdByFirstName: 'Me', createdByLastName: '!',
    targetUserId: 200, targetUserEmail: 'bob@test.com',
    targetUserFirstName: 'Bob', targetUserLastName: 'X',
    createdAt: '', updatedAt: ''
  };
  const me: Roommate = {
    id: 1, userId: 100, firstName: 'Me', lastName: '!', email: 'me@test.com',
    role: 'MAIN_TENANT', joinedAt: ''
  };
  const otherRoommate: Roommate = {
    id: 2, userId: 200, firstName: 'Bob', lastName: 'X', email: 'bob@test.com',
    role: 'ROOMMATE', joinedAt: ''
  };

  beforeEach(async () => {
    shoppingSpy = jasmine.createSpyObj('ShoppingItemService', ['findOne', 'update']);
    shoppingSpy.findOne.and.returnValue(of(existing));
    shoppingSpy.update.and.returnValue(of(existing));

    householdSpy = jasmine.createSpyObj('HouseholdService', ['getMyHousehold', 'getMembers']);
    householdSpy.getMyHousehold.and.returnValue(of({ id: 10, name: 'WG' } as any));
    householdSpy.getMembers.and.returnValue(of([me, otherRoommate]));

    authSpy = jasmine.createSpyObj('AuthService', ['getUserEmail']);
    authSpy.getUserEmail.and.returnValue('me@test.com');

    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemEditComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, RouterModule.forRoot([])],
      providers: [
        { provide: ShoppingItemService, useValue: shoppingSpy },
        { provide: HouseholdService, useValue: householdSpy },
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '42' }) } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemEditComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads existing item with visibility + target', () => {
    fixture.detectChanges();
    expect(shoppingSpy.findOne).toHaveBeenCalledWith(42);
    expect(component.itemForm.value.visibility).toBe(ShoppingItemVisibility.TARGETED);
    expect(component.itemForm.value.targetUserEmail).toBe('bob@test.com');
    expect(component.itemForm.value.quantityAmount).toBe(2);
    expect(component.itemForm.value.quantityUnit).toBe(QuantityUnit.LITER);
    expect(component.quantityPreview).toBe('2 L');
    expect(component.loading).toBeFalse();
  });

  it('filters current user from roommates', () => {
    fixture.detectChanges();
    expect(component.roommates.length).toBe(1);
    expect(component.roommates[0].email).toBe('bob@test.com');
  });

  it('onSubmit() updates with the chosen visibility', () => {
    fixture.detectChanges();
    component.itemForm.patchValue({
      name: 'Milk', visibility: ShoppingItemVisibility.SHARED
    });
    component.onSubmit();
    const dto = shoppingSpy.update.calls.mostRecent().args[1] as any;
    expect(dto.visibility).toBe(ShoppingItemVisibility.SHARED);
    expect(dto.targetUserEmail).toBeUndefined();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/shopping-items']);
  });

  it('handles loading errors', () => {
    shoppingSpy.findOne.and.returnValue(throwError(() => ({ status: 404 })));
    fixture.detectChanges();
    expect(component.error).toBeTrue();
    expect(component.errorMessage).toContain('Failed to load');
  });

  it('blocks repeated submission while submitting', () => {
    fixture.detectChanges();
    component.submitting = true;
    component.onSubmit();
    expect(shoppingSpy.update).not.toHaveBeenCalled();
  });
});
