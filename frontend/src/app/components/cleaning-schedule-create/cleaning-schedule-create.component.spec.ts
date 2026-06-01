import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CleaningScheduleCreateComponent } from './cleaning-schedule-create.component';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { HouseholdService } from '../../services/household.service';
import { CleaningFrequency, CleaningScheduleDetailDto } from '../../dtos/cleaning-schedule';
import { Household } from '../../dtos/household';
import { Roommate } from '../../dtos/roommate';

describe('CleaningScheduleCreateComponent', () => {
  let component: CleaningScheduleCreateComponent;
  let fixture: ComponentFixture<CleaningScheduleCreateComponent>;
  let cleaningScheduleService: jasmine.SpyObj<CleaningScheduleService>;
  let householdService: jasmine.SpyObj<HouseholdService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    cleaningScheduleService = jasmine.createSpyObj<CleaningScheduleService>('CleaningScheduleService', ['create']);
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['getMyHousehold', 'getMembers']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    householdService.getMyHousehold.and.returnValue(of(household()));
    householdService.getMembers.and.returnValue(of([roommate()]));
    cleaningScheduleService.create.and.returnValue(of(schedule()));

    await TestBed.configureTestingModule({
      declarations: [CleaningScheduleCreateComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: CleaningScheduleService, useValue: cleaningScheduleService },
        { provide: HouseholdService, useValue: householdService },
        { provide: Router, useValue: router }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CleaningScheduleCreateComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load household members as assignee options on init', () => {
    fixture.detectChanges();

    expect(householdService.getMyHousehold).toHaveBeenCalled();
    expect(householdService.getMembers).toHaveBeenCalledOnceWith(1);
    expect(component.roommates).toEqual([roommate()]);
  });

  it('should block service call when title is empty', () => {
    fixture.detectChanges();
    component.scheduleForm.patchValue(validScheduleForm({ title: '' }));

    component.onSubmit();

    expect(component.submitted).toBeTrue();
    expect(component.scheduleForm.get('title')?.errors?.required).toBeTruthy();
    expect(cleaningScheduleService.create).not.toHaveBeenCalled();
  });

  it('should require frequency, start date, and assignee', () => {
    fixture.detectChanges();
    component.scheduleForm.patchValue(validScheduleForm({
      frequency: '',
      startDate: '',
      assignedToEmail: ''
    }));

    component.onSubmit();

    expect(component.scheduleForm.get('frequency')?.errors?.required).toBeTruthy();
    expect(component.scheduleForm.get('startDate')?.errors?.required).toBeTruthy();
    expect(component.scheduleForm.get('assignedToEmail')?.errors?.required).toBeTruthy();
    expect(cleaningScheduleService.create).not.toHaveBeenCalled();
  });

  it('should not block end date before start date because the component has no date-order validator', () => {
    fixture.detectChanges();
    component.scheduleForm.patchValue(validScheduleForm({
      startDate: '2026-06-10',
      endDate: '2026-06-01'
    }));

    component.onSubmit();

    expect(component.scheduleForm.valid).toBeTrue();
    expect(cleaningScheduleService.create).toHaveBeenCalled();
  });

  it('should call CleaningScheduleService with form values for a valid form', () => {
    fixture.detectChanges();
    component.scheduleForm.patchValue(validScheduleForm());

    component.onSubmit();

    expect(cleaningScheduleService.create).toHaveBeenCalledOnceWith({
      title: 'Kitchen rotation',
      description: 'Clean kitchen surfaces',
      frequency: CleaningFrequency.WEEKLY,
      startDate: '2026-06-05',
      endDate: '2026-07-05',
      assignedToEmail: 'roommate@example.com'
    });
  });

  it('should omit empty optional description and end date in create payload', () => {
    fixture.detectChanges();
    component.scheduleForm.patchValue(validScheduleForm({ description: '', endDate: '' }));

    component.onSubmit();

    expect(cleaningScheduleService.create).toHaveBeenCalledOnceWith({
      title: 'Kitchen rotation',
      description: undefined,
      frequency: CleaningFrequency.WEEKLY,
      startDate: '2026-06-05',
      endDate: undefined,
      assignedToEmail: 'roommate@example.com'
    });
  });

  it('should navigate to cleaning schedule list after successful create', () => {
    fixture.detectChanges();
    component.scheduleForm.patchValue(validScheduleForm());

    component.onSubmit();

    expect(router.navigate).toHaveBeenCalledOnceWith(['/cleaning-schedules']);
  });

  it('should keep roommates empty when household cannot be loaded', () => {
    householdService.getMyHousehold.and.returnValue(throwError(() => ({ status: 403 })));

    fixture.detectChanges();

    expect(component.roommates).toEqual([]);
    expect(householdService.getMembers).not.toHaveBeenCalled();
  });

  it('should keep roommates empty when member loading fails', () => {
    householdService.getMembers.and.returnValue(throwError(() => ({ status: 500 })));

    fixture.detectChanges();

    expect(component.roommates).toEqual([]);
  });

  it('should show backend validation error when schedule creation fails', () => {
    fixture.detectChanges();
    cleaningScheduleService.create.and.returnValue(
      throwError(() => ({ error: { 'Validation errors': ['title must not be blank'] } }))
    );
    component.scheduleForm.patchValue(validScheduleForm());

    component.onSubmit();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('must not be blank');
  });
});

function validScheduleForm(overrides: Partial<{
  title: string;
  description: string;
  frequency: CleaningFrequency | '';
  startDate: string;
  endDate: string;
  assignedToEmail: string;
}> = {}) {
  return {
    title: 'Kitchen rotation',
    description: 'Clean kitchen surfaces',
    frequency: CleaningFrequency.WEEKLY,
    startDate: '2026-06-05',
    endDate: '2026-07-05',
    assignedToEmail: 'roommate@example.com',
    ...overrides
  };
}

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

function roommate(overrides: Partial<Roommate> = {}): Roommate {
  return {
    id: 1,
    userId: 1,
    firstName: 'Anna',
    lastName: 'Berger',
    email: 'roommate@example.com',
    role: 'ROOMMATE',
    joinedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

function schedule(overrides: Partial<CleaningScheduleDetailDto> = {}): CleaningScheduleDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Kitchen rotation',
    description: 'Clean kitchen surfaces',
    frequency: CleaningFrequency.WEEKLY,
    startDate: '2026-06-05',
    endDate: '2026-07-05',
    nextDueDate: '2026-06-05',
    active: true,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
