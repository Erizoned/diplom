import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import gsap from 'gsap';
import { AxiosService } from '../axios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-update-recipe',
  imports: [FormsModule, CommonModule],
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
  // Разрешаем в массиве хранить как File, так и null
  stepPhotos: (File | null)[] = [];
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
        const recipeData = data.recipe ? data.recipe : data;
        
        this.recipe.id = recipeData.id;
        this.recipe.name = recipeData.name;
        this.recipe.description = recipeData.description;
        this.recipe.theme = recipeData.theme;
        this.recipe.countPortion = recipeData.countPortion;
        this.recipe.kkal = recipeData.kkal;
        this.recipe.timeToCook = recipeData.timeToCook;

        // Заполнение списка фото блюда (фильтрация только фото с photoFood === true)
        if (recipeData.photos && recipeData.photos.length > 0) {
          this.recipe.photoFood = recipeData.photos.filter((photo: any) => photo.photoFood);
        } else if (data.photoFood) {
          // Если фото блюда приходит отдельно
          this.recipe.photoFood = [data.photoFood];
        }
        
        // Заполнение шагов приготовления
        const steps = data.steps ? data.steps : recipeData.steps;
        if (steps && steps.length > 0) {
          steps.forEach((s: any) => {
            this.stepDescriptions.push(s.description);
            // Файл фото шага изначально отсутствует (будет добавлен, если пользователь выберет новый)
            this.stepPhotos.push(null);
          });
        }
        
        const ingredients = data.ingredients ? data.ingredients : recipeData.ingredients;
        if (ingredients && ingredients.length > 0) {
          ingredients.forEach((ing: any) => {
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

  trackByIndex(index: number, item: any): number {
    return index;
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
    this.stepPhotos.push(null);
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
      '/api/update_recipe/' + this.recipeId,
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
