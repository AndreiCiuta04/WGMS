import {TestBed} from '@angular/core/testing';

import {MessageService} from './message.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import {Globals} from '../global/globals';
import {Message} from '../dtos/message';

describe('MessageService', () => {
  let service: MessageService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => TestBed.configureTestingModule({
    imports: [RouterTestingModule, ReactiveFormsModule],
    providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
}));

  beforeEach(() => {
    service = TestBed.inject(MessageService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all messages', () => {
    const messages: Message[] = [{ id: 1, title: 'Hello', summary: 'Summary', text: 'Text' } as any];

    service.getMessage().subscribe(result => expect(result).toEqual(messages));

    const req = httpMock.expectOne(`${globals.backendUri}/messages`);
    expect(req.request.method).toBe('GET');
    req.flush(messages);
  });

  it('should fetch one message by id', () => {
    spyOn(console, 'log');
    const message = { id: 7, title: 'Details', summary: 'Summary', text: 'Text' } as any;

    service.getMessageById(7).subscribe(result => expect(result).toEqual(message));

    const req = httpMock.expectOne(`${globals.backendUri}/messages/7`);
    expect(req.request.method).toBe('GET');
    expect(console.log).toHaveBeenCalledWith('Load message details for 7');
    req.flush(message);
  });

  it('should create a message', () => {
    spyOn(console, 'log');
    const message = { title: 'New message', summary: 'Summary', text: 'Text' } as any;

    service.createMessage(message).subscribe(result => expect(result.title).toBe('New message'));

    const req = httpMock.expectOne(`${globals.backendUri}/messages`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe(message);
    expect(console.log).toHaveBeenCalledWith('Create message with title New message');
    req.flush({ id: 2, ...message });
  });
});
