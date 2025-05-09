import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DietPageComponent } from './diet-page.component';

describe('DietPageComponent', () => {
  let component: DietPageComponent;
  let fixture: ComponentFixture<DietPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DietPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DietPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
