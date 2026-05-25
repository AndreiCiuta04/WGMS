import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShoppingItemListComponent } from './shopping-item-list.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { AggregatedShoppingItemDto, ShoppingItemDto, ShoppingItemStatus, ShoppingItemVisibility } from '../../dtos/shopping-item';
import { LucideAngularModule, ShoppingCart, CircleCheck, CircleX, Pencil, Trash2, RotateCcw } from 'lucide-angular';

describe('ShoppingItemListComponent', () => {
  let component: ShoppingItemListComponent;
  let fixture: ComponentFixture<ShoppingItemListComponent>;
  let serviceSpy: jasmine.SpyObj<ShoppingItemService>;

  const personal: ShoppingItemDto = {
    id: 1, name: 'My personal', quantity: null, notes: null,
    status: ShoppingItemStatus.NEEDED, visibility: ShoppingItemVisibility.PERSONAL,
    householdId: 10, createdById: 100,
    createdByEmail: 'a@test.com', createdByFirstName: 'A', createdByLastName: 'B',
    targetUserId: null, targetUserEmail: null,
    targetUserFirstName: null, targetUserLastName: null,
    createdAt: '2026-05-17T10:00:00', updatedAt: '2026-05-17T10:00:00'
  };
  const targeted: ShoppingItemDto = {
    ...personal, id: 2, name: 'Milk for me',
    visibility: ShoppingItemVisibility.TARGETED,
    targetUserId: 200, targetUserEmail: 'b@test.com',
    targetUserFirstName: 'B', targetUserLastName: 'C'
  };
  const aggregated: AggregatedShoppingItemDto = {
    name: 'Milk',
    quantity: '2L',
    itemCount: 2,
    sourceItems: [
      {
        id: 10,
        name: 'Milk',
        quantity: '2L',
        notes: null,
        createdById: 100,
        createdByEmail: 'a@test.com',
        createdByFirstName: 'A',
        createdByLastName: 'B',
        createdAt: '2026-05-17T10:00:00'
      },
      {
        id: 11,
        name: ' milk ',
        quantity: ' 2L ',
        notes: null,
        createdById: 200,
        createdByEmail: 'b@test.com',
        createdByFirstName: 'B',
        createdByLastName: 'C',
        createdAt: '2026-05-17T10:05:00'
      }
    ]
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('ShoppingItemService',
      ['findAll', 'findTargetedAtMe', 'findAggregatedHouseholdShoppingList', 'updateStatus', 'delete']);
    serviceSpy.findAll.and.returnValue(of([personal, targeted]));
    serviceSpy.findTargetedAtMe.and.returnValue(of([targeted]));
    serviceSpy.findAggregatedHouseholdShoppingList.and.returnValue(of([aggregated]));
    serviceSpy.updateStatus.and.returnValue(of({ ...personal, status: ShoppingItemStatus.BOUGHT }));
    serviceSpy.delete.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemListComponent],
      imports: [
        HttpClientTestingModule,
        RouterModule.forRoot([]),
        LucideAngularModule.pick({ShoppingCart, CircleCheck, CircleX, Pencil, Trash2, RotateCcw})
      ],
      providers: [{ provide: ShoppingItemService, useValue: serviceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch all-items, targeted-at-me, and aggregated household list on init', () => {
    fixture.detectChanges();
    expect(serviceSpy.findAll).toHaveBeenCalled();
    expect(serviceSpy.findTargetedAtMe).toHaveBeenCalled();
    expect(serviceSpy.findAggregatedHouseholdShoppingList).toHaveBeenCalled();
    expect(component.allItems.length).toBe(2);
    expect(component.targetedItems.length).toBe(1);
    expect(component.aggregatedItems.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('switchTab() swaps active items', () => {
    fixture.detectChanges();
    expect(component.currentTab).toBe('ALL');
    expect(component.activeTabItems.length).toBe(2);

    component.switchTab('TARGETED');
    expect(component.currentTab).toBe('TARGETED');
    expect(component.activeTabItems.length).toBe(1);
    expect(component.activeTabItems[0].name).toBe('Milk for me');
  });

  it('markAsBought() should call updateStatus with BOUGHT then reload', () => {
    fixture.detectChanges();
    component.markAsBought(personal);
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(1,
      { status: ShoppingItemStatus.BOUGHT });
    expect(serviceSpy.findAll).toHaveBeenCalledTimes(2);
  });

  it('markAsNeeded() should reopen an item by setting status to NEEDED', () => {
    fixture.detectChanges();
    component.markAsNeeded({ ...personal, status: ShoppingItemStatus.BOUGHT });
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(1,
      { status: ShoppingItemStatus.NEEDED });
  });

  it('cancelItem() should set status to CANCELLED', () => {
    fixture.detectChanges();
    component.cancelItem(personal);
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(1,
      { status: ShoppingItemStatus.CANCELLED });
  });

  it('deleteItem() should call delete after confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    fixture.detectChanges();
    component.deleteItem(personal);
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
  });

  it('deleteItem() should NOT call delete when confirm is dismissed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    fixture.detectChanges();
    component.deleteItem(personal);
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('should display an error message when loadItems fails', () => {
    serviceSpy.findAll.and.returnValue(throwError(() => ({
      status: 403, error: { message: 'Forbidden' }
    })));
    fixture.detectChanges();
    expect(component.actionError).toBe('Forbidden');
    expect(component.loading).toBeFalse();
  });

  it('switchTab() can switch to aggregated household list', () => {
    fixture.detectChanges();

    component.switchTab('AGGREGATED');

    expect(component.currentTab).toBe('AGGREGATED');
    expect(component.isAggregatedTab).toBeTrue();
    expect(component.aggregatedItems[0].name).toBe('Milk');
    expect(component.aggregatedItems[0].itemCount).toBe(2);
  });
});

