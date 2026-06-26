import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, NgForm, ReactiveFormsModule } from '@angular/forms';
import { NgbModal, NgbPaginationConfig } from '@ng-bootstrap/ng-bootstrap';
import { of, throwError } from 'rxjs';

import { MessageComponent } from './message.component';
import { MessageService } from '../../services/message.service';
import { AuthService } from '../../services/auth.service';
import { Message } from '../../dtos/message';

describe('MessageComponent', () => {
  let component: MessageComponent;
  let fixture: ComponentFixture<MessageComponent>;
  let messageService: jasmine.SpyObj<MessageService>;
  let authService: jasmine.SpyObj<AuthService>;
  let modalService: jasmine.SpyObj<NgbModal>;

  const messages: Message[] = [
    {
      id: 1,
      title: 'House news',
      summary: 'Short update',
      text: 'Full update',
      publishedAt: '2026-06-26T10:00:00.000Z'
    }
  ];

  beforeEach(async () => {
    messageService = jasmine.createSpyObj<MessageService>('MessageService', [
      'getMessage',
      'getMessageById',
      'createMessage'
    ]);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getUserRole']);
    modalService = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);

    messageService.getMessage.and.returnValue(of(messages));
    messageService.getMessageById.and.returnValue(of(messages[0]));
    messageService.createMessage.and.returnValue(of(messages[0]));
    authService.getUserRole.and.returnValue('USER');
    modalService.open.and.returnValue({} as never);

    await TestBed.configureTestingModule({
      declarations: [MessageComponent],
      imports: [FormsModule, ReactiveFormsModule],
      providers: [
        NgbPaginationConfig,
        { provide: MessageService, useValue: messageService },
        { provide: AuthService, useValue: authService },
        { provide: NgbModal, useValue: modalService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load existing messages on init', () => {
    expect(component).toBeTruthy();
    expect(messageService.getMessage).toHaveBeenCalled();
    expect(component.message).toEqual(messages);
  });

  it('should expose admin status from the authenticated role', () => {
    expect(component.isAdmin()).toBeFalse();

    authService.getUserRole.and.returnValue('ADMIN');

    expect(component.isAdmin()).toBeTrue();
  });

  it('should open an empty add-message modal', () => {
    component.currentMessage = {
      id: 99,
      title: 'Old',
      summary: 'Old summary',
      text: 'Old text'
    };

    component.openAddModal({} as never);

    expect(component.currentMessage).toEqual({title: '', summary: '', text: ''});
    expect(modalService.open).toHaveBeenCalledWith({} as never, {ariaLabelledBy: 'modal-basic-title'});
  });

  it('should load an existing message before opening the details modal', () => {
    component.openExistingMessageModal(1, {} as never);

    expect(messageService.getMessageById).toHaveBeenCalledOnceWith(1);
    expect(component.currentMessage).toEqual(messages[0]);
    expect(modalService.open).toHaveBeenCalledWith({} as never, {ariaLabelledBy: 'modal-basic-title'});
  });

  it('should ignore detail modal requests without an id', () => {
    component.openExistingMessageModal(undefined, {} as never);

    expect(messageService.getMessageById).not.toHaveBeenCalled();
    expect(modalService.open).not.toHaveBeenCalled();
  });

  it('should create a valid message and clear the form state', () => {
    component.currentMessage = {
      title: 'New',
      summary: 'Summary',
      text: 'Body'
    };

    component.addMessage({valid: true} as NgForm);

    expect(messageService.createMessage).toHaveBeenCalled();
    const created = messageService.createMessage.calls.mostRecent().args[0];
    expect(created.title).toBe('New');
    expect(created.publishedAt).toBeTruthy();
    expect(messageService.getMessage).toHaveBeenCalledTimes(2);
    expect(component.currentMessage).toEqual({title: '', summary: '', text: ''});
    expect(component.submitted).toBeFalse();
  });

  it('should not create a message when the form is invalid', () => {
    component.addMessage({valid: false} as NgForm);

    expect(component.submitted).toBeTrue();
    expect(messageService.createMessage).not.toHaveBeenCalled();
  });

  it('should show service errors from object or string response bodies', () => {
    messageService.getMessageById.and.returnValue(throwError(() => ({error: {error: 'Object error'}})));

    component.openExistingMessageModal(1, {} as never);

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Object error');

    component.vanishError();
    messageService.createMessage.and.returnValue(throwError(() => ({error: 'String error'})));
    component.currentMessage = {title: 'New', summary: 'Summary', text: 'Body'};

    component.addMessage({valid: true} as NgForm);

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('String error');
    component.vanishError();
    expect(component.error).toBeFalse();
  });
});
