import { Component, OnInit } from '@angular/core';
import { finalize, forkJoin } from 'rxjs';
import {
  ExpenseStatisticsDto,
  ShoppingStatisticsDto,
  StatisticsFilter,
  StatisticsGrouping,
  StatisticsModule,
  StatisticsOverviewDto,
  TaskStatisticsDto
} from '../../dtos/history';
import { HistoryService } from '../../services/history.service';

type StatisticsSection = 'overview' | 'tasks' | 'expenses' | 'shopping';
type PresetPeriod = 'LAST_7_DAYS' | 'LAST_30_DAYS' | 'LAST_3_MONTHS' | 'LAST_6_MONTHS' | 'LAST_12_MONTHS' | 'CUSTOM';

interface PresetOption {
  key: PresetPeriod;
  label: string;
  days?: number;
  months?: number;
  grouping: StatisticsGrouping;
}

interface ModuleOption {
  key: StatisticsModule;
  label: string;
}

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss'],
  standalone: false
})
export class StatisticsComponent implements OnInit {

  loading = false;
  errorMessage = '';
  activeStatisticsSection: StatisticsSection = 'overview';

  overview?: StatisticsOverviewDto;
  taskStatistics?: TaskStatisticsDto;
  expenseStatistics?: ExpenseStatisticsDto;
  shoppingStatistics?: ShoppingStatisticsDto;

  presetOptions: PresetOption[] = [
    { key: 'LAST_7_DAYS', label: 'Last 7 days', days: 7, grouping: 'DAY' },
    { key: 'LAST_30_DAYS', label: 'Last 30 days', days: 30, grouping: 'DAY' },
    { key: 'LAST_3_MONTHS', label: 'Last 3 months', months: 3, grouping: 'WEEK' },
    { key: 'LAST_6_MONTHS', label: 'Last 6 months', months: 6, grouping: 'WEEK' },
    { key: 'LAST_12_MONTHS', label: 'Last 12 months', months: 12, grouping: 'MONTH' },
    { key: 'CUSTOM', label: 'Custom', grouping: 'DAY' }
  ];

  groupingOptions: StatisticsGrouping[] = ['DAY', 'WEEK', 'MONTH'];

  moduleOptions: ModuleOption[] = [
    { key: 'TASKS', label: 'Tasks' },
    { key: 'CLEANING', label: 'Cleaning' },
    { key: 'EXPENSES', label: 'Expenses' },
    { key: 'PAYMENTS', label: 'Payments' },
    { key: 'SHOPPING', label: 'Shopping' }
  ];

  selectedPreset: PresetPeriod = 'LAST_30_DAYS';
  selectedModules = new Set<StatisticsModule>(this.moduleOptions.map(option => option.key));
  statisticsFilter: StatisticsFilter = this.filterFromPreset(this.selectedPreset);

  constructor(private historyService: HistoryService) { }

  ngOnInit(): void {
    this.loadStatistics();
  }

  setStatisticsSection(section: StatisticsSection): void {
    this.activeStatisticsSection = section;
  }

  applyPreset(preset: PresetPeriod): void {
    this.selectedPreset = preset;
    const previousGrouping = this.statisticsFilter.grouping;
    this.statisticsFilter = this.filterFromPreset(preset);

    if (preset === 'CUSTOM') {
      this.statisticsFilter.grouping = previousGrouping;
    }
  }

  applyStatisticsFilters(): void {
    this.errorMessage = '';
    this.statisticsFilter.modules = Array.from(this.selectedModules);

    if (!this.statisticsFilter.from || !this.statisticsFilter.to) {
      this.errorMessage = 'Please select a valid start and end date.';
      return;
    }
    if (this.statisticsFilter.from > this.statisticsFilter.to) {
      this.errorMessage = 'The start date must be before or equal to the end date.';
      return;
    }

    this.loadStatistics();
  }

  resetStatisticsFilters(): void {
    this.selectedModules = new Set<StatisticsModule>(this.moduleOptions.map(option => option.key));
    this.selectedPreset = 'LAST_30_DAYS';
    this.statisticsFilter = this.filterFromPreset(this.selectedPreset);
    this.loadStatistics();
  }

  toggleModule(module: StatisticsModule, checked: boolean): void {
    if (checked) {
      this.selectedModules.add(module);
    } else {
      this.selectedModules.delete(module);
    }
  }

  moduleSelected(module: StatisticsModule): boolean {
    return this.selectedModules.has(module);
  }

  loadStatistics(): void {
    this.loading = true;
    this.errorMessage = '';
    const filter = {
      ...this.statisticsFilter,
      modules: Array.from(this.selectedModules)
    };

    forkJoin({
      overview: this.historyService.getStatisticsOverview(filter),
      tasks: this.historyService.getTaskStatistics(filter),
      expenses: this.historyService.getExpenseStatistics(filter),
      shopping: this.historyService.getShoppingStatistics(filter)
    })
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: result => {
          this.overview = result.overview;
          this.taskStatistics = result.tasks;
          this.expenseStatistics = result.expenses;
          this.shoppingStatistics = result.shopping;
        },
        error: err => {
          console.error('Error fetching statistics', err);
          this.overview = undefined;
          this.taskStatistics = undefined;
          this.expenseStatistics = undefined;
          this.shoppingStatistics = undefined;
          this.errorMessage = this.extractErrorMessage(err, 'Could not load statistics. Please adjust the filters and try again.');
        }
      });
  }

  formatPercent(value: number | null | undefined): string {
    return `${this.numeric(value).toFixed(1)}%`;
  }

  formatNumber(value: number | null | undefined): string {
    return this.numeric(value).toLocaleString(undefined, { maximumFractionDigits: 0 });
  }

  formatMoney(value: number | null | undefined): string {
    return this.numeric(value).toLocaleString(undefined, { style: 'currency', currency: 'EUR' });
  }

  groupingLabel(grouping: StatisticsGrouping): string {
    switch (grouping) {
      case 'DAY': return 'Daily';
      case 'WEEK': return 'Weekly';
      case 'MONTH': return 'Monthly';
      default: return grouping;
    }
  }

  private filterFromPreset(preset: PresetPeriod): StatisticsFilter {
    const option = this.presetOptions.find(entry => entry.key === preset) ?? this.presetOptions[1];
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const from = new Date(today);

    if (option.months) {
      from.setMonth(from.getMonth() - option.months);
    } else {
      from.setDate(from.getDate() - ((option.days ?? 30) - 1));
    }

    return {
      from: this.toDateInput(from),
      to: this.toDateInput(today),
      grouping: option.grouping,
      modules: Array.from(this.selectedModules)
    };
  }

  private toDateInput(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private numeric(value: number | string | null | undefined): number {
    const parsed = Number(value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  private extractErrorMessage(err: any, fallback: string): string {
    if (typeof err?.error === 'string' && err.error.trim()) {
      return err.error;
    }
    if (err?.error?.message) {
      return err.error.message;
    }
    return fallback;
  }
}
