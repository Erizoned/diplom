import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { AxiosService } from '../axios.service';
import { Router } from '@angular/router';

interface Recipe {
  id: number;
  name: string;
  description: string;
  default: boolean;
}

@Component({
  selector: 'app-ingredients-recipe-creator',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule],
  templateUrl: './ingredients-recipe-creator.component.html',
  styleUrls: ['./ingredients-recipe-creator.component.css']
})
export class IngredientsRecipeCreatorComponent {
  ingredients: string = '';
  loading: boolean = false;
  error: string = '';
  recipes: Recipe[] = [];
  loadingMap: { [key: number]: boolean } = {};

  constructor(
    private dialogRef: MatDialogRef<IngredientsRecipeCreatorComponent>,
    private axiosService: AxiosService,
    private router: Router
  ) {}

  createRecipe() {
    if (!this.ingredients.trim()) {
      this.error = 'Пожалуйста, введите ингредиенты';
      return;
    }

    this.loading = true;
    this.error = '';

    const ingredientsList = this.ingredients.split(',').map(name => ({
      name: name.trim()
    }));

    this.axiosService.request('POST', '/api/script/create_recipe_from_ingredients', { ingredients: ingredientsList })
      .then((response) => {
        this.recipes = response.data;
      })
      .catch((error) => {
        this.error = 'Ошибка при создании рецепта';
        console.error(error);
      })
      .finally(() => {
        this.loading = false;
      });
  }

  searchRecipe(recipeName: string) {
    this.dialogRef.close();
    this.router.navigate(['/recipes'], {
      queryParams: {
        keyword: recipeName
      }
    });
  }

  onCreateRecipeFromDefault(recipeName: string, recipeId: number) {
    this.loadingMap[recipeId] = true;
    
    this.axiosService.request('POST', '/api/script/create_recipe', { prompt: recipeName })
      .then((response) => {
        this.dialogRef.close();
        this.router.navigate(['/recipe', response.data.id]);
      })
      .catch((error) => {
        this.error = 'Ошибка при создании рецепта';
        console.error(error);
      })
      .finally(() => {
        this.loadingMap[recipeId] = false;
      });
  }

  close() {
    this.dialogRef.close();
  }
} 