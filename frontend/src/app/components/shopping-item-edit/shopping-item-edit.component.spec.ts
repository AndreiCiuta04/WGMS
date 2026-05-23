import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShoppingItemEditComponent } from './shopping-item-edit.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { ShoppingItemDto, ShoppingItemStatus } from '../../dtos/shopping-item';

describe('ShoppingItemEditComponent', () => {
  let component: ShoppingItemEditComponent;
  let fixture: ComponentFixture<ShoppingItemEditComponent>;
  let serviceSpy: jasmine.SpyObj<ShoppingItemService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const existing: ShoppingItemDto = {
    id: 42, name: 'Milk', quantity: '2L', notes: 'whole',
    status: ShoppingItemStatus.NEEDED, householdId: 10, createdById: 100,
    createdByEmail: 'a@test.com', createdByFirstName: 'A', createdByLastName: 'B',
    createdAt: '2026-05-17T10:00:00', updatedAt: '2026-05-17T10:00:00'
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('ShoppingItemService', ['findOne', 'update']);
    serviceSpy.findOne.and.returnValue(of(existing));
    serviceSpy.update.and.returnValue(of(existing));
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemEditComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, RouterModule.forRoot([])],
      providers: [
        { provide: ShoppingItemService, useValue: serviceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '42' }) } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemEditComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit() should load the item and patch the form', () => {
    fixture.detectChanges();
    expect(serviceSpy.findOne).toHaveBeenCalledWith(42);
    expect(component.itemForm.value.name).toBe('Milk');
    expect(component.itemForm.value.quantity).toBe('2L');
    expect(component.itemForm.value.notes).toBe('whole');
    expect(component.loading).toBeFalse();
  });

  it('onSubmit() should call update and navigate', () => {
    fixture.detectChanges();
    component.itemForm.patchValue({ name: 'Milk', quantity: '1L', notes: '' });
    component.onSubmit();
    expect(serviceSpy.update).toHaveBeenCalledWith(42,
      { name: 'Milk', quantity: '1L', notes: '' });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/shopping-items']);
  });

  it('should show error when loading the item fails', () => {
    serviceSpy.findOne.and.returnValue(throwError(() => ({ status: 404 })));
    fixture.detectChanges();
    expect(component.error).toBeTrue();
    expect(component.errorMessage).toContain('Failed to load');
  });

  it('onSubmit() should ignore repeated calls while submitting', () => {
    fixture.detectChanges();
    component.submitting = true;
    component.onSubmit();
    expect(serviceSpy.update).not.toHaveBeenCalled();
  });
});
