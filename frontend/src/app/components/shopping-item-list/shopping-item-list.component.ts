import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { AggregatedShoppingItemDto, QUANTITY_UNIT_OPTIONS, QuantityUnit, ShoppingItemDto, ShoppingItemStatus, ShoppingItemSuggestionDto, ShoppingItemVisibility } from '../../dtos/shopping-item';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';

@Component({
  selector: 'app-shopping-item-list',
  templateUrl: './shopping-item-list.component.html',
  styleUrls: ['./shopping-item-list.component.scss'],
  standalone: false
})
export class ShoppingItemListComponent implements OnInit {

  allItems: ShoppingItemDto[] = [];
  targetedItems: ShoppingItemDto[] = [];
  aggregatedItems: AggregatedShoppingItemDto[] = [];
  historyItems: ShoppingItemDto[] = [];
  suggestions: ShoppingItemSuggestionDto[] = [];

  currentTab: 'ALL' | 'TARGETED' | 'AGGREGATED' | 'SUGGESTIONS' | 'HISTORY' = 'ALL';
  loading = false;
  actionError: string | null = null;
  successMessage: string | null = null;

  suggestionName = '';
  suggestionQuantityCount: number | null = null;
  suggestionQuantityAmount: number | null = null;
  suggestionQuantityUnit: QuantityUnit | null = null;
  suggestionNotes = '';
  suggestionSubmitting = false;

  readonly quantityUnitOptions = QUANTITY_UNIT_OPTIONS;

  suggestionActionIds = new Set<number>();

  /** Controls visibility of the Bought & Cancelled section */
  completedExpanded = false;

  ShoppingItemStatus = ShoppingItemStatus;
  ShoppingItemVisibility = ShoppingItemVisibility;

  constructor(
    private shoppingItemService: ShoppingItemService,
    private confirmDialog: ConfirmDialogService
  ) { }

  ngOnInit(): void {
    this.loadItems();
  }

  get activeTabItems(): ShoppingItemDto[] {
    return this.currentTab === 'ALL' ? this.allItems : this.targetedItems;
  }

  /** Items that still need to be bought in the active tab */
  get neededItems(): ShoppingItemDto[] {
    return this.activeTabItems.filter(i => i.status === ShoppingItemStatus.NEEDED);
  }

  /** Items that are bought or cancelled in the active tab */
  get completedItems(): ShoppingItemDto[] {
    return this.activeTabItems.filter(
      i => i.status === ShoppingItemStatus.BOUGHT || i.status === ShoppingItemStatus.CANCELLED
    );
  }

  get isAggregatedTab(): boolean {
    return this.currentTab === 'AGGREGATED';
  }

  get isHistoryTab(): boolean {
    return this.currentTab === 'HISTORY';
  }

  get isSuggestionsTab(): boolean {
    return this.currentTab === 'SUGGESTIONS';
  }

  toggleCompletedExpanded(): void {
    this.completedExpanded = !this.completedExpanded;
  }

  switchTab(tab: 'ALL' | 'TARGETED' | 'AGGREGATED' | 'SUGGESTIONS' | 'HISTORY') {
    this.currentTab = tab;
  }

  loadItems() {
    this.loading = true;
    this.actionError = null;
    forkJoin({
      all: this.shoppingItemService.findAll(),
      targeted: this.shoppingItemService.findTargetedAtMe(),
      aggregated: this.shoppingItemService.findAggregatedHouseholdShoppingList(),
      suggestions: this.shoppingItemService.findSuggestions(),
      history: this.shoppingItemService.findHouseholdShoppingHistory()

    }).subscribe({
      next: ({ all, targeted, aggregated, suggestions, history }) => {
        this.allItems = all;
        this.targetedItems = targeted;
        this.aggregatedItems = aggregated;
        this.suggestions = suggestions;
        this.historyItems = history;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching shopping items', err);
        this.actionError = this.extractError(err, 'Failed to load shopping items.');
        this.loading = false;
      }
    });
  }

  markAsBought(item: ShoppingItemDto) {
    this.updateStatus(item, ShoppingItemStatus.BOUGHT, 'mark as bought', 'Item marked as bought.');
  }

