import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { ShoppingItemCreateComponent } from './shopping-item-create.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { HouseholdService } from '../../services/household.service';
import { AuthService } from '../../services/auth.service';
import { ShoppingItemDto, ShoppingItemStatus, ShoppingItemVisibility } from '../../dtos/shopping-item';
import { Roommate } from '../../dtos/roommate';

describe('ShoppingItemCreateComponent', () => {
  let component: ShoppingItemCreateComponent;
  let fixture: ComponentFixture<ShoppingItemCreateComponent>;
  let shoppingSpy: jasmine.SpyObj<ShoppingItemService>;
  let householdSpy: jasmine.SpyObj<HouseholdService>;
  let authSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  const me: Roommate = {
    id: 1, userId: 100, firstName: 'Me', lastName: '!', email: 'me@test.com',
    role: 'MAIN_TENANT', joinedAt: ''
  };
  const otherRoommate: Roommate = {
    id: 2, userId: 200, firstName: 'Bob', lastName: 'X', email: 'bob@test.com',
    role: 'ROOMMATE', joinedAt: ''
  };
  const created: ShoppingItemDto = {
    id: 1, name: 'X', quantity: null, notes: null,
    status: ShoppingItemStatus.NEEDED, visibility: ShoppingItemVisibility.PERSONAL,
    householdId: 10, createdById: 100,
    createdByEmail: 'me@test.com', createdByFirstName: 'Me', createdByLastName: '!',
    targetUserId: null, targetUserEmail: null,
    targetUserFirstName: null, targetUserLastName: null,
    createdAt: '', updatedAt: ''
  };

  beforeEach(async () => {
    shoppingSpy = jasmine.createSpyObj('ShoppingItemService', ['create']);
    shoppingSpy.create.and.returnValue(of(created));

    householdSpy = jasmine.createSpyObj('HouseholdService', ['getMyHousehold', 'getMembers']);
    householdSpy.getMyHousehold.and.returnValue(of({ id: 10, name: 'WG' } as any));
    householdSpy.getMembers.and.returnValue(of([me, otherRoommate]));

    authSpy = jasmine.createSpyObj('AuthService', ['getUserEmail']);
    authSpy.getUserEmail.and.returnValue('me@test.com');

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemCreateComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, RouterTestingModule],
      providers: [
        { provide: ShoppingItemService, useValue: shoppingSpy },
        { provide: HouseholdService, useValue: householdSpy },
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemCreateComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filters the current user out of the roommates dropdown', () => {
    expect(component.roommates.length).toBe(1);
    expect(component.roommates[0].email).toBe('bob@test.com');
  });

  it('isTargeted() reflects the visibility control', () => {
    component.itemForm.patchValue({ visibility: ShoppingItemVisibility.TARGETED });
    fixture.detectChanges();
    expect(component.isTargeted()).toBeTrue();
    component.itemForm.patchValue({ visibility: ShoppingItemVisibility.SHARED });
    fixture.detectChanges();
    expect(component.isTargeted()).toBeFalse();
  });

  it('rejects submit when TARGETED but no target chosen', () => {
    component.itemForm.patchValue({
      name: 'X', visibility: ShoppingItemVisibility.TARGETED, targetUserEmail: ''
    });
    fixture.detectChanges();
    component.onSubmit();
    expect(shoppingSpy.create).not.toHaveBeenCalled();
  });

  it('sends targetUserEmail when TARGETED is chosen', () => {
    component.itemForm.patchValue({
      name: 'Milk', visibility: ShoppingItemVisibility.TARGETED,
      targetUserEmail: 'bob@test.com'
    });
    fixture.detectChanges();
    component.onSubmit();
    expect(shoppingSpy.create).toHaveBeenCalled();
    const dto = shoppingSpy.create.calls.mostRecent().args[0] as any;
    expect(dto.visibility).toBe(ShoppingItemVisibility.TARGETED);
    expect(dto.targetUserEmail).toBe('bob@test.com');
    expect(router.navigate).toHaveBeenCalledWith(['/shopping-items']);
  });

  it('omits targetUserEmail when not TARGETED', () => {
    component.itemForm.patchValue({
      name: 'Milk', visibility: ShoppingItemVisibility.SHARED,
      targetUserEmail: 'bob@test.com'
    });
    fixture.detectChanges();
    component.onSubmit();
    const dto = shoppingSpy.create.calls.mostRecent().args[0] as any;
    expect(dto.targetUserEmail).toBeUndefined();
  });

  it('surfaces 409 error message from backend', () => {
    shoppingSpy.create.and.returnValue(throwError(() => ({
      status: 409,
      error: { message: 'Cannot target a shopping request at yourself; use PERSONAL visibility instead.' }
    })));
    component.itemForm.patchValue({
      name: 'X', visibility: ShoppingItemVisibility.TARGETED, targetUserEmail: 'bob@test.com'
    });
    fixture.detectChanges();
    component.onSubmit();
    expect(component.error).toBeTrue();
    expect(component.errorMessage).toContain('Cannot target');
    expect(component.submitting).toBeFalse();
  });

  it('form should be invalid when name is blank', () => {
    component.itemForm.patchValue({ name: '', quantity: '', notes: '' });
    fixture.detectChanges();
    expect(component.itemForm.invalid).toBeTrue();
  });

  it('form should be valid with name only', () => {
    component.itemForm.patchValue({ name: 'Bread' });
    fixture.detectChanges();
    expect(component.itemForm.valid).toBeTrue();
  });

  it('onSubmit() should not call service when form is invalid', () => {
    component.itemForm.patchValue({ name: '' });
    fixture.detectChanges();
    component.onSubmit();
    expect(shoppingSpy.create).not.toHaveBeenCalled();
  });

  it('onSubmit() should surface validation errors from backend', () => {
    shoppingSpy.create.and.returnValue(throwError(() => ({
      status: 400, error: { 'Validation errors': ['name must not be blank'] }
    })));
    component.itemForm.patchValue({ name: 'X' });
    fixture.detectChanges();
    component.onSubmit();
    expect(component.error).toBeTrue();
    expect(component.errorMessage).toContain('must not be blank');
    expect(component.submitting).toBeFalse();
  });

  it('onSubmit() should ignore repeated calls while submitting', () => {
    let count = 0;
    shoppingSpy.create.and.callFake(() => {
      count++;
      return of(created);
    });
    component.itemForm.patchValue({ name: 'X' });
    fixture.detectChanges();
    component.submitting = true;
    component.onSubmit();
    expect(count).toBe(0);
  });
});
