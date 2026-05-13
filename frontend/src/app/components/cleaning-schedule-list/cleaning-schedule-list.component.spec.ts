import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CleaningScheduleListComponent } from './cleaning-schedule-list.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';

describe('CleaningScheduleListComponent', () => {
  let component: CleaningScheduleListComponent;
  let fixture: ComponentFixture<CleaningScheduleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CleaningScheduleListComponent],
      imports: [
        HttpClientTestingModule,
        RouterModule.forRoot([])
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CleaningScheduleListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
