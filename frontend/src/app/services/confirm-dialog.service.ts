import { Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmDialogComponent } from '../components/confirm-dialog/confirm-dialog.component';

export interface ConfirmDialogOptions {
  title: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  /** Renders the confirm button in a destructive (red) style. Defaults to true. */
  danger?: boolean;
}

/**
 * Opens a single, app-wide styled confirmation dialog so every destructive action
 * shares one look and feel instead of relying on the native browser confirm().
 */
@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {

  constructor(private modalService: NgbModal) { }

  /** Resolves to true when the user confirms, false on cancel/backdrop/Esc. */
  confirm(options: ConfirmDialogOptions): Promise<boolean> {
    const modalRef = this.modalService.open(ConfirmDialogComponent, {
      centered: true,
      backdrop: true,
      keyboard: true
    });

    const instance = modalRef.componentInstance as ConfirmDialogComponent;
    instance.title = options.title;
    instance.message = options.message ?? '';
    if (options.confirmLabel !== undefined) {
      instance.confirmLabel = options.confirmLabel;
    }
    if (options.cancelLabel !== undefined) {
      instance.cancelLabel = options.cancelLabel;
    }
    if (options.danger !== undefined) {
      instance.danger = options.danger;
    }

    return modalRef.result.then(
      result => result === true,
      () => false
    );
  }
}
