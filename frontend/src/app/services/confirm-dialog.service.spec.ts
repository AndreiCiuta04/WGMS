import { TestBed } from '@angular/core/testing';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmDialogComponent } from '../components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogService } from './confirm-dialog.service';

describe('ConfirmDialogService', () => {
  let service: ConfirmDialogService;
  let modalService: jasmine.SpyObj<NgbModal>;
  let modalRef: Partial<NgbModalRef> & { componentInstance: ConfirmDialogComponent };

  beforeEach(() => {
    modalRef = {
      componentInstance: new ConfirmDialogComponent({} as never),
      result: Promise.resolve(true)
    };
    modalService = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);
    modalService.open.and.returnValue(modalRef as NgbModalRef);

    TestBed.configureTestingModule({
      providers: [
        ConfirmDialogService,
        { provide: NgbModal, useValue: modalService }
      ]
    });

    service = TestBed.inject(ConfirmDialogService);
  });

  it('should open the shared dialog and apply provided options', async () => {
    const result = await service.confirm({
      title: 'Delete expense?',
      message: 'Expense data will be removed.',
      confirmLabel: 'Delete',
      cancelLabel: 'Cancel',
      danger: false
    });

    expect(modalService.open).toHaveBeenCalledOnceWith(ConfirmDialogComponent, {
      centered: true,
      backdrop: true,
      keyboard: true
    });
    expect(modalRef.componentInstance.title).toBe('Delete expense?');
    expect(modalRef.componentInstance.message).toBe('Expense data will be removed.');
    expect(modalRef.componentInstance.confirmLabel).toBe('Delete');
    expect(modalRef.componentInstance.cancelLabel).toBe('Cancel');
    expect(modalRef.componentInstance.danger).toBeFalse();
    expect(result).toBeTrue();
  });

  it('should keep default labels when optional labels are omitted', async () => {
    await service.confirm({ title: 'Leave defaults' });

    expect(modalRef.componentInstance.title).toBe('Leave defaults');
    expect(modalRef.componentInstance.message).toBe('');
    expect(modalRef.componentInstance.confirmLabel).toBe('Delete');
    expect(modalRef.componentInstance.cancelLabel).toBe('Cancel');
    expect(modalRef.componentInstance.danger).toBeTrue();
  });

  it('should resolve false when the dialog is dismissed', async () => {
    modalRef.result = Promise.reject('esc');

    await expectAsync(service.confirm({ title: 'Dismiss' })).toBeResolvedTo(false);
  });
});
