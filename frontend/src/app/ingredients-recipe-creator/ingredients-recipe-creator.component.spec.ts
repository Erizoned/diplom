import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IngredientsRecipeCreatorComponent } from './ingredients-recipe-creator.component';

describe('IngredientsRecipeCreatorComponent', () => {
  let component: IngredientsRecipeCreatorComponent;
  let fixture: ComponentFixture<IngredientsRecipeCreatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IngredientsRecipeCreatorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IngredientsRecipeCreatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
