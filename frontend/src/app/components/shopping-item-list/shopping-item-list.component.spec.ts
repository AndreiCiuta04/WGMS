import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShoppingItemListComponent } from './shopping-item-list.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { AggregatedShoppingItemDto, QuantityUnit, ShoppingItemDto, ShoppingItemStatus, ShoppingItemSuggestionDto, ShoppingItemVisibility } from '../../dtos/shopping-item';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { ShoppingQuantityPipe } from '../../pipes/shopping-quantity.pipe';
import { LucideAngularModule, ShoppingCart, CircleCheck, CircleX, Pencil, Trash2, RotateCcw, QrCode, ScanLine } from 'lucide-angular';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

describe('ShoppingItemListComponent', () => {
  let component: ShoppingItemListComponent;
  let fixture: ComponentFixture<ShoppingItemListComponent>;
  let serviceSpy: jasmine.SpyObj<ShoppingItemService>;
  let confirmDialog: jasmine.SpyObj<ConfirmDialogService>;
  let modalService: jasmine.SpyObj<NgbModal>;

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

  const suggestion: ShoppingItemSuggestionDto = {
    id: 20,
    name: 'Dishwasher tabs',
    quantity: '1 pack',
    notes: 'Almost empty',
    householdId: 10,
    createdById: 100,
    createdByEmail: 'a@test.com',
    createdByFirstName: 'A',
    createdByLastName: 'B',
    voteCount: 2,
    votedByCurrentUser: true,
    voters: [
      {
        userId: 100,
        email: 'a@test.com',
        firstName: 'A',
        lastName: 'B'
      },
      {
        userId: 200,
        email: 'b@test.com',
        firstName: 'B',
        lastName: 'C'
      }
    ],
    createdAt: '2026-05-17T10:00:00',
    updatedAt: '2026-05-17T10:05:00'
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('ShoppingItemService', [
      'findAll',
      'findTargetedAtMe',
      'findAggregatedHouseholdShoppingList',
      'findHouseholdShoppingHistory',
      'findSuggestions',
      'createSuggestion',
      'voteForSuggestion',
      'removeVoteFromSuggestion',
      'promoteSuggestion',
      'deleteSuggestion',
      'updateStatus',
      'delete'
    ]);
    serviceSpy.findAll.and.returnValue(of([personal, targeted]));
    serviceSpy.findTargetedAtMe.and.returnValue(of([targeted]));
    serviceSpy.findAggregatedHouseholdShoppingList.and.returnValue(of([aggregated]));
    serviceSpy.findHouseholdShoppingHistory.and.returnValue(of([historyItem]));
    serviceSpy.updateStatus.and.returnValue(of({ ...personal, status: ShoppingItemStatus.BOUGHT }));
    serviceSpy.delete.and.returnValue(of(void 0));
    serviceSpy.findSuggestions.and.returnValue(of([suggestion]));
    serviceSpy.createSuggestion.and.returnValue(of(suggestion));

    serviceSpy.voteForSuggestion.and.returnValue(of({
      ...suggestion,
      votedByCurrentUser: true
    }));

    serviceSpy.removeVoteFromSuggestion.and.returnValue(of({
      ...suggestion,
      voteCount: 1,
      votedByCurrentUser: false,
      voters: [suggestion.voters[0]]
    }));

    serviceSpy.promoteSuggestion.and.returnValue(of({
      ...personal,
      id: suggestion.id,
      name: suggestion.name,
      quantityCount: null,
      quantityAmount: 1,
      quantityUnit: QuantityUnit.PACK,
      notes: suggestion.notes,
      visibility: ShoppingItemVisibility.SHARED
    }));

    serviceSpy.deleteSuggestion.and.returnValue(of(void 0));
    confirmDialog = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));
    modalService = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);
    modalService.open.and.returnValue({ componentInstance: {} } as never);


    await TestBed.configureTestingModule({
      declarations: [ShoppingItemListComponent, ShoppingQuantityPipe],
      imports: [
        HttpClientTestingModule,
        RouterModule.forRoot([]),
        LucideAngularModule.pick({ShoppingCart, CircleCheck, CircleX, Pencil, Trash2, RotateCcw, QrCode, ScanLine})
      ],
      providers: [
        { provide: ShoppingItemService, useValue: serviceSpy },
        { provide: ConfirmDialogService, useValue: confirmDialog },
        { provide: NgbModal, useValue: modalService }
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
    expect(serviceSpy.findSuggestions).toHaveBeenCalled();
    expect(serviceSpy.findHouseholdShoppingHistory).toHaveBeenCalled();

    expect(component.allItems.length).toBe(2);
    expect(component.targetedItems.length).toBe(1);
    expect(component.aggregatedItems.length).toBe(1);
    expect(component.suggestions.length).toBe(1);
    expect(component.historyItems.length).toBe(1);
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

  it('switchTab() can switch to suggestions', () => {
    fixture.detectChanges();

    component.switchTab('SUGGESTIONS');

    expect(component.currentTab).toBe('SUGGESTIONS');
    expect(component.isSuggestionsTab).toBeTrue();
    expect(component.suggestions[0].name).toBe('Dishwasher tabs');
  });

  it('isPopularSuggestion() returns true for two or more votes', () => {
    expect(component.isPopularSuggestion(suggestion)).toBeTrue();

    expect(component.isPopularSuggestion({
      ...suggestion,
      voteCount: 1
    })).toBeFalse();
  });

  it('voterDisplayName() returns the voters full name', () => {
    expect(component.voterDisplayName(suggestion.voters[0])).toBe('A B');
  });

  it('voterDisplayName() falls back to email when name is empty', () => {
    expect(component.voterDisplayName({
      userId: 300,
      email: 'fallback@test.com',
      firstName: '',
      lastName: ''
    })).toBe('fallback@test.com');
  });

  it('createSuggestion() calls the service and reloads all views', () => {
    fixture.detectChanges();

    component.suggestionName = 'Olive oil';
    component.suggestionQuantityCount = null;
    component.suggestionQuantityAmount = 1;
    component.suggestionQuantityUnit = QuantityUnit.PACK;
    component.suggestionNotes = 'For cooking';

    component.createSuggestion();

    expect(serviceSpy.createSuggestion).toHaveBeenCalledWith({
      name: 'Olive oil',
      quantity: '1 pack',
      notes: 'For cooking'
    });

    expect(serviceSpy.findSuggestions).toHaveBeenCalledTimes(2);
    expect(component.currentTab).toBe('SUGGESTIONS');
    expect(component.suggestionName).toBe('');
    expect(component.suggestionQuantityCount).toBeNull();
    expect(component.suggestionQuantityAmount).toBeNull();
    expect(component.suggestionQuantityUnit).toBeNull();
    expect(component.suggestionNotes).toBe('');
  });

  it('createSuggestion() does not call service for a blank name', () => {
    component.suggestionName = '   ';

    component.createSuggestion();

    expect(serviceSpy.createSuggestion).not.toHaveBeenCalled();
    expect(component.actionError).toBe('Suggestion name is required.');
  });

  it('voteForSuggestion() votes and reloads the views', () => {
    fixture.detectChanges();

    component.voteForSuggestion({
      ...suggestion,
      votedByCurrentUser: false
    });

    expect(serviceSpy.voteForSuggestion).toHaveBeenCalledWith(20);
    expect(serviceSpy.findSuggestions).toHaveBeenCalledTimes(2);
    expect(component.isSuggestionActionPending(20)).toBeFalse();
  });

  it('removeVoteFromSuggestion() removes vote and reloads the views', () => {
    fixture.detectChanges();

    component.removeVoteFromSuggestion(suggestion);

    expect(serviceSpy.removeVoteFromSuggestion).toHaveBeenCalledWith(20);
    expect(serviceSpy.findSuggestions).toHaveBeenCalledTimes(2);
    expect(component.isSuggestionActionPending(20)).toBeFalse();
  });

  it('promoteSuggestion() promotes after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    fixture.detectChanges();

    component.promoteSuggestion(suggestion);

    expect(serviceSpy.promoteSuggestion).toHaveBeenCalledWith(20);
    expect(serviceSpy.findSuggestions).toHaveBeenCalledTimes(2);
  });

  it('promoteSuggestion() does not promote when confirmation is dismissed', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.promoteSuggestion(suggestion);

    expect(serviceSpy.promoteSuggestion).not.toHaveBeenCalled();
  });

  it('deleteSuggestion() deletes after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    fixture.detectChanges();

    component.deleteSuggestion(suggestion);

    expect(serviceSpy.deleteSuggestion).toHaveBeenCalledWith(20);
    expect(serviceSpy.findSuggestions).toHaveBeenCalledTimes(2);
  });

  it('deleteSuggestion() does not delete when confirmation is dismissed', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.deleteSuggestion(suggestion);

    expect(serviceSpy.deleteSuggestion).not.toHaveBeenCalled();
  });

  it('createSuggestion() builds a structured counted quantity', () => {
    fixture.detectChanges();

    component.suggestionName = 'Water';
    component.suggestionQuantityCount = 5;
    component.suggestionQuantityAmount = 3;
    component.suggestionQuantityUnit = QuantityUnit.LITER;

    component.createSuggestion();

    expect(serviceSpy.createSuggestion).toHaveBeenCalledWith({
      name: 'Water',
      quantity: '5 x 3 L',
      notes: null
    });
  });

  it('createSuggestion() rejects incomplete structured quantity', () => {
    component.suggestionName = 'Milk';
    component.suggestionQuantityAmount = 2;
    component.suggestionQuantityUnit = null;

    component.createSuggestion();

    expect(serviceSpy.createSuggestion).not.toHaveBeenCalled();
    expect(component.actionError)
      .toBe('Quantity amount and unit must be provided together.');
  });

  it('openQrShare() opens the QR modal and passes the selected item', () => {
    component.openQrShare(personal);

    expect(modalService.open).toHaveBeenCalled();
    expect((modalService.open.calls.mostRecent().returnValue.componentInstance as { item: ShoppingItemDto }).item)
      .toBe(personal);
  });

  it('toggleCompletedExpanded() toggles the completed section', () => {
    expect(component.completedExpanded).toBeFalse();

    component.toggleCompletedExpanded();
    expect(component.completedExpanded).toBeTrue();

    component.toggleCompletedExpanded();
    expect(component.completedExpanded).toBeFalse();
  });

  it('neededItems and completedItems split active tab by status', () => {
    component.allItems = [
      personal,
      { ...personal, id: 30, status: ShoppingItemStatus.BOUGHT },
      { ...personal, id: 31, status: ShoppingItemStatus.CANCELLED }
    ];

    expect(component.neededItems.map(item => item.id)).toEqual([1]);
    expect(component.completedItems.map(item => item.id)).toEqual([30, 31]);
  });

  it('deleteItem() surfaces delete errors after confirmation', fakeAsync(() => {
    serviceSpy.delete.and.returnValue(throwError(() => ({ status: 403 })));
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    component.deleteItem(personal);
    tick();

    expect(component.actionError).toBe('You are not allowed to perform this action.');
  }));

  it('updateStatus() surfaces validation arrays from the backend', () => {
    serviceSpy.updateStatus.and.returnValue(throwError(() => ({
      error: { 'Validation errors': ['status must not be null', 'id is invalid'] }
    })));

    component.markAsBought(personal);

    expect(component.actionError).toBe('status must not be null, id is invalid');
  });

  it('createSuggestion() surfaces service errors and resets submitting state', () => {
    serviceSpy.createSuggestion.and.returnValue(throwError(() => ({ status: 401 })));
    component.suggestionName = 'Tea';

    component.createSuggestion();

    expect(component.suggestionSubmitting).toBeFalse();
    expect(component.actionError).toContain('session has expired');
  });

  it('suggestion actions are ignored while the same suggestion is pending', () => {
    component.suggestionActionIds.add(suggestion.id);
    const confirmSpy = spyOn(window, 'confirm');

    component.voteForSuggestion(suggestion);
    component.removeVoteFromSuggestion(suggestion);
    component.promoteSuggestion(suggestion);
    component.deleteSuggestion(suggestion);

    expect(serviceSpy.voteForSuggestion).not.toHaveBeenCalled();
    expect(serviceSpy.removeVoteFromSuggestion).not.toHaveBeenCalled();
    expect(serviceSpy.promoteSuggestion).not.toHaveBeenCalled();
    expect(serviceSpy.deleteSuggestion).not.toHaveBeenCalled();
    expect(confirmSpy).not.toHaveBeenCalled();
  });

  it('suggestion action errors remove pending state and expose safe messages', () => {
    serviceSpy.voteForSuggestion.and.returnValue(throwError(() => ({ status: 403 })));
    serviceSpy.removeVoteFromSuggestion.and.returnValue(throwError(() => ({ error: { message: 'Vote missing' } })));
    serviceSpy.promoteSuggestion.and.returnValue(throwError(() => ({ status: 500 })));
    serviceSpy.deleteSuggestion.and.returnValue(throwError(() => ({ status: 401 })));
    spyOn(window, 'confirm').and.returnValue(true);

    component.voteForSuggestion(suggestion);
    expect(component.isSuggestionActionPending(suggestion.id)).toBeFalse();
    expect(component.actionError).toBe('You are not allowed to perform this action.');

    component.removeVoteFromSuggestion(suggestion);
    expect(component.isSuggestionActionPending(suggestion.id)).toBeFalse();
    expect(component.actionError).toBe('Vote missing');

    component.promoteSuggestion(suggestion);
    expect(component.isSuggestionActionPending(suggestion.id)).toBeFalse();
    expect(component.actionError).toBe('Failed to add the suggestion to the shopping list.');

    component.deleteSuggestion(suggestion);
    expect(component.isSuggestionActionPending(suggestion.id)).toBeFalse();
    expect(component.actionError).toContain('session has expired');
  });
});
