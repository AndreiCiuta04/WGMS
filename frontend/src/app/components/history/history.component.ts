import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { HistoryDto, PaymentHistoryDto, TaskHistoryDto } from '../../dtos/history';
import { HistoryService } from '../../services/history.service';

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.scss'],
  standalone: false
})
export class HistoryComponent implements OnInit {

  tasks: TaskHistoryDto[] = [];
  payments: PaymentHistoryDto[] = [];
  loading = true;
  errorMessage = '';

  constructor(private historyService: HistoryService) { }

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.loading = true;
    this.errorMessage = '';

    this.historyService.getHistory()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (history: HistoryDto) => {
          this.tasks = history.tasks ?? [];
          this.payments = history.payments ?? [];
        },
        error: err => {
          console.error('Error fetching history', err);
          this.tasks = [];
          this.payments = [];
          this.errorMessage = this.extractErrorMessage(err, 'Could not load history. Please try again.');
        }
      });
  }

  statusClass(status: string | null | undefined): string {
    switch ((status ?? '').toUpperCase()) {
      case 'DONE':
      case 'SETTLED':
      case 'RECORDED':
        return 'badge-wgms-active';
      case 'CANCELLED':
        return 'badge-wgms-expired';
      case 'PENDING':
      case 'PARTIALLY_SETTLED':
        return 'badge-wgms-used';
      default:
        return 'badge-wgms-used';
    }
  }

  displayStatus(status: string | null | undefined): string {
    return status ? status.replace(/_/g, ' ') : 'UNKNOWN';
  }

  taskTimestamp(task: TaskHistoryDto): string {
    return task.updatedAt || task.createdAt;
  }

  paymentTimestamp(payment: PaymentHistoryDto): string | null | undefined {
    return payment.updatedAt || payment.createdAt;
  }

  private extractErrorMessage(err: any, fallback: string): string {
    if (typeof err?.error === 'string' && err.error.trim()) {
      return err.error;
    }
    if (err?.error?.message) {
      return err.error.message;
    }
    return fallback;
  }
}
