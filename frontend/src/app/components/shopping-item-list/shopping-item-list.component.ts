import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { AggregatedShoppingItemDto, ShoppingItemDto, ShoppingItemStatus, ShoppingItemVisibility } from '../../dtos/shopping-item';

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

  currentTab: 'ALL' | 'TARGETED' | 'AGGREGATED' | 'HISTORY' = 'ALL';
  loading = false;
  actionError: string | null = null;
  successMessage: string | null = null;

  /** Controls visibility of the Bought & Cancelled section */
  completedExpanded = false;

  ShoppingItemStatus = ShoppingItemStatus;
  ShoppingItemVisibility = ShoppingItemVisibility;

  constructor(private shoppingItemService: ShoppingItemService) { }

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

  toggleCompletedExpanded(): void {
    this.completedExpanded = !this.completedExpanded;
  }

  switchTab(tab: 'ALL' | 'TARGETED' | 'AGGREGATED' | 'HISTORY') {
    this.currentTab = tab;
  }

  loadItems() {
    this.loading = true;
    this.actionError = null;
    forkJoin({
      all: this.shoppingItemService.findAll(),
      targeted: this.shoppingItemService.findTargetedAtMe(),
      aggregated: this.shoppingItemService.findAggregatedHouseholdShoppingList(),
      history: this.shoppingItemService.findHouseholdShoppingHistory()

    }).subscribe({
      next: ({ all, targeted, aggregated, history }) => {
        this.allItems = all;
        this.targetedItems = targeted;
        this.aggregatedItems = aggregated;
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
    if (confirm('Are you sure you want to delete this shopping item?')) {
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
    }
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
