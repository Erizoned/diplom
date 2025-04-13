import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import gsap from 'gsap';
import { AxiosService } from '../axios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-create-recipe',
  imports: [FormsModule, CommonModule],
  templateUrl: './create-recipe-form.component.html',
  styleUrls: ['./create-recipe-form.component.css']
})
export class CreateRecipeComponent implements OnInit, AfterViewInit {
  recipe = {
    name: '',
    description: '',
    theme: '',
    typeOfFood: '',
    typeOfCook: '',
    restrictions: '',
    countPortion: 0,
    kkal: 0,
    timeToCook: 0,
    photoFood: [] as any[]
  };

  photoFoodFile: File | null = null;
  stepPhotos: (File | null)[] = [];
  stepDescriptions: string[] = [];
  ingredientNames: string[] = [];
  ingredientsCounts: number[] = [];

  constructor(
    private router: Router,
    private axiosService: AxiosService
  ) {}

  trackByIndex(index: number, obj: any): any {
    return index;
  }
  

  ngOnInit(): void {
    this.addIngredient();
    this.addStep();
  }

  ngAfterViewInit(): void {
    gsap.to('.create-recipe-container', {
      duration: 1,
      opacity: 1,
      y: 0,
      ease: 'power3.out'
    });
  }

  onPhotoFoodSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.photoFoodFile = event.target.files[0];
    }
  }

  onStepPhotoSelected(event: any, index: number) {
    if (event.target.files && event.target.files.length > 0) {
      this.stepPhotos[index] = event.target.files[0];
    }
  }

  addStep() {
    this.stepDescriptions.push('');
    this.stepPhotos.push(null);
  }

  addIngredient() {
    this.ingredientNames.push('');
    this.ingredientsCounts.push(0);
  }

  onCreateRecipe() {
    const formData = new FormData();
    formData.append('name', this.recipe.name);
    formData.append('description', this.recipe.description);
    formData.append('theme', this.recipe.theme);
    formData.append('typeOfFood', this.recipe.typeOfFood);
    formData.append('typeOfCook', this.recipe.typeOfCook);
    formData.append('restrictions', this.recipe.restrictions);
    formData.append('countPortion', this.recipe.countPortion.toString());
    formData.append('kkal', this.recipe.kkal.toString());
    formData.append('timeToCook', this.recipe.timeToCook.toString());

    if (this.photoFoodFile) {
      formData.append('photoFood', this.photoFoodFile);
    }

    this.stepDescriptions.forEach(desc => {
      formData.append('stepDescriptions', desc);
    });
    this.stepPhotos.forEach(file => {
      if (file) {
        formData.append('stepPhotos', file);
      } else {
        formData.append('stepPhotos', new Blob([]), '');
      }
    });

    this.ingredientNames.forEach(name => {
      formData.append('ingredientNames', name);
    });
    this.ingredientsCounts.forEach(count => {
      formData.append('ingredientsCounts', count.toString());
    });

    this.axiosService.request(
      'POST',
      '/api/create_recipe',
      formData
    )
    .then(() => {
      this.router.navigate(['/recipes']);
    })
    .catch(error => {
      console.error('Ошибка при создании рецепта:', error);
    });
  }
}
