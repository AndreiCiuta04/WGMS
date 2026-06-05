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
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Privacy and Data Usage');
    expect(text).toContain('Data we store');
    expect(text).toContain('User rights supported in this demo');
  });
});
