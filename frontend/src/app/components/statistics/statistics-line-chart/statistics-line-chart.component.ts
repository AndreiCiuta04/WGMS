import { Component, Input } from '@angular/core';
import { StatisticsSeriesDto } from '../../../dtos/history';

interface SvgPoint {
  x: number;
  y: number;
  value: number;
  label: string;
}

interface SvgSeries {
  key: string;
  label: string;
  points: SvgPoint[];
  path: string;
}

@Component({
  selector: 'app-statistics-line-chart',
  templateUrl: './statistics-line-chart.component.html',
  styleUrls: ['./statistics-line-chart.component.scss'],
  standalone: false
})
export class StatisticsLineChartComponent {

  @Input() title = '';
  @Input() emptyText = 'No chart data available for the selected period.';
  @Input() series: StatisticsSeriesDto[] = [];

  readonly width = 640;
  readonly height = 220;
  readonly paddingLeft = 42;
  readonly paddingRight = 20;
  readonly paddingTop = 16;
  readonly paddingBottom = 34;

  get hasData(): boolean {
    return this.series?.some(s => s.points?.some(p => Number(p.value) > 0)) ?? false;
  }

  get maxValue(): number {
    const values = this.series.flatMap(s => s.points ?? []).map(point => Number(point.value) || 0);
    return Math.max(1, ...values);
  }

  get renderedSeries(): SvgSeries[] {
    const maxPoints = Math.max(1, ...this.series.map(s => s.points?.length ?? 0));

    return this.series.map(s => {
      const points = (s.points ?? []).map((point, index) => this.toSvgPoint(point.periodStart, Number(point.value) || 0, index, maxPoints));
      return {
        key: s.key,
        label: s.label,
        points,
        path: points.map(point => `${point.x},${point.y}`).join(' ')
      };
    });
  }

  get yAxisLabels(): number[] {
    const max = this.maxValue;
    return [max, Math.round(max / 2), 0];
  }

  xAxisLabel(series: StatisticsSeriesDto, index: number): string {
    const points = series.points ?? [];
    if (points.length === 0 || index < 0 || index >= points.length) {
      return '';
    }
    return this.formatDate(points[index].periodStart);
  }

  yForValue(value: number): number {
    const plotHeight = this.height - this.paddingTop - this.paddingBottom;
    return this.paddingTop + plotHeight - ((Number(value) || 0) / this.maxValue) * plotHeight;
  }

  private toSvgPoint(label: string, value: number, index: number, count: number): SvgPoint {
    const plotWidth = this.width - this.paddingLeft - this.paddingRight;
    const denominator = Math.max(1, count - 1);
    return {
      x: this.paddingLeft + (plotWidth * index / denominator),
      y: this.yForValue(value),
      value,
      label: this.formatDate(label)
    };
  }

  private formatDate(value: string): string {
    if (!value) {
      return '';
    }
    const date = new Date(`${value}T00:00:00`);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  }
}