  markAsNeeded(item: ShoppingItemDto) {
    this.updateStatus(item, ShoppingItemStatus.NEEDED, 'reopen', 'Item moved back to needed.');
  }

  cancelItem(item: ShoppingItemDto) {
    this.updateStatus(item, ShoppingItemStatus.CANCELLED, 'cancel', 'Item cancelled.');
  }

  deleteItem(item: ShoppingItemDto) {
    this.confirmDialog.confirm({
      title: 'Delete item?',
      message: `"${item.name}" will be permanently removed from the shopping list.`,
      confirmLabel: 'Delete',
      danger: true
    }).then(confirmed => {
      if (!confirmed) {
        return;
      }
      this.actionError = null;
      this.shoppingItemService.delete(item.id).subscribe({
        next: () => {
          this.showSuccess('Item deleted.');
          this.loadItems();
        },
        error: (err) => {
          console.error('Error deleting item', err);
          this.actionError = this.extractError(err, 'Failed to delete the shopping item.');
        }
      });
    });
  }

  createSuggestion(): void {
    const name = this.suggestionName.trim();
    const notes = this.suggestionNotes.trim();

    if (!name) {
      this.actionError = 'Suggestion name is required.';
      return;
    }

    const quantity = this.buildSuggestionQuantity();

    if (
      (this.suggestionQuantityCount != null
        || this.suggestionQuantityAmount != null
        || this.suggestionQuantityUnit != null)
      && quantity == null
    ) {
      this.actionError =
        'Quantity amount and unit must be provided together.';
      return;
    }

    this.actionError = null;
    this.suggestionSubmitting = true;

    this.shoppingItemService.createSuggestion({
      name,
      quantity,
      notes: notes || null
    }).subscribe({
      next: () => {
        this.suggestionName = '';
        this.suggestionQuantityCount = null;
        this.suggestionQuantityAmount = null;
        this.suggestionQuantityUnit = null;
        this.suggestionNotes = '';
        this.suggestionSubmitting = false;
        this.showSuccess('Suggestion saved and your vote recorded.');
        this.loadItems();
        this.currentTab = 'SUGGESTIONS';
      },
      error: (err) => {
        console.error('Error creating shopping item suggestion', err);
        this.actionError = this.extractError(err, 'Failed to create suggestion.');
        this.suggestionSubmitting = false;
      }
    });
  }

  private buildSuggestionQuantity(): string | null {
    const count = this.suggestionQuantityCount;
    const amount = this.suggestionQuantityAmount;
    const unit = this.suggestionQuantityUnit;

    const nothingEntered =
      count == null && amount == null && unit == null;

    if (nothingEntered) {
      return null;
    }

    if (amount == null || unit == null) {
      return null;
    }

    const unitSymbol = this.quantityUnitSymbol(unit);
    const measuredQuantity = `${amount} ${unitSymbol}`;

    return count != null
      ? `${count} x ${measuredQuantity}`
      : measuredQuantity;
  }

  private quantityUnitSymbol(unit: QuantityUnit): string {
    switch (unit) {
      case QuantityUnit.MILLILITER:
        return 'ml';
      case QuantityUnit.LITER:
        return 'L';
      case QuantityUnit.GRAM:
        return 'g';
      case QuantityUnit.KILOGRAM:
        return 'kg';
      case QuantityUnit.PIECE:
        return 'piece';
      case QuantityUnit.PACK:
        return 'pack';
    }
  }

  voteForSuggestion(suggestion: ShoppingItemSuggestionDto): void {
    if (this.isSuggestionActionPending(suggestion.id)) {
      return;
    }

    this.actionError = null;
    this.suggestionActionIds.add(suggestion.id);

    this.shoppingItemService.voteForSuggestion(suggestion.id).subscribe({
      next: () => {
        this.suggestionActionIds.delete(suggestion.id);
        this.showSuccess('Vote added.');
        this.loadItems();
      },
      error: (err) => {
        this.suggestionActionIds.delete(suggestion.id);
        console.error('Error voting for suggestion', err);
        this.actionError = this.extractError(err, 'Failed to vote for suggestion.');
      }
    });
  }

