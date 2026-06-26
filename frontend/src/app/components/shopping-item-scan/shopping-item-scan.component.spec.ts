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

  it('ignores manual payload while already submitting', () => {
    component.state = 'submitting';

    component.submitManual('{"name":"Milk"}');

    expect(shoppingSpy.scan).not.toHaveBeenCalled();
  });

  it('uses backend error messages when scan validation returns one', () => {
    shoppingSpy.scan.and.returnValue(throwError(() => ({ error: { message: 'Duplicate scanned item' } })));

    component.submitManual('{"name":"Milk"}');

    expect(component.state).toBe('error');
    expect(component.errorMessage).toBe('Duplicate scanned item');
  });

  it('maps authentication, household and generic scan errors to safe messages', () => {
    shoppingSpy.scan.and.returnValues(
      throwError(() => ({ status: 401 })),
      throwError(() => ({ status: 403 })),
      throwError(() => ({ status: 500 }))
    );

    component.submitManual('expired-session');
    expect(component.errorMessage).toContain('session has expired');

    component.submitManual('no-household');
    expect(component.errorMessage).toContain('part of a household');

    component.submitManual('server-error');
    expect(component.errorMessage).toContain('Could not add');
  });

  it('rescan() resets state and restarts the camera', () => {
    component.state = 'error';
    component.errorMessage = 'oops';

    component.rescan();

    expect(component.errorMessage).toBeNull();
    expect(component.startCamera).toHaveBeenCalled();
  });

  it('toggleManual() flips the manual fallback visibility', () => {
    expect(component.showManual).toBeFalse();

    component.toggleManual();
    expect(component.showManual).toBeTrue();

    component.toggleManual();
    expect(component.showManual).toBeFalse();
  });

  it('startCamera() exposes manual fallback when no camera API exists', async () => {
    const descriptor = Object.getOwnPropertyDescriptor(navigator, 'mediaDevices');
    Object.defineProperty(navigator, 'mediaDevices', { value: undefined, configurable: true });
    (component.startCamera as jasmine.Spy).and.callThrough();

    await component.startCamera();

    expect(component.cameraError).toContain('No camera is available');
    expect(component.showManual).toBeTrue();

    Object.defineProperty(navigator, 'mediaDevices', descriptor ?? { value: undefined, configurable: true });
  });

  it('startCamera() exposes manual fallback when camera permission is rejected', async () => {
    const descriptor = Object.getOwnPropertyDescriptor(navigator, 'mediaDevices');
    Object.defineProperty(navigator, 'mediaDevices', {
      value: { getUserMedia: () => Promise.reject(new Error('denied')) },
      configurable: true
    });
    (component.startCamera as jasmine.Spy).and.callThrough();

    await component.startCamera();

    expect(component.cameraError).toContain('Camera access was blocked');
    expect(component.showManual).toBeTrue();

    Object.defineProperty(navigator, 'mediaDevices', descriptor ?? { value: undefined, configurable: true });
  });

  it('ngOnDestroy() stops camera tracks and cancels pending animation frame', () => {
    const stop = jasmine.createSpy('stop');
    const cancelSpy = spyOn(window, 'cancelAnimationFrame');
    (component as unknown as { stream: MediaStream | null }).stream = {
      getTracks: () => [{ stop }]
    } as unknown as MediaStream;
    (component as unknown as { rafId: number | null }).rafId = 123;
    component.cameraActive = true;

    component.ngOnDestroy();

    expect(stop).toHaveBeenCalled();
    expect(cancelSpy).toHaveBeenCalledWith(123);
    expect(component.cameraActive).toBeFalse();
  });
});
