import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { TermsComponent } from './terms.component';

describe('TermsComponent', () => {
  let fixture: ComponentFixture<TermsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TermsComponent],
      imports: [RouterTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(TermsComponent);
    fixture.detectChanges();
  });

  it('should render the terms headings', () => {
    const element: HTMLElement = fixture.nativeElement;
    const text = element.textContent || '';
    expect(element.querySelector('[data-cy="terms-page"]')).toBeTruthy();
    expect(text).toContain('Terms and Conditions');
    expect(text).toContain('User responsibilities');
    expect(text).toContain('No production guarantee');
    expect(text).not.toContain('Data we store');
  });
});
