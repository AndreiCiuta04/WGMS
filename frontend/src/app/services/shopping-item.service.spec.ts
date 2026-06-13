import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ShoppingItemService } from './shopping-item.service';
import {
  ShoppingItemCreateDto,
  ShoppingItemDto,
  ShoppingItemStatus,
  ShoppingItemStatusUpdateDto,
  ShoppingItemUpdateDto,
  ShoppingItemVisibility,
  QuantityUnit
} from '../dtos/shopping-item';
import { Globals } from '../global/globals';

describe('ShoppingItemService', () => {
  let service: ShoppingItemService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  const sampleItem: ShoppingItemDto = {
    id: 1, name: 'Milk', quantityCount: null, quantityAmount: 2, quantityUnit: QuantityUnit.LITER, notes: null,
    status: ShoppingItemStatus.NEEDED, visibility: ShoppingItemVisibility.PERSONAL,
    householdId: 10, createdById: 100,
    createdByEmail: 'a@test.com', createdByFirstName: 'A', createdByLastName: 'B',
    targetUserId: null, targetUserEmail: null, targetUserFirstName: null, targetUserLastName: null,
    createdAt: '2026-05-17T10:00:00', updatedAt: '2026-05-17T10:00:00'
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

  it('findTargetedAtMe() should issue GET to /shopping-items/targeted-at-me', () => {
    service.findTargetedAtMe().subscribe(items => expect(items.length).toBe(0));
    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/targeted-at-me');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('findAggregatedHouseholdShoppingList() should issue GET to /shopping-items/aggregated without identity parameters', () => {
    const aggregatedItems = [{
      name: 'Milk',
      quantityCount: null, quantityAmount: 2, quantityUnit: QuantityUnit.LITER,
      itemCount: 2,
      sourceItems: [{
        id: 1,
        name: 'Milk',
        quantityCount: null, quantityAmount: 2, quantityUnit: QuantityUnit.LITER,
        notes: null,
        createdById: 100,
        createdByEmail: 'a@test.com',
        createdByFirstName: 'A',
        createdByLastName: 'B',
        createdAt: '2026-05-17T10:00:00'
      }]
    }];

    service.findAggregatedHouseholdShoppingList().subscribe(items => {
      expect(items).toEqual(aggregatedItems);
      expect(items[0].itemCount).toBe(2);
    });

    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/aggregated');
    expect(req.request.method).toBe('GET');
    expect(req.request.body).toBeNull();
    expect(req.request.params.has('householdId')).toBeFalse();
    expect(req.request.params.has('userId')).toBeFalse();
    expect(req.request.params.has('roommateId')).toBeFalse();
    expect(req.request.params.has('email')).toBeFalse();
    req.flush(aggregatedItems);
  });

  it('findHouseholdShoppingHistory() should issue GET to /shopping-items/history without identity parameters', () => {
    service.findHouseholdShoppingHistory().subscribe(items => {
      expect(items).toEqual([sampleItem]);
      expect(items[0].name).toBe('Milk');
    });

    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/history');
    expect(req.request.method).toBe('GET');
    expect(req.request.body).toBeNull();
    expect(req.request.params.has('householdId')).toBeFalse();
    expect(req.request.params.has('userId')).toBeFalse();
    expect(req.request.params.has('roommateId')).toBeFalse();
    expect(req.request.params.has('email')).toBeFalse();
    req.flush([sampleItem]);
  });

  it('findOne(id) should issue GET to /shopping-items/{id}', () => {
    service.findOne(1).subscribe(item => expect(item.id).toBe(1));
    const req = httpMock.expectOne(globals.backendUri + '/shopping-items/1');
    expect(req.request.method).toBe('GET');
    req.flush(sampleItem);
  });

  it('create() should issue POST with the dto body', () => {
    const dto: ShoppingItemCreateDto = {
      name: 'Bread', quantityAmount: 1, notes: undefined,
      visibility: ShoppingItemVisibility.PERSONAL, targetUserEmail: undefined
    };
    service.create(dto).subscribe(item => expect(item.name).toBe('Milk'));

    const req = httpMock.expectOne(globals.backendUri + '/shopping-items');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(sampleItem);
  });

  it('create() POSTs with visibility + targetUserEmail when TARGETED', () => {
    const dto: ShoppingItemCreateDto = {
      name: 'Bread', visibility: ShoppingItemVisibility.TARGETED,
      targetUserEmail: 'roommate@test.com'
    };
    service.create(dto).subscribe();
    const req = httpMock.expectOne(globals.backendUri + '/shopping-items');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.visibility).toBe(ShoppingItemVisibility.TARGETED);
    expect(req.request.body.targetUserEmail).toBe('roommate@test.com');
    req.flush(sampleItem);
  });

  it('update() should issue PUT to /shopping-items/{id}', () => {
    const dto: ShoppingItemUpdateDto = {
      name: 'Milk 2L', quantityAmount: 2, quantityUnit: QuantityUnit.LITER, notes: 'whole',
      visibility: ShoppingItemVisibility.SHARED, targetUserEmail: undefined
    };
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

  it('surfaces 409 conflict errors', (done) => {
    service.create({ name: 'X', visibility: ShoppingItemVisibility.TARGETED } as any).subscribe({
      next: () => done.fail('expected error'),
      error: (err) => {
        expect(err.status).toBe(409);
        done();
      }
    });
    httpMock.expectOne(globals.backendUri + '/shopping-items').flush(
      { message: 'targetUserEmail is required for TARGETED visibility.' },
      { status: 409, statusText: 'Conflict' }
    );
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
