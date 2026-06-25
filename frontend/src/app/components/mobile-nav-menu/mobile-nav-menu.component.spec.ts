import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { NgbActiveOffcanvas } from '@ng-bootstrap/ng-bootstrap';

import { MobileNavMenuComponent } from './mobile-nav-menu.component';
import { AuthService } from '../../services/auth.service';

describe('MobileNavMenuComponent', () => {
  let component: MobileNavMenuComponent;
  let fixture: ComponentFixture<MobileNavMenuComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let activeOffcanvas: jasmine.SpyObj<NgbActiveOffcanvas>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['logoutUser']);
    activeOffcanvas = jasmine.createSpyObj<NgbActiveOffcanvas>('NgbActiveOffcanvas', ['dismiss', 'close']);

    await TestBed.configureTestingModule({
      declarations: [MobileNavMenuComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: NgbActiveOffcanvas, useValue: activeOffcanvas }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(MobileNavMenuComponent);
    component = fixture.componentInstance;
  });

  it('lists exactly the non-primary nav items', () => {
    expect(component.overflowItems.map(i => i.label)).toEqual([
      'Cleaning', 'Expenses', 'History', 'Statistics', 'My WG', 'Invitations'
    ]);
  });

  it('close() dismisses the sheet', () => {
    component.close();
    expect(activeOffcanvas.dismiss).toHaveBeenCalled();
  });

  it('logout() logs out and dismisses the sheet', () => {
    component.logout();
    expect(authService.logoutUser).toHaveBeenCalled();
    expect(activeOffcanvas.dismiss).toHaveBeenCalled();
  });
});
