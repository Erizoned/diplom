import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeminiSearchComponent } from './gemini-search.component';

describe('GeminiChatComponent', () => {
  let component: GeminiSearchComponent;
  let fixture: ComponentFixture<GeminiSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GeminiSearchComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GeminiSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
