import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShoppingItemListComponent } from './shopping-item-list.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { ShoppingItemDto, ShoppingItemStatus } from '../../dtos/shopping-item';
import { LucideAngularModule, ShoppingCart, CircleCheck, CircleX, Pencil, Trash2, RotateCcw } from 'lucide-angular';

describe('ShoppingItemListComponent', () => {
  let component: ShoppingItemListComponent;
  let fixture: ComponentFixture<ShoppingItemListComponent>;
  let serviceSpy: jasmine.SpyObj<ShoppingItemService>;

  const item: ShoppingItemDto = {
    id: 1, name: 'Milk', quantity: '2L', notes: undefined,
    status: ShoppingItemStatus.NEEDED,
    householdId: 10, createdById: 100,
    createdByEmail: 'a@test.com', createdByFirstName: 'A', createdByLastName: 'B',
    createdAt: '2026-05-17T10:00:00', updatedAt: '2026-05-17T10:00:00'
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('ShoppingItemService',
      ['findAll', 'updateStatus', 'delete']);
    serviceSpy.findAll.and.returnValue(of([item]));
    serviceSpy.updateStatus.and.returnValue(of({ ...item, status: ShoppingItemStatus.BOUGHT }));
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

  it('should load items on init', () => {
    fixture.detectChanges();
    expect(serviceSpy.findAll).toHaveBeenCalled();
    expect(component.items.length).toBe(1);
    expect(component.items[0].name).toBe('Milk');
    expect(component.loading).toBeFalse();
  });

  it('markAsBought() should call updateStatus with BOUGHT then reload', () => {
    fixture.detectChanges();
    component.markAsBought(item);
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(1,
      { status: ShoppingItemStatus.BOUGHT });
    expect(serviceSpy.findAll).toHaveBeenCalledTimes(2);
  });

  it('markAsNeeded() should reopen an item by setting status to NEEDED', () => {
    fixture.detectChanges();
    component.markAsNeeded({ ...item, status: ShoppingItemStatus.BOUGHT });
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(1,
      { status: ShoppingItemStatus.NEEDED });
  });

  it('cancelItem() should set status to CANCELLED', () => {
    fixture.detectChanges();
    component.cancelItem(item);
    expect(serviceSpy.updateStatus).toHaveBeenCalledWith(1,
      { status: ShoppingItemStatus.CANCELLED });
  });

  it('deleteItem() should call delete after confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    fixture.detectChanges();
    component.deleteItem(item);
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
  });

  it('deleteItem() should NOT call delete when confirm is dismissed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    fixture.detectChanges();
    component.deleteItem(item);
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('should display an error message when findAll fails', () => {
    serviceSpy.findAll.and.returnValue(throwError(() => ({
      status: 403, error: { message: 'Forbidden' }
    })));
    fixture.detectChanges();
    expect(component.actionError).toBe('Forbidden');
    expect(component.loading).toBeFalse();
  });
});
