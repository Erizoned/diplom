import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { HeaderComponent } from '../header/header.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AxiosService } from '../axios.service';
import { GeminiSearchComponent } from '../gemini-search/gemini-search.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

gsap.registerPlugin(ScrollTrigger);

export interface Recipe {
photoFood: any;
  id: number;
  name: string;
  description: string;
  timeToCook: number;
  photos?: { name: string }[];
  new?: boolean;
}


@Component({
  selector: 'app-recipes-page',
  imports:[HeaderComponent, FormsModule, CommonModule, RouterModule, MatDialogModule],
  templateUrl: './recipes-page.component.html',
  styleUrls: ['./recipes-page.component.css']
})
export class RecipesPageComponent implements OnInit, AfterViewInit {
  keyword: string = '';
  filter = {
    countPortion: null as number | null,
    kkal: null as number | null,
    timeToCook: null as number | null,
    nationalKitchen: '',
    restrictions: '',
    theme: '',
    typeOfCook: '',
    typeOfFood: ''
  };
  
  recipes: Recipe[] = [];

  filterVisible = false;

  constructor(private router: Router, private axiosService: AxiosService, private dialog: MatDialog) {}

  openGemini() {
    const dialogRef = this.dialog.open(GeminiSearchComponent, {
      width: '500px',
      disableClose: false
    });
  
    dialogRef.afterClosed().subscribe((result: any[]) => {
      if (result && Array.isArray(result)) {
        this.recipes = result;
        console.log('Рецепты из Gemini:', result);
      }
    });
  }
  
  

  ngOnInit(): void {
    this.axiosService.request("GET", "/api/recipes", null)
    .then(response => {
      this.recipes = response.data;
      console.log("Рецепты загружены.");
    })
    .catch(error => {
      console.error('Ошибка при получении рецептов', error);
    });
  }

  getPhotoUrl(fileName: string): string {
     return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(fileName);
  }
  

  ngAfterViewInit(): void {
    // Анимация для секции поиска
    gsap.from(".search-section", {
      duration: 0.8,
      y: -20,
      opacity: 0,
      ease: "power3.out"
    });
    gsap.from(".actions-container", {
      duration: 0.8,
      y: -15,
      opacity: 0,
      delay: 0.2,
      ease: "power3.out"
    });
    gsap.from("h1", {
      duration: 1,
      y: -20,
      opacity: 0,
      delay: 0.4,
      ease: "power3.out"
    });
    gsap.utils.toArray<HTMLElement>('.recipe-card').forEach((card, i) => {
      gsap.from(card, {
        scrollTrigger: {
          trigger: card,
          start: "top bottom-=100",
          toggleActions: "play none none reverse"
        },
        y: 100,
        opacity: 0,
        duration: 0.8,
        delay: i * 0.1,
        ease: "power3.out",
        onComplete: () => {
          card.style.opacity = '1';
          card.style.transform = "translateY(0)";
        }
      });
    });
    
    document.querySelectorAll('.recipe-card').forEach((card) => {
      const cardEl = card as HTMLElement;
      cardEl.addEventListener('mouseenter', () => {
        const img = cardEl.querySelector('img') as HTMLElement | null;
        const details = cardEl.querySelector('.recipe-details') as HTMLElement | null;
        if (img) {
          gsap.to(img, {
            scale: 1.1,
            duration: 0.4,
            ease: "power2.out"
          });
        }
        if (details) {
          gsap.to(details, {
            y: -5,
            duration: 0.3,
            ease: "power2.out"
          });
        }
      });
      cardEl.addEventListener('mouseleave', () => {
        const img = cardEl.querySelector('img') as HTMLElement | null;
        const details = cardEl.querySelector('.recipe-details') as HTMLElement | null;
        if (img) {
          gsap.to(img, {
            scale: 1,
            duration: 0.4,
            ease: "power2.out"
          });
        }
        if (details) {
          gsap.to(details, {
            y: 0,
            duration: 0.3,
            ease: "power2.out"
          });
        }
      });
    });

    document.querySelectorAll('.recipe-card').forEach((card, index) => {
      (card as HTMLElement).style.animationDelay = `${index * 0.1}s`;
    });
    
  }

  toggleDiv(): void {
    this.filterVisible = !this.filterVisible;
    if (this.filterVisible) {
      gsap.fromTo(
        "#filterDiv",
        { height: 0, opacity: 0 },
        { duration: 0.5, height: "auto", opacity: 1, ease: "power3.out" }
      );
    } else {
      gsap.to("#filterDiv", {
        duration: 0.5,
        height: 0,
        opacity: 0,
        ease: "power3.out"
      });
    }
  }


  onFilter(event: Event): void {
    event.preventDefault();
  
    let queryParams = '?';
    
    if (this.keyword) {
      queryParams += 'keyword=' + encodeURIComponent(this.keyword) + '&';
    }
    if (this.filter.countPortion != null) {
      queryParams += 'countPortion=' + encodeURIComponent(this.filter.countPortion) + '&';
    }
    if (this.filter.kkal != null) {
      queryParams += 'kkal=' + encodeURIComponent(this.filter.kkal) + '&';
    }
    if (this.filter.timeToCook != null) {
      queryParams += 'timeToCook=' + encodeURIComponent(this.filter.timeToCook) + '&';
    }
    // if (this.filter.nationalKitchen) {
    //   queryParams += 'nationalKitchen=' + encodeURIComponent(this.filter.nationalKitchen) + '&';
    // }
    if (this.filter.restrictions) {
      queryParams += 'restrictions=' + encodeURIComponent(this.filter.restrictions) + '&';
    }
    if (this.filter.theme) {
      queryParams += 'theme=' + encodeURIComponent(this.filter.theme) + '&';
    }
    if (this.filter.typeOfCook) {
      queryParams += 'typeOfCook=' + encodeURIComponent(this.filter.typeOfCook) + '&';
    }
    if (this.filter.typeOfFood) {
      queryParams += 'typeOfFood=' + encodeURIComponent(this.filter.typeOfFood) + '&';
    }
  
    if (queryParams.endsWith('&')) {
      queryParams = queryParams.slice(0, -1);
    }
  
    this.axiosService.request("GET", "/api/recipes" + queryParams, null)
      .then(response => {
        this.recipes = response.data;
        console.log("Отфильтрованные/найденные рецепты:", this.recipes);
      })
      .catch(error => {
        console.error("Ошибка при фильтрации/поиске:", error);
      });
  }
  
}
