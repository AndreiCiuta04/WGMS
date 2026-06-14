import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShoppingItemListComponent } from './shopping-item-list.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { ShoppingQuantityPipe } from '../../pipes/shopping-quantity.pipe';
import { AggregatedShoppingItemDto, ShoppingItemDto, ShoppingItemStatus, ShoppingItemVisibility, QuantityUnit } from '../../dtos/shopping-item';
import { LucideAngularModule, ShoppingCart, CircleCheck, CircleX, Pencil, Trash2, RotateCcw } from 'lucide-angular';

describe('ShoppingItemListComponent', () => {
  let component: ShoppingItemListComponent;
  let fixture: ComponentFixture<ShoppingItemListComponent>;
  let serviceSpy: jasmine.SpyObj<ShoppingItemService>;
  let confirmDialog: jasmine.SpyObj<ConfirmDialogService>;

  const personal: ShoppingItemDto = {
    id: 1, name: 'My personal', quantityCount: null, quantityAmount: null, quantityUnit: null, notes: null,
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
    quantityCount: null, quantityAmount: 2, quantityUnit: QuantityUnit.LITER,
    itemCount: 2,
    sourceItems: [
      {
        id: 10,
        name: 'Milk',
        quantityCount: null, quantityAmount: 2, quantityUnit: QuantityUnit.LITER,
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
        quantityCount: null, quantityAmount: 2, quantityUnit: QuantityUnit.LITER,
        notes: null,
        createdById: 200,
        createdByEmail: 'b@test.com',
        createdByFirstName: 'B',
        createdByLastName: 'C',
        createdAt: '2026-05-17T10:05:00'
      }
    ]
  };
  const historyItem: ShoppingItemDto = {
    ...personal,
    id: 3,
    name: 'Bought bread',
    quantityCount: null, quantityAmount: 1, quantityUnit: QuantityUnit.PIECE,
    status: ShoppingItemStatus.BOUGHT,
    visibility: ShoppingItemVisibility.SHARED,
    updatedAt: '2026-05-18T08:00:00'
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('ShoppingItemService',
      ['findAll', 'findTargetedAtMe', 'findAggregatedHouseholdShoppingList', 'findHouseholdShoppingHistory', 'updateStatus', 'delete']);
    serviceSpy.findAll.and.returnValue(of([personal, targeted]));
    serviceSpy.findTargetedAtMe.and.returnValue(of([targeted]));
    serviceSpy.findAggregatedHouseholdShoppingList.and.returnValue(of([aggregated]));
    serviceSpy.findHouseholdShoppingHistory.and.returnValue(of([historyItem]));
    serviceSpy.updateStatus.and.returnValue(of({ ...personal, status: ShoppingItemStatus.BOUGHT }));
    serviceSpy.delete.and.returnValue(of(void 0));
    confirmDialog = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemListComponent, ShoppingQuantityPipe],
      imports: [
        HttpClientTestingModule,
        RouterModule.forRoot([]),
        LucideAngularModule.pick({ShoppingCart, CircleCheck, CircleX, Pencil, Trash2, RotateCcw})
      ],
      providers: [
        { provide: ShoppingItemService, useValue: serviceSpy },
        { provide: ConfirmDialogService, useValue: confirmDialog }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch all-items, targeted-at-me, aggregated household list, and history on init', () => {
    fixture.detectChanges();
    expect(serviceSpy.findAll).toHaveBeenCalled();
    expect(serviceSpy.findTargetedAtMe).toHaveBeenCalled();
    expect(serviceSpy.findAggregatedHouseholdShoppingList).toHaveBeenCalled();
    expect(serviceSpy.findHouseholdShoppingHistory).toHaveBeenCalled();
    expect(component.allItems.length).toBe(2);
    expect(component.targetedItems.length).toBe(1);
    expect(component.aggregatedItems.length).toBe(1);
    expect(component.historyItems.length).toBe(1);
    expect(component.historyItems[0].status).toBe(ShoppingItemStatus.BOUGHT);
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
    component.markAsNeeded(historyItem);
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(3,
      { status: ShoppingItemStatus.NEEDED });
  });

  it('cancelItem() should set status to CANCELLED', () => {
    fixture.detectChanges();
    component.cancelItem(personal);
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(1,
      { status: ShoppingItemStatus.CANCELLED });
  });

  it('deleteItem() should call delete after confirm', fakeAsync(() => {
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));
    fixture.detectChanges();
    component.deleteItem(personal);
    tick();
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
  }));

  it('deleteItem() should NOT call delete when confirm is dismissed', fakeAsync(() => {
    confirmDialog.confirm.and.returnValue(Promise.resolve(false));
    fixture.detectChanges();
    component.deleteItem(personal);
    tick();
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  }));

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

  it('switchTab() can switch to shopping history', () => {
    fixture.detectChanges();

    component.switchTab('HISTORY');

    expect(component.currentTab).toBe('HISTORY');
    expect(component.isHistoryTab).toBeTrue();
    expect(component.historyItems[0].name).toBe('Bought bread');
  });
});
