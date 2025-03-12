import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import gsap from 'gsap';
import { AxiosService } from '../axios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-update-recipe',
  imports:[FormsModule, CommonModule],
  templateUrl: './update-recipe.component.html',
  styleUrls: ['./update-recipe.component.css']
})
export class UpdateRecipeComponent implements OnInit, AfterViewInit {
  recipeId: string | null = null;
  recipe = {
    id: 0,
    name: '',
    description: '',
    theme: '',
    countPortion: 0,
    kkal: 0,
    timeToCook: 0,
    photoFood: [] as any[],
    ingredients: [] as any[],
    steps: [] as any[],
  };
  photoFoodFile: File | null = null;
  stepPhotos: File[] = [];
  stepDescriptions: string[] = [];
  ingredientNames: string[] = [];
  ingredientsCounts: number[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private axiosService: AxiosService
  ) {}

  ngOnInit(): void {
    this.recipeId = this.route.snapshot.paramMap.get('id');
    this.axiosService.request('GET', '/api/recipe/' + this.recipeId, null)
      .then(response => {
        const data = response.data;
        this.recipe.photoFood = data.photoFood;
        this.recipe.id = data.id;
        this.recipe.name = data.name;
        this.recipe.description = data.description;
        this.recipe.theme = data.theme;
        this.recipe.countPortion = data.countPortion;
        this.recipe.kkal = data.kkal;
        this.recipe.timeToCook = data.timeToCook;
        if (data.steps && data.steps.length > 0) {
          data.steps.forEach((s: any) => {
            this.stepDescriptions.push(s.description);
            this.stepPhotos.push(null as any);
          });
        }
        if (data.ingredients && data.ingredients.length > 0) {
          data.ingredients.forEach((ing: any) => {
            this.ingredientNames.push(ing.name);
            this.ingredientsCounts.push(ing.count);
          });
        }
      })
      .catch(error => {
        console.error('Ошибка при загрузке рецепта:', error);
      });
  }

  ngAfterViewInit(): void {
    gsap.to('.update-recipe-container', { duration: 1, opacity: 1, y: 0, ease: 'power3.out' });
  }

  onPhotoFoodSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.photoFoodFile = event.target.files[0];
    }
  }

  getPhotoUrl(fileName: string): string {
    return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(fileName);
 }

  onStepPhotoSelected(event: any, index: number) {
    if (event.target.files && event.target.files.length > 0) {
      this.stepPhotos[index] = event.target.files[0];
    }
  }

  addStep() {
    this.stepDescriptions.push('');
    this.stepPhotos.push(null as any);
  }

  addIngredient() {
    this.ingredientNames.push('');
    this.ingredientsCounts.push(0);
  }

  onUpdateRecipe() {
    if (!this.recipeId) return;
    const formData = new FormData();
    formData.append('id', this.recipeId);
    formData.append('name', this.recipe.name);
    formData.append('description', this.recipe.description);
    formData.append('theme', this.recipe.theme);
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
      'PUT',
      '/update_recipe/' + this.recipeId,
      formData,
    )
    .then(response => {
      this.router.navigate(['/recipes']);
    })
    .catch(error => {
      console.error('Ошибка при обновлении рецепта:', error);
    });
  }
}
