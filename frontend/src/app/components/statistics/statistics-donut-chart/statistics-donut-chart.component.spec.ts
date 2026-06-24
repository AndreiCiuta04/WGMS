import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatisticsDonutChartComponent } from './statistics-donut-chart.component';

describe('StatisticsDonutChartComponent', () => {
  let component: StatisticsDonutChartComponent;
  let fixture: ComponentFixture<StatisticsDonutChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StatisticsDonutChartComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(StatisticsDonutChartComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filters zero-value items from visible items', () => {
    component.items = [
      { key: 'DONE', label: 'Done', value: 2 },
      { key: 'OPEN', label: 'Open', value: 0 }
    ];

    expect(component.visibleItems.length).toBe(1);
    expect(component.visibleItems[0].label).toBe('Done');
    expect(component.total).toBe(2);
    expect(component.hasData).toBeTrue();
  });

  it('builds donut slices with percentages', () => {
    component.items = [
      { key: 'DONE', label: 'Done', value: 3 },
      { key: 'OPEN', label: 'Open', value: 1 }
    ];

    expect(component.slices.length).toBe(2);
    expect(component.slices[0].percentage).toBe(75);
    expect(component.slices[0].dashArray).toContain(' ');
  });

  it('reports empty data when there are no positive values', () => {
    component.items = [{ key: 'DONE', label: 'Done', value: 0 }];

    expect(component.total).toBe(0);
    expect(component.hasData).toBeFalse();
    expect(component.slices.length).toBe(0);
  });
});