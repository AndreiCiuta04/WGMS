import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmDialogComponent } from './confirm-dialog.component';

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;
  let fixture: ComponentFixture<ConfirmDialogComponent>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    activeModal = jasmine.createSpyObj<NgbActiveModal>('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [ConfirmDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: activeModal }]
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmDialogComponent);
    component = fixture.componentInstance;
  });

  it('should render default labels and message inputs', () => {
    component.title = 'Remove expense?';
    component.message = 'This cannot be undone.';
    component.confirmLabel = 'Remove';
    component.cancelLabel = 'Keep';
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Remove expense?');
    expect(text).toContain('This cannot be undone.');
    expect(text).toContain('Remove');
    expect(text).toContain('Keep');
  });

  it('should close with true when confirmed', () => {
    component.confirm();

    expect(activeModal.close).toHaveBeenCalledOnceWith(true);
  });

  it('should dismiss when cancelled', () => {
    component.cancel();

    expect(activeModal.dismiss).toHaveBeenCalled();
  });
});
