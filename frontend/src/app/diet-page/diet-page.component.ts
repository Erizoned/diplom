import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import axios from 'axios';
import { HeaderComponent } from '../header/header.component';
import { AxiosService } from '../axios.service';

interface Recipe {
  id: number;
  name: string;
  description: string;
  photos: { name: string }[];
  timeToCook: number;
}

interface User {
  id: number;
  username: string;
}

interface Diet {
  id: number;
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
  diet: Diet | null = null;

  constructor(
    private axiosService: AxiosService,
    private route: ActivatedRoute
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
        console.log("Диета загружена.");
      })
      .catch(error => {
        console.error('Ошибка при получении диеты', error);
      });
  }

  getPhotoUrl(photoName: string): string {
   return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(photoName);
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
