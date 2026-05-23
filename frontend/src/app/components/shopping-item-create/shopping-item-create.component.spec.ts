import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { ShoppingItemCreateComponent } from './shopping-item-create.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { ShoppingItemDto, ShoppingItemStatus } from '../../dtos/shopping-item';

describe('ShoppingItemCreateComponent', () => {
  let component: ShoppingItemCreateComponent;
  let fixture: ComponentFixture<ShoppingItemCreateComponent>;
  let serviceSpy: jasmine.SpyObj<ShoppingItemService>;
  let router: Router;

  const created: ShoppingItemDto = {
    id: 1, name: 'Bread', quantity: undefined, notes: undefined,
    status: ShoppingItemStatus.NEEDED, householdId: 10, createdById: 100,
    createdByEmail: 'a@test.com', createdByFirstName: 'A', createdByLastName: 'B',
    createdAt: '2026-05-17T10:00:00', updatedAt: '2026-05-17T10:00:00'
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('ShoppingItemService', ['create']);
    serviceSpy.create.and.returnValue(of(created));

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemCreateComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, RouterTestingModule],
      providers: [
        { provide: ShoppingItemService, useValue: serviceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemCreateComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('form should be invalid when name is blank', () => {
    component.itemForm.patchValue({ name: '', quantity: '', notes: '' });
    expect(component.itemForm.invalid).toBeTrue();
  });

  it('form should be valid with name only', () => {
    component.itemForm.patchValue({ name: 'Bread' });
    expect(component.itemForm.valid).toBeTrue();
  });

  it('onSubmit() should not call service when form is invalid', () => {
    component.itemForm.patchValue({ name: '' });
    component.onSubmit();
    expect(serviceSpy.create).not.toHaveBeenCalled();
  });

  it('onSubmit() should call service.create and navigate on success', () => {
    component.itemForm.patchValue({ name: 'Bread', quantity: '1 loaf', notes: 'sourdough' });
    component.onSubmit();
    expect(serviceSpy.create).toHaveBeenCalledWith({
      name: 'Bread', quantity: '1 loaf', notes: 'sourdough'
    });
    expect(router.navigate).toHaveBeenCalledWith(['/shopping-items']);
    expect(component.submitting).toBeFalse();
  });

  it('onSubmit() should surface validation errors from backend', () => {
    serviceSpy.create.and.returnValue(throwError(() => ({
      status: 400, error: { 'Validation errors': ['name must not be blank'] }
    })));
    component.itemForm.patchValue({ name: 'X' });
    component.onSubmit();
    expect(component.error).toBeTrue();
    expect(component.errorMessage).toContain('must not be blank');
    expect(component.submitting).toBeFalse();
  });

  it('onSubmit() should ignore repeated calls while submitting', () => {
    // Simulate slow request — service returns a stream we never complete here
    let count = 0;
    serviceSpy.create.and.callFake(() => {
      count++;
      return of(created);
    });
    component.itemForm.patchValue({ name: 'X' });
    component.submitting = true;
    component.onSubmit();
    expect(count).toBe(0);
  });
});
