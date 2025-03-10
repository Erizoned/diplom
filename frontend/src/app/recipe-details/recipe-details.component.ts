import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import gsap from 'gsap';
import { AxiosService } from '../axios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-recipe-details',
  imports:[FormsModule, CommonModule],
  templateUrl: './recipe-details.component.html',
  styleUrls: ['./recipe-details.component.css']
})
export class RecipeDetailsComponent implements OnInit, AfterViewInit {

  recipe: any = {
    name: '',
    description: '',
    theme: '',
    typeOfFood: '',
    typeOfCook: '',
    restrictions: '',
    countPortion: 0,
    nationalKitchen: '',
    kkal: 0,
    timeToCook: 0,
    photoFood: [],
    ingredients: [],
    steps: [],      
    author: { username: '' } 
  };

  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private axiosService: AxiosService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    this.axiosService.request("GET", "/api/recipe/" + id, null)
    .then(response => {
      const data = response.data;
      this.recipe = data.recipe;
      this.recipe.photoFood = data.photoFood;
      this.recipe.steps = data.steps;
      this.recipe.ingredients = data.ingredients; 
      this.recipe.authorUsername = data.authorUsername;     
      console.log("Рецепт загружен.");
    })
    .catch(error => {
      console.error('Ошибка при получении рецепта', error),
      this.errorMessage = 'Не удалось загрузить рецепт';
    })
  }

  getPhotoUrl(fileName: string): string {
    return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(fileName);
 }

  ngAfterViewInit(): void {
    gsap.from('h1', {
      opacity: 0,
      y: -30,
      duration: 0.8,
      ease: 'power3.out'
    });

    gsap.from('.recipe-description', {
      opacity: 0,
      y: 20,
      duration: 0.8,
      delay: 0.3,
      ease: 'power3.out'
    });

    gsap.from('.photos img', {
      opacity: 0,
      y: 20,
      duration: 0.6,
      stagger: 0.2,
      ease: 'power3.out'
    });

    gsap.from('.details', {
      opacity: 0,
      y: 20,
      duration: 0.8,
      ease: 'power3.out'
    });

    gsap.from('.ingredients p', {
      opacity: 0,
      x: -20,
      duration: 0.5,
      stagger: 0.1,
      ease: 'power3.out'
    });
  }

  onDeleteRecipe(): void {
    const id = this.route.snapshot.paramMap.get('id');

    this.axiosService.request("DELETE", "/api/recipe/" + id + "/delete", null)
    .then(response => {
      if (response.status === 200) {
          alert("Рецепт успешно удален.");
      }
  })
  .catch(error => {
    if (error.response && error.response.status === 403) {
        alert(error.response.data); // "У вас недостаточно прав для удаления рецепта"
    } else if (error.response && error.response.status === 404) {
        alert(error.response.data); // "Рецепт с id: X не найден"
    } else {
        alert("Не удалось удалить рецепт. Пожалуйста, попробуйте снова.");
    }
  });
  }

  onUpdateRecipe(): void {
    const id = this.route.snapshot.paramMap.get('id');

    
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
