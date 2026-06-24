import { Component, Input } from '@angular/core';
import { StatisticsCategoryValueDto } from '../../../dtos/history';

interface DonutSlice {
  label: string;
  value: number;
  percentage: number;
  dashArray: string;
  dashOffset: number;
}

@Component({
  selector: 'app-statistics-donut-chart',
  templateUrl: './statistics-donut-chart.component.html',
  styleUrls: ['./statistics-donut-chart.component.scss'],
  standalone: false
})
export class StatisticsDonutChartComponent {

  @Input() title = '';
  @Input() items: StatisticsCategoryValueDto[] = [];
  @Input() emptyText = 'No distribution data available.';

  readonly radius = 42;
  readonly circumference = 2 * Math.PI * this.radius;

  get total(): number {
    return this.visibleItems.reduce((sum, item) => sum + this.numericValue(item.value), 0);
  }

  get hasData(): boolean {
    return this.total > 0;
  }

  get visibleItems(): StatisticsCategoryValueDto[] {
    return (this.items ?? []).filter(item => this.numericValue(item.value) > 0);
  }

  get slices(): DonutSlice[] {
    let offset = 0;
    return this.visibleItems.map(item => {
      const value = this.numericValue(item.value);
      const percentage = this.total === 0 ? 0 : value / this.total;
      const dash = percentage * this.circumference;
      const slice = {
        label: item.label,
        value,
        percentage: Math.round(percentage * 100),
        dashArray: `${dash} ${this.circumference - dash}`,
        dashOffset: -offset
      };
      offset += dash;
      return slice;
    });
  }

  private numericValue(value: number | string | null | undefined): number {
    const parsed = Number(value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  }
}
