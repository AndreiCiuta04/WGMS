import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, Subject, throwError } from 'rxjs';

import { StatisticsComponent } from './statistics.component';
import { HistoryService } from '../../services/history.service';
import {
  ExpenseStatisticsDto,
  ShoppingStatisticsDto,
  StatisticsOverviewDto,
  TaskStatisticsDto
} from '../../dtos/history';

describe('StatisticsComponent', () => {
  let component: StatisticsComponent;
  let fixture: ComponentFixture<StatisticsComponent>;
  let serviceSpy: jasmine.SpyObj<HistoryService>;

  const overview: StatisticsOverviewDto = {
    from: '2026-06-01',
    to: '2026-06-30',
    grouping: 'DAY',
    completedTaskCount: 3,
    taskCompletionRate: 75,
    totalExpenseAmount: 120,
    recordedPaymentAmount: 35,
    purchasedShoppingItemCount: 4,
    activeCleaningScheduleCount: 2,
    totalActivityCount: 5,
    activityByModule: [
      { key: 'TASKS', label: 'Tasks', value: 3 },
      { key: 'EXPENSES', label: 'Expenses', value: 2 }
    ],
    activityTrend: [
      {
        key: 'ACTIVITY',
        label: 'Activity',
        points: [
          { periodStart: '2026-06-01', value: 2 },
          { periodStart: '2026-06-02', value: 1 }
        ]
      }
    ]
  };

  const tasks: TaskStatisticsDto = {
    from: '2026-06-01',
    to: '2026-06-30',
    grouping: 'DAY',
    totalTaskCount: 5,
    completedTaskCount: 3,
    cancelledTaskCount: 1,
    overdueTaskCount: 1,
    completionRate: 60,
    totalCleaningScheduleCount: 2,
    activeCleaningScheduleCount: 1,
    scheduleGeneratedTaskCount: 2,
    scheduleGeneratedTaskCompletionRate: 50,
    taskStatusDistribution: [
      { key: 'DONE', label: 'Done', value: 3 }
    ],
    cleaningFrequencyDistribution: [
      { key: 'WEEKLY', label: 'Weekly', value: 1 }
    ],
    completionTrend: [
      {
        key: 'COMPLETED',
        label: 'Completed tasks',
        points: [
          { periodStart: '2026-06-01', value: 1 }
        ]
      }
    ],
    completedTasksByMember: [
      { roommateId: 1, displayName: 'Main Tenant', value: 2 }
    ],
    completedTasksByWeekday: [
      { key: 'MONDAY', label: 'Monday', value: 1 }
    ]
  };

  const expenses: ExpenseStatisticsDto = {
    from: '2026-06-01',
    to: '2026-06-30',
    grouping: 'DAY',
    expenseCount: 2,
    totalExpenseAmount: 120,
    averageExpenseAmount: 60,
    largestExpenseAmount: 80,
    paymentCount: 1,
    totalPaymentAmount: 35,
    expenseTrend: [
      {
        key: 'EXPENSES',
        label: 'Expenses',
        points: [
          { periodStart: '2026-06-01', value: 80 }
        ]
      }
    ],
    paymentTrend: [
      {
        key: 'PAYMENTS',
        label: 'Payments',
        points: [
          { periodStart: '2026-06-02', value: 35 }
        ]
      }
    ],
    expensesPaidByMember: [
      { roommateId: 1, displayName: 'Main Tenant', value: 120 }
    ],
    paymentsSentByMember: [
      { roommateId: 2, displayName: 'Roommate', value: 35 }
    ],
    paymentsReceivedByMember: [
      { roommateId: 1, displayName: 'Main Tenant', value: 35 }
    ]
  };

  const shopping: ShoppingStatisticsDto = {
    from: '2026-06-01',
    to: '2026-06-30',
    grouping: 'DAY',
    addedItemCount: 3,
    purchasedItemCount: 2,
    purchaseCompletionRate: 66.7,
    statusDistribution: [
      { key: 'BOUGHT', label: 'Bought', value: 2 }
    ],
    visibilityDistribution: [
      { key: 'SHARED', label: 'Shared', value: 2 }
    ],
    itemActivityTrend: [
      {
        key: 'ADDED',
        label: 'Items added',
        points: [
          { periodStart: '2026-06-01', value: 2 }
        ]
      }
    ],
    mostFrequentlyPurchasedItems: [
      { key: 'milk', label: 'Milk', value: 2 }
    ],
    itemsAddedByMember: [
      { roommateId: 1, displayName: 'Main Tenant', value: 2 }
    ]
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('HistoryService', [
      'getStatisticsOverview',
      'getTaskStatistics',
      'getExpenseStatistics',
      'getShoppingStatistics'
    ]);

    serviceSpy.getStatisticsOverview.and.returnValue(of(overview));
    serviceSpy.getTaskStatistics.and.returnValue(of(tasks));
    serviceSpy.getExpenseStatistics.and.returnValue(of(expenses));
    serviceSpy.getShoppingStatistics.and.returnValue(of(shopping));

    await TestBed.configureTestingModule({
      declarations: [StatisticsComponent],
      providers: [{ provide: HistoryService, useValue: serviceSpy }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(StatisticsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads all statistics on init', () => {
    fixture.detectChanges();

    expect(serviceSpy.getStatisticsOverview).toHaveBeenCalled();
    expect(serviceSpy.getTaskStatistics).toHaveBeenCalled();
    expect(serviceSpy.getExpenseStatistics).toHaveBeenCalled();
    expect(serviceSpy.getShoppingStatistics).toHaveBeenCalled();
    expect(component.overview).toEqual(overview);
    expect(component.taskStatistics).toEqual(tasks);
    expect(component.expenseStatistics).toEqual(expenses);
    expect(component.shoppingStatistics).toEqual(shopping);
    expect(component.loading).toBeFalse();
  });

  it('shows loading state while statistics are being fetched', () => {
    const pendingOverview = new Subject<StatisticsOverviewDto>();
    serviceSpy.getStatisticsOverview.and.returnValue(pendingOverview.asObservable());

    fixture.detectChanges();

    expect(component.loading).toBeTrue();
  });

  it('clears statistics and displays an error message when loading fails', () => {
    serviceSpy.getStatisticsOverview.and.returnValue(throwError(() => ({
      status: 500,
      error: { message: 'Statistics unavailable' }
    })));

    fixture.detectChanges();

    expect(component.overview).toBeUndefined();
    expect(component.taskStatistics).toBeUndefined();
    expect(component.expenseStatistics).toBeUndefined();
    expect(component.shoppingStatistics).toBeUndefined();
    expect(component.errorMessage).toBe('Statistics unavailable');
    expect(component.loading).toBeFalse();
  });

  it('updates the active statistics section', () => {
    component.setStatisticsSection('expenses');

    expect(component.activeStatisticsSection).toBe('expenses');
  });

  it('applies preset date range and default grouping', () => {
    component.applyPreset('LAST_12_MONTHS');

    expect(component.selectedPreset).toBe('LAST_12_MONTHS');
    expect(component.statisticsFilter.grouping).toBe('MONTH');
    expect(component.statisticsFilter.from).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(component.statisticsFilter.to).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('keeps the current grouping when switching to custom preset', () => {
    component.statisticsFilter.grouping = 'WEEK';

    component.applyPreset('CUSTOM');

    expect(component.selectedPreset).toBe('CUSTOM');
    expect(component.statisticsFilter.grouping).toBe('WEEK');
  });

  it('does not load statistics for an invalid custom date range', () => {
    fixture.detectChanges();
    serviceSpy.getStatisticsOverview.calls.reset();

    component.statisticsFilter.from = '2026-06-30';
    component.statisticsFilter.to = '2026-06-01';

    component.applyStatisticsFilters();

    expect(component.errorMessage).toBe('The start date must be before or equal to the end date.');
    expect(serviceSpy.getStatisticsOverview).not.toHaveBeenCalled();
  });

  it('passes selected modules when applying filters', () => {
    component.toggleModule('SHOPPING', false);

    component.applyStatisticsFilters();

    const filter = serviceSpy.getStatisticsOverview.calls.mostRecent().args[0];
    expect(filter.modules).not.toContain('SHOPPING');
    expect(filter.modules).toContain('TASKS');
  });

  it('resets filters to the default period and all modules', () => {
    component.toggleModule('TASKS', false);
    component.statisticsFilter.grouping = 'MONTH';

    component.resetStatisticsFilters();

    expect(component.selectedPreset).toBe('LAST_30_DAYS');
    expect(component.statisticsFilter.grouping).toBe('DAY');
    expect(component.moduleSelected('TASKS')).toBeTrue();
    expect(component.moduleSelected('SHOPPING')).toBeTrue();
  });

  it('formats numbers, percentages, and money defensively', () => {
    expect(component.formatPercent(12.345)).toBe('12.3%');
    expect(component.formatNumber(1234.8)).toContain('1');
    expect(component.formatMoney(12)).toContain('€');
    expect(component.formatPercent(undefined)).toBe('0.0%');
  });

  it('returns readable grouping labels', () => {
    expect(component.groupingLabel('DAY')).toBe('Daily');
    expect(component.groupingLabel('WEEK')).toBe('Weekly');
    expect(component.groupingLabel('MONTH')).toBe('Monthly');
  });
});