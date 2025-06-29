import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import axios from 'axios';
import { HeaderComponent } from '../header/header.component';
import { AxiosService } from '../axios.service';
import { MatDialogRef } from '@angular/material/dialog';

interface Recipe {
  id: number;
  name: string;
  description: string;
  photos: { name: string, isPhotoFood?: boolean }[];
  timeToCook: number;
  default: boolean;
}


interface User {
  id: number;
  username: string;
}

interface Diet {
  id: number;
  name: string;
  recipesForBreakfast: Recipe[];
  recipesForLunch: Recipe[];
  recipesForDiner: Recipe[];
  recommendation: string;
  user: User;
  term: string;
  dateOfCreation: string;
}

@Component({
  selector: 'app-diet-page',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './diet-page.component.html',
  styleUrl: './diet-page.component.css'
})
export class DietPageComponent implements OnInit {
  onDeleteDiet() {
    const id = this.route.snapshot.paramMap.get('id');

    this.axiosService.request("DELETE", "/api/diet/delete/" + id, null)
    .then(response => {
      if (response.status === 200) {
        alert("Диета успешно удалена.");
        this.router.navigate(['/recipes']);
      }
    })
    .catch(error => {
      if (error.response && error.response.status === 403) {
        alert(error.response.data);
      } else if (error.response && error.response.status === 404) {
        alert(error.response.data);
      } else {
        alert("Не удалось удалить диету. Пожалуйста, попробуйте снова.");
      }
    });
  }

  loading: boolean = false;
  error: string = '';
  recipeId: number | null = null;
  loadingMap: { [recipeId: number]: boolean } = {};
  dietId: number | null = null;

  onCreateRecipeFromDefault(prompt: string, recipeId: number) {
    if (!prompt.trim()) return;
    this.loadingMap[recipeId] = true;
    this.error = '';
  
    this.axiosService.request('POST', '/api/script/create_recipe', { prompt: prompt })
      .then((response) => {
        this.recipeId = response.data;
        window.location.reload();
        console.log("Рецепт: " + this.recipeId);
      })
      .catch((error) => {
        this.error = 'Ошибка при обработке промпта';
        console.error(error);
      })
      .finally(() => {
        this.loadingMap[recipeId] = false;
      });
  }

  onDeleteRecipe(recipeId: number, recipeName: string, event: MouseEvent) {
    event.stopPropagation();
    event.preventDefault();
    const body = { recipeName };
    this.axiosService.request('POST', `/api/script/diet/${this.dietId}/default/${recipeId}`, body)
      .then(() => {
        window.location.reload();
      })
      .catch(() => {
        alert('Ошибка при удалении рецепта');
      });
  }
  
  diet: Diet | null = null;

  constructor(
    private axiosService: AxiosService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const dietId = params['id'];
      if (dietId) {
        this.loadDiet(dietId);
      }
    });
  }

  loadDiet(dietId: number): void {
    this.axiosService.request("GET", `/api/diet/${dietId}`, null)
      .then(response => {
        this.diet = response.data;
        this.dietId = dietId;
        console.log("Диета загружена.");
      })
      .catch(error => {
        console.error('Ошибка при получении диеты', error);
      });
  }

  getPhotoUrl(photoName: string): string {
   return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(photoName);
  }

  getPhotoFood(recipe: Recipe): string | null {
    if (!recipe.photos || recipe.photos.length === 0) return null;
  
    const photo = recipe.photos.find(p => p.isPhotoFood);
    return photo ? photo.name : null;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
}
