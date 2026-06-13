import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShoppingItemListComponent } from './shopping-item-list.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { AggregatedShoppingItemDto, ShoppingItemDto, ShoppingItemStatus, ShoppingItemSuggestionDto, ShoppingItemVisibility } from '../../dtos/shopping-item';
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
  const historyItem: ShoppingItemDto = {
    ...personal,
    id: 3,
    name: 'Bought bread',
    quantity: '1 loaf',
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
      quantity: suggestion.quantity,
      notes: suggestion.notes,
      visibility: ShoppingItemVisibility.SHARED
    }));

    serviceSpy.deleteSuggestion.and.returnValue(of(void 0));

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
    component.suggestionQuantity = '1 bottle';
    component.suggestionNotes = 'For cooking';

    component.createSuggestion();

    expect(serviceSpy.createSuggestion).toHaveBeenCalledWith({
      name: 'Olive oil',
      quantity: '1 bottle',
      notes: 'For cooking'
    });

    expect(serviceSpy.findSuggestions).toHaveBeenCalledTimes(2);
    expect(component.currentTab).toBe('SUGGESTIONS');
    expect(component.suggestionName).toBe('');
    expect(component.suggestionQuantity).toBe('');
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
});
