import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { ShoppingItemScanComponent } from './shopping-item-scan.component';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { ShoppingItemDto, ShoppingItemStatus, ShoppingItemVisibility } from '../../dtos/shopping-item';

describe('ShoppingItemScanComponent', () => {
  let component: ShoppingItemScanComponent;
  let fixture: ComponentFixture<ShoppingItemScanComponent>;
  let shoppingSpy: jasmine.SpyObj<ShoppingItemService>;
  let router: Router;

  const created: ShoppingItemDto = {
    id: 7, name: 'Oat Milk', quantityCount: 2, quantityAmount: 1, quantityUnit: null, notes: null,
    status: ShoppingItemStatus.NEEDED, visibility: ShoppingItemVisibility.SHARED,
    householdId: 10, createdById: 100,
    createdByEmail: 'me@test.com', createdByFirstName: 'Me', createdByLastName: '!',
    targetUserId: null, targetUserEmail: null, targetUserFirstName: null, targetUserLastName: null,
    createdAt: '', updatedAt: ''
  };

  beforeEach(async () => {
    shoppingSpy = jasmine.createSpyObj('ShoppingItemService', ['scan']);

    await TestBed.configureTestingModule({
      declarations: [ShoppingItemScanComponent],
      imports: [RouterTestingModule],
      providers: [{ provide: ShoppingItemService, useValue: shoppingSpy }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingItemScanComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);

    // Don't touch the real camera during tests.
    spyOn(component, 'startCamera').and.resolveTo();
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('submits a pasted payload and navigates to the list on success', fakeAsync(() => {
    shoppingSpy.scan.and.returnValue(of(created));
    const navSpy = spyOn(router, 'navigate');

    component.submitManual('{"type":"wgms.shopping-item","version":1,"name":"Oat Milk"}');

    expect(shoppingSpy.scan).toHaveBeenCalledTimes(1);
    expect(component.state).toBe('success');
    expect(component.createdName).toBe('Oat Milk');

    tick(900);
    expect(navSpy).toHaveBeenCalledWith(['/shopping-items']);
  }));

  it('surfaces a validation error and stays on the page', () => {
    shoppingSpy.scan.and.returnValue(throwError(() => ({ status: 400 })));

    component.submitManual('not-a-wgms-code');

    expect(component.state).toBe('error');
    expect(component.errorMessage).toContain('not a valid WGMS shopping item');
  });

  it('ignores an empty manual payload', () => {
    component.submitManual('   ');
    expect(shoppingSpy.scan).not.toHaveBeenCalled();
  });

  it('rescan() resets state and restarts the camera', () => {
    component.state = 'error';
    component.errorMessage = 'oops';

    component.rescan();

    expect(component.errorMessage).toBeNull();
    expect(component.startCamera).toHaveBeenCalled();
  });
});
