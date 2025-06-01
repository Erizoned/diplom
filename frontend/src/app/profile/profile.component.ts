import { Component, OnInit } from '@angular/core';
import { AxiosService } from '../axios.service';   
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
interface Recipe {
  id: number;
  name: string;
  description: string;
  avatar?: string;
}

interface Diet {
  id: number;
  recomendation?: string;
  recommendation?: string;
  term?: any;
  dateOfCreation?: any;
}

interface UserProfile {
  username: string;
  email: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,          
  imports: [CommonModule,  RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: UserProfile | null = null;
  recipes: Recipe[] = [];
  diets: Diet[] = [];
  loading: boolean = true;
  error: string = '';

  ngOnInit(): void {
    this.fetchUser();
  }

  constructor(private axiosService: AxiosService) {}

  fetchUser() {
    this.loading = true;
    this.axiosService.request('GET', '/api/user/profile', {})
      .then(response => {
        this.user = {
          username: response.data.username,
          email: response.data.email
        };
        this.recipes = response.data.recipes || [];
        this.diets = response.data.diets || [];
        this.loading = false;
      })
      .catch(error => {
        this.error = 'Ошибка при загрузке профиля';
        this.loading = false;
      });
  }

  getPhotoUrl(avatarPath?: string): string {
    if (!avatarPath) return '/static/images/default-recipe.png';
    // Обрезаем путь до имени файла
    const fileName = avatarPath.split('\\').pop() || avatarPath.split('/').pop();
    return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(fileName || '');
  }
}
