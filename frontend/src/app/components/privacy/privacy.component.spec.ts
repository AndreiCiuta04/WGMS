import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrivacyComponent } from './privacy.component';

describe('PrivacyComponent', () => {
  let fixture: ComponentFixture<PrivacyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrivacyComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(PrivacyComponent);
    fixture.detectChanges();
  });

  it('should render the privacy headings', () => {
    const element: HTMLElement = fixture.nativeElement;
    const text = element.textContent || '';
    expect(element.querySelector('[data-cy="privacy-page"]')).toBeTruthy();
    expect(text).toContain('Privacy and Data Usage');
    expect(text).toContain('Data we store');
    expect(text).toContain('User rights supported in this demo');
    expect(text).not.toContain('User responsibilities');
  });
});
