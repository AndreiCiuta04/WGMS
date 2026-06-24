import { Component, Input } from '@angular/core';
import { StatisticsCategoryValueDto, StatisticsMemberValueDto } from '../../../dtos/history';

type BarItem = StatisticsCategoryValueDto | StatisticsMemberValueDto;

@Component({
  selector: 'app-statistics-bar-list',
  templateUrl: './statistics-bar-list.component.html',
  styleUrls: ['./statistics-bar-list.component.scss'],
  standalone: false
})
export class StatisticsBarListComponent {

  @Input() title = '';
  @Input() items: BarItem[] = [];
  @Input() emptyText = 'No ranking data available.';
  @Input() valueSuffix = '';

  get visibleItems(): BarItem[] {
    return (this.items ?? []).filter(item => this.valueOf(item) > 0);
  }

  get maxValue(): number {
    return Math.max(1, ...this.visibleItems.map(item => this.valueOf(item)));
  }

  labelOf(item: BarItem): string {
    return 'displayName' in item ? item.displayName : item.label;
  }

  valueOf(item: BarItem): number {
    const parsed = Number(item.value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  widthOf(item: BarItem): number {
    return Math.max(4, Math.round((this.valueOf(item) / this.maxValue) * 100));
  }
}
