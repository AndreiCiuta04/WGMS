import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LegalComponent } from './legal.component';

describe('LegalComponent', () => {
  let fixture: ComponentFixture<LegalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LegalComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(LegalComponent);
    fixture.detectChanges();
  });

  it('should render all GDPR/legal sections', () => {
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('[data-cy="legal-page"]')).toBeTruthy();
    expect(element.querySelector('[data-cy="legal-privacy-section"]')).toBeTruthy();
    expect(element.querySelector('[data-cy="legal-terms-section"]')).toBeTruthy();
    expect(element.querySelector('[data-cy="legal-gdpr-support-section"]')).toBeTruthy();
    expect(element.querySelector('[data-cy="legal-limitations-section"]')).toBeTruthy();
  });

  it('should contain the expected legal keywords', () => {
    const text = fixture.nativeElement.textContent.toLowerCase();

    expect(text).toContain('university demo');
    expect(text).toContain('account data');
    expect(text).toContain('household');
    expect(text).toContain('tasks');
    expect(text).toContain('expenses');
    expect(text).toContain('shopping');
    expect(text).toContain('export');
    expect(text).toContain('not a production service');
    expect(text).toContain('no real money transfers');
  });
});
