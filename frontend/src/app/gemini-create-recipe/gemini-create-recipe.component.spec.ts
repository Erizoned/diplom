import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeminiCreateRecipeComponent } from './gemini-create-recipe.component';

describe('GeminiCreateRecipeComponent', () => {
  let component: GeminiCreateRecipeComponent;
  let fixture: ComponentFixture<GeminiCreateRecipeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GeminiCreateRecipeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GeminiCreateRecipeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
