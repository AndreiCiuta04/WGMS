import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ShoppingItemService } from './shopping-item.service';
import {
  ShoppingItemCreateDto,
  ShoppingItemDto,
  ShoppingItemStatus,
  ShoppingItemStatusUpdateDto,
  ShoppingItemUpdateDto
} from '../dtos/shopping-item';
import { Globals } from '../global/globals';

describe('ShoppingItemService', () => {
  let service: ShoppingItemService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  const sampleItem: ShoppingItemDto = {
    id: 1,
    name: 'Milk',
    quantity: '2L',
    notes: undefined,
    status: ShoppingItemStatus.NEEDED,
    householdId: 10,
    createdById: 100,
    createdByEmail: 'a@test.com',
    createdByFirstName: 'A',
    createdByLastName: 'B',
    createdAt: '2026-05-17T10:00:00',
    updatedAt: '2026-05-17T10:00:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ShoppingItemService, Globals]
    });
    service = TestBed.inject(ShoppingItemService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('findAll() should issue GET to /shopping-items', () => {
    service.findAll().subscribe(items => {
      expect(items.length).toBe(1);
      expect(items[0].name).toBe('Milk');
    });

    const req = httpMock.expectOne(globals.backendUri + '/shopping-items');
    expect(req.request.method).toBe('GET');
    req.flush([sampleItem]);
  });

  it('findOne(id) should issue GET to /shopping-items/{id}', () => {
    service.findOne(1).subscribe(item => expect(item.id).toBe(1));
    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/1');
    expect(req.request.method).toBe('GET');
    req.flush(sampleItem);
  });

  it('create() should issue POST with the dto body', () => {
    const dto: ShoppingItemCreateDto = { name: 'Bread', quantity: '1', notes: undefined };
    service.create(dto).subscribe(item => expect(item.name).toBe('Milk'));

    const req = httpMock.expectOne(globals.backendUri + '/shopping-items');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(sampleItem);
  });

  it('update() should issue PUT to /shopping-items/{id}', () => {
    const dto: ShoppingItemUpdateDto = { name: 'Milk 2L', quantity: '2L', notes: 'whole' };
    service.update(1, dto).subscribe();
    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(sampleItem);
  });

  it('updateStatus() should issue PATCH to /shopping-items/{id}/status', () => {
    const dto: ShoppingItemStatusUpdateDto = { status: ShoppingItemStatus.BOUGHT };
    service.updateStatus(1, dto).subscribe();
    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/1/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(dto);
    req.flush({ ...sampleItem, status: ShoppingItemStatus.BOUGHT });
  });

  it('delete() should issue DELETE to /shopping-items/{id}', () => {
    service.delete(1).subscribe();
    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('findAll() should surface backend errors', (done) => {
    service.findAll().subscribe({
      next: () => done.fail('expected error'),
      error: (err) => {
        expect(err.status).toBe(403);
        done();
      }
    });
    httpMock.expectOne(globals.backendUri + '/shopping-items')
      .flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });
  });
});
