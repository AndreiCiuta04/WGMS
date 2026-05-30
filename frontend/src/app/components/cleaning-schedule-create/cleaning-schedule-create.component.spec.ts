import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CleaningScheduleCreateComponent } from './cleaning-schedule-create.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';

describe('CleaningScheduleCreateComponent', () => {
  let component: CleaningScheduleCreateComponent;
  let fixture: ComponentFixture<CleaningScheduleCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CleaningScheduleCreateComponent],
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        RouterModule.forRoot([])
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CleaningScheduleCreateComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
