import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatisticsBarListComponent } from './statistics-bar-list.component';

describe('StatisticsBarListComponent', () => {
  let component: StatisticsBarListComponent;
  let fixture: ComponentFixture<StatisticsBarListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StatisticsBarListComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(StatisticsBarListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filters out zero-value items', () => {
    component.items = [
      { key: 'milk', label: 'Milk', value: 3 },
      { key: 'bread', label: 'Bread', value: 0 }
    ];

    expect(component.visibleItems.length).toBe(1);
    expect(component.labelOf(component.visibleItems[0])).toBe('Milk');
  });

  it('supports member-value labels', () => {
    const item = { roommateId: 1, displayName: 'Main Tenant', value: 4 };

    expect(component.labelOf(item)).toBe('Main Tenant');
    expect(component.valueOf(item)).toBe(4);
  });

  it('calculates relative bar widths with a minimum visible width', () => {
    component.items = [
      { key: 'a', label: 'A', value: 10 },
      { key: 'b', label: 'B', value: 1 }
    ];

    expect(component.maxValue).toBe(10);
    expect(component.widthOf(component.items[0])).toBe(100);
    expect(component.widthOf(component.items[1])).toBeGreaterThanOrEqual(4);
  });
});