import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { HouseholdCreateComponent } from './household-create.component';
import { HouseholdService } from '../../services/household.service';
import { Household } from '../../dtos/household';

describe('HouseholdCreateComponent', () => {
  let component: HouseholdCreateComponent;
  let fixture: ComponentFixture<HouseholdCreateComponent>;
  let householdService: jasmine.SpyObj<HouseholdService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['createHousehold']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [HouseholdCreateComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: HouseholdService, useValue: householdService },
        { provide: Router, useValue: router }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(HouseholdCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should block service call for empty household name', () => {
    component.form.patchValue({ name: '', address: 'Main street 1' });

    component.submit();

    expect(component.form.get('name')?.errors?.required).toBeTruthy();
    expect(component.form.get('name')?.touched).toBeTrue();
    expect(householdService.createHousehold).not.toHaveBeenCalled();
  });

  it('should call HouseholdService with name and address for a valid form', () => {
    householdService.createHousehold.and.returnValue(of(household()));
    component.form.patchValue({ name: 'Dream Loft Vienna', address: 'Main street 1' });

    component.submit();

    expect(householdService.createHousehold).toHaveBeenCalledOnceWith({
      name: 'Dream Loft Vienna',
      address: 'Main street 1'
    });
  });

  it('should navigate home after successful household creation', () => {
    householdService.createHousehold.and.returnValue(of(household()));
    component.form.patchValue({ name: 'Dream Loft Vienna', address: 'Main street 1' });

    component.submit();

    expect(router.navigate).toHaveBeenCalledOnceWith(['/']);
  });

  it('should set readable error message when backend returns conflict message', () => {
    householdService.createHousehold.and.returnValue(
      throwError(() => ({ status: 409, error: { message: 'Household already exists.' } }))
    );
    component.form.patchValue({ name: 'Dream Loft Vienna', address: 'Main street 1' });

    component.submit();
    fixture.detectChanges();

    expect(component.errorMessage).toBe('Household already exists.');
    expect(fixture.nativeElement.textContent).toContain('Household already exists.');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should set fallback error message when backend error has no message', () => {
    householdService.createHousehold.and.returnValue(throwError(() => ({ status: 500, error: {} })));
    component.form.patchValue({ name: 'Dream Loft Vienna', address: '' });

    component.submit();

    expect(component.errorMessage).toBe('An error occurred while creating the household.');
  });
});

function household(overrides: Partial<Household> = {}): Household {
  return {
    id: 1,
    name: 'Dream Loft Vienna',
    address: 'Main street 1',
    createdAt: '2026-06-01T00:00:00',
    currentUserRole: 'MAIN_TENANT',
    ...overrides
  };
}