  removeVoteFromSuggestion(suggestion: ShoppingItemSuggestionDto): void {
    if (this.isSuggestionActionPending(suggestion.id)) {
      return;
    }

    this.actionError = null;
    this.suggestionActionIds.add(suggestion.id);

    this.shoppingItemService.removeVoteFromSuggestion(suggestion.id).subscribe({
      next: () => {
        this.suggestionActionIds.delete(suggestion.id);
        this.showSuccess('Vote removed.');
        this.loadItems();
      },
      error: (err) => {
        this.suggestionActionIds.delete(suggestion.id);
        console.error('Error removing suggestion vote', err);
        this.actionError = this.extractError(err, 'Failed to remove vote.');
      }
    });
  }

  promoteSuggestion(suggestion: ShoppingItemSuggestionDto): void {
    if (this.isSuggestionActionPending(suggestion.id)) {
      return;
    }

    const confirmed = confirm(
      `Add "${suggestion.name}" to the shared household shopping list?`
    );

    if (!confirmed) {
      return;
    }

    this.actionError = null;
    this.suggestionActionIds.add(suggestion.id);

    this.shoppingItemService.promoteSuggestion(suggestion.id).subscribe({
      next: () => {
        this.suggestionActionIds.delete(suggestion.id);
        this.showSuccess('Suggestion added to the household shopping list.');
        this.loadItems();
      },
      error: (err) => {
        this.suggestionActionIds.delete(suggestion.id);
        console.error('Error promoting shopping suggestion', err);
        this.actionError = this.extractError(
          err,
          'Failed to add the suggestion to the shopping list.'
        );
      }
    });
  }

  deleteSuggestion(suggestion: ShoppingItemSuggestionDto): void {
    if (this.isSuggestionActionPending(suggestion.id)) {
      return;
    }

    const confirmed = confirm(
      `Are you sure you want to delete the suggestion "${suggestion.name}"?`
    );

    if (!confirmed) {
      return;
    }

    this.actionError = null;
    this.suggestionActionIds.add(suggestion.id);

    this.shoppingItemService.deleteSuggestion(suggestion.id).subscribe({
      next: () => {
        this.suggestionActionIds.delete(suggestion.id);
        this.showSuccess('Suggestion deleted.');
        this.loadItems();
      },
      error: (err) => {
        this.suggestionActionIds.delete(suggestion.id);
        console.error('Error deleting shopping suggestion', err);
        this.actionError = this.extractError(
          err,
          'Failed to delete the suggestion.'
        );
      }
    });
  }

  isSuggestionActionPending(id: number): boolean {
    return this.suggestionActionIds.has(id);
  }

  isPopularSuggestion(suggestion: ShoppingItemSuggestionDto): boolean {
    return suggestion.voteCount >= 2;
  }

  voterDisplayName(voter: ShoppingItemSuggestionDto['voters'][number]): string {
    const fullName = `${voter.firstName ?? ''} ${voter.lastName ?? ''}`.trim();
    return fullName || voter.email;
  }

  private updateStatus(item: ShoppingItemDto, status: ShoppingItemStatus, label: string, successMsg: string) {
    this.actionError = null;
    this.shoppingItemService.updateStatus(item.id, { status }).subscribe({
      next: () => {
        this.showSuccess(successMsg);
        this.loadItems();
      },
      error: (err) => {
        console.error(`Error trying to ${label} item`, err);
        this.actionError = this.extractError(err, `Failed to ${label} the shopping item.`);
      }
    });
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => { this.successMessage = null; }, 3000);
  }



  private extractError(err: any, fallback: string): string {
    if (err?.error?.message) {
      return err.error.message;
    }
    const validation = err?.error?.['Validation errors'];
    if (Array.isArray(validation) && validation.length) {
      return validation.join(', ');
    }
    if (err?.status === 403) {
      return 'You are not allowed to perform this action.';
    }
    if (err?.status === 401) {
      return 'Your session has expired. Please log in again.';
    }
    return fallback;
  }
}
