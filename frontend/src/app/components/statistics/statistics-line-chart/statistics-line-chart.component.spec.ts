import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatisticsLineChartComponent } from './statistics-line-chart.component';

describe('StatisticsLineChartComponent', () => {
  let component: StatisticsLineChartComponent;
  let fixture: ComponentFixture<StatisticsLineChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StatisticsLineChartComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(StatisticsLineChartComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('reports empty data when all series values are zero', () => {
    component.series = [
      {
        key: 'TASKS',
        label: 'Tasks',
        points: [
          { periodStart: '2026-06-01', value: 0 }
        ]
      }
    ];

    expect(component.hasData).toBeFalse();
    expect(component.maxValue).toBe(1);
  });

  it('builds SVG series points and paths for non-empty data', () => {
    component.series = [
      {
        key: 'TASKS',
        label: 'Tasks',
        points: [
          { periodStart: '2026-06-01', value: 2 },
          { periodStart: '2026-06-02', value: 4 }
        ]
      }
    ];

    expect(component.hasData).toBeTrue();
    expect(component.maxValue).toBe(4);
    expect(component.renderedSeries.length).toBe(1);
    expect(component.renderedSeries[0].points.length).toBe(2);
    expect(component.renderedSeries[0].path).toContain(',');
  });

  it('returns blank x-axis labels for missing points', () => {
    component.series = [{ key: 'EMPTY', label: 'Empty', points: [] }];

    expect(component.xAxisLabel(component.series[0], 0)).toBe('');
  });
});
