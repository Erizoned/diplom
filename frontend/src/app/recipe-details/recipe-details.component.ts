import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import gsap from 'gsap';
import { AxiosService } from '../axios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Recipe } from '../recipes-page/recipes-page.component';

@Component({
  selector: 'app-recipe-details',
  imports:[FormsModule, CommonModule],
  templateUrl: './recipe-details.component.html',
  styleUrls: ['./recipe-details.component.css']
})
export class RecipeDetailsComponent implements OnInit, AfterViewInit {

  id: string | null = null;
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
    author: { username: '' },
    rating: 0,
  };

  errorMessage: string | null = null;
  comments: Array<any> = [];
  newComment: string = '';
  recipeId: string | null = null;
  currentRating: number = 0;
  previewRating: number = 0;
  showPreview: boolean = false;
  previewPosition: number = 0;
  currentUsername = '';
  role: string = '';
  isAdmin = false;
  isOwner = false;
  openedMenuIndex: number | null = null;
  editingCommentId: number | null = null;
  editingCommentContent: string = '';
  

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private axiosService: AxiosService
  ) {}

  ngOnInit(): void {
    this.recipeId = this.route.snapshot.paramMap.get('id');
    this.fetchCurrentUser();
    this.fetchRecipe();
  }

  fetchCurrentUser() {
    this.axiosService.request('GET', '/api/user/profile', {})
      .then(res => {
        this.currentUsername = res.data.username;
  
        let rolesArr: string[] = [];
        try {
          rolesArr = JSON.parse(res.data.role);
        } catch {
          rolesArr = res.data.role
            .replace(/^\[|\]$/g, '')
            .split(',')
            .map((r: string) => r.trim().replace(/^"|"$/g, ''));
        }
  
        this.role = rolesArr.join(',');
        this.isAdmin = rolesArr.includes('ADMIN');
        console.log('[fetchCurrentUser] currentUsername:', this.currentUsername);
        console.log('[fetchCurrentUser] rolesArr:', rolesArr, '→ isAdmin:', this.isAdmin);
        console.log('---------------------------------------------');
      })
      .catch(err => {
        console.error('[fetchCurrentUser] Ошибка при запросе профиля пользователя:', err);
      });
    
    console.log("Найден пользователь с именем " + this.currentUsername + " и ролью " + this.role);
  }
    

fetchRecipe() {
  if (!this.recipeId) return;
  this.axiosService.request("GET", "/api/recipe/" + this.recipeId, null)
  .then(response => {
    const data = response.data;
    this.recipe = data.recipe;
    this.recipe.photos = data.photoFood;
    this.recipe.steps = data.steps;
    this.recipe.nationalKitchen = data.recipe.nationalKitchen;
    this.recipe.ingredients = data.ingredients;
    this.recipe.authorUsername = data.authorUsername;
    this.comments = data.comments || [];
    this.currentRating = data.rating ? data.rating.rating : 0;
    this.reloadRecipe();
  })
  .catch(error => {
    console.error('Ошибка при получении рецепта', error);
    this.errorMessage = 'Не удалось загрузить рецепт';
  });
}

checkOwnership() {
  console.log('[checkOwnership] currentUsername:', this.currentUsername);
  console.log('[checkOwnership] recipe.authorUsername:', this.recipe.authorUsername);

  this.isOwner = (this.currentUsername === this.recipe.authorUsername);

  console.log('[checkOwnership] → isOwner:', this.isOwner);
}


  onUpdateRecipe() {
    this.router.navigate(['/update_recipe', this.recipeId]);
  }

  getAvatarName(): string | undefined {
    return this.recipe.photoFood?.find((p: any) => p.isPhotoFood)?.name;
  }
  
  getPhotoUrl(fileName: string): string {
    return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(fileName);
 }

 getPhotoFood(): string | null {
  if (!this.recipe.photos || this.recipe.photos.length === 0) {
    return null;
  }
  const photo = this.recipe.photos.find((p: any) => p.isPhotoFood);
  return photo ? photo.name : null;
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
    
    gsap.from('.comment', {
      opacity: 0,
      y: 20,
      duration: 0.6,
      stagger: 0.15,
      delay: 0.5,
      ease: 'power3.out'
    });
  }

addRating(rating: number, recipeId: number): void {
  const roundedRating = Math.round(rating * 2) / 2;
  this.axiosService.request("POST", `/api/recipe/${recipeId}/rating`, { rating: roundedRating })
    .then(() => {
      this.reloadRecipe();
      this.fetchCurrentUser()

    })
    .catch(error => {
      console.error("Ошибка при добавлении рейтинга:", error);
    });
}

private reloadRecipe(): void {
  this.axiosService.request("GET", "/api/recipe/" + this.recipeId, null)
    .then(response => {
      const data = response.data;
      this.recipe = data.recipe;
      this.recipe.photoFood = data.photoFood;
      this.recipe.steps = data.steps;
      this.recipe.ingredients = data.ingredients;
      this.recipe.authorUsername = data.authorUsername;
      this.comments = data.comments || [];
      this.currentRating = data.rating ? data.rating.rating : 0;
      this.checkOwnership();
    })
    .catch(error => {
      console.error('Ошибка при повторной загрузке рецепта', error);
    });
}

  likeComment(commentId: number): void {
    this.axiosService.request("POST", `/api/comment/${commentId}/like`, null)
      .then((response) => {
        const updated = response.data;
        const comment = this.comments.find(c => c.id === commentId);
        if (comment) {
          comment.likes = updated.likes;
          comment.dislikes = updated.dislikes;
          comment.reaction = updated.reaction;
        }
      })
      .catch(error => {
        alert(error.response?.data || "Ошибка при добавлении лайка");
      });
  }
  
  dislikeComment(commentId: number): void {
    this.axiosService.request("POST", `/api/comment/${commentId}/dislike`, null)
      .then((response) => {
        const updated = response.data;
        const comment = this.comments.find(c => c.id === commentId);
        if (comment) {
          comment.likes = updated.likes;
          comment.dislikes = updated.dislikes;
          comment.reaction = updated.reaction;
        }
      })
      .catch(error => {
        alert(error.response?.data || "Ошибка при добавлении дизлайка");
      });
  }
  

  onDeleteRecipe(): void {
    const id = this.route.snapshot.paramMap.get('id');

    this.axiosService.request("DELETE", "/api/recipe/" + id + "/delete", null)
    .then(response => {
      if (response.status === 200) {
        alert("Рецепт успешно удален.");
        this.router.navigate(['/recipes']);
      }
    })
    .catch(error => {
      if (error.response && error.response.status === 403) {
        alert(error.response.data);
      } else if (error.response && error.response.status === 404) {
        alert(error.response.data);
      } else {
        alert("Не удалось удалить рецепт. Пожалуйста, попробуйте снова.");
      }
    });
  }


  goBack(): void {
    this.router.navigate(['/recipes']);
  }

  addComment(): void {
    if (!this.newComment.trim()) {
      return; 
    }

    const id = this.route.snapshot.paramMap.get('id');
    const commentData = {
      content: this.newComment
    };

    this.axiosService.request("POST", `/api/recipe/${id}/comment`, commentData)
      .then(response => {
        if (response.data) {
          this.comments.push(response.data);
        } else {
          // Заглушка
          this.comments.push({
            content: this.newComment,
            likes: 0,
            dislikes: 0,
            author: 'Вы'
          });
        }
        this.newComment = '';
      })
      .catch(error => {
        console.error('Ошибка при отправке комментария', error);
        alert('Не удалось отправить комментарий');
      });
  }

  onRatingHover(event: MouseEvent): void {
    const starsContainer = event.currentTarget as HTMLElement;
    const rect = starsContainer.getBoundingClientRect();
    this.previewPosition = event.clientX - rect.left;
  }
  onStarHover(event: MouseEvent, index: number): void {
    const starEl = (event.target as HTMLElement);
    const rect = starEl.getBoundingClientRect();
    const relativeX = event.clientX - rect.left;
    if (relativeX < rect.width / 2) {
      this.previewRating = index * 2 + 1;
    } else {
      this.previewRating = index * 2 + 2;
    }
    this.showPreview = true;
    const containerRect = (starEl.closest('.rating-container') as HTMLElement).getBoundingClientRect();
    this.previewPosition = rect.left + rect.width / 2 - containerRect.left;
  }
  
  onStarClick(event: MouseEvent, index: number): void {
    const starEl = (event.target as HTMLElement);
    const rect = starEl.getBoundingClientRect();
    const relativeX = event.clientX - rect.left;
    let clickedRating: number;
    if (relativeX < rect.width / 2) {
      clickedRating = index * 2 + 1;
    } else {
      clickedRating = index * 2 + 2;
    }
    this.addRating(clickedRating, this.recipe.id);
  }
  
  onRatingLeave(): void {
    this.showPreview = false;
  }

  toggleCommentMenu(index: number, event: MouseEvent): void {
    event.stopPropagation();
    if (this.openedMenuIndex === index) {
      this.openedMenuIndex = null;
    } else {
      this.openedMenuIndex = index;
    }
  }

  startEditComment(comment: any): void {
    this.editingCommentId = comment.id;
    this.editingCommentContent = comment.content;
    this.openedMenuIndex = null;
  }

  cancelEditComment(): void {
    this.editingCommentId = null;
    this.editingCommentContent = '';
  }

  updateComment(commentId: number): void {
    if (!this.editingCommentContent.trim()) return;
   this.axiosService.request(
  'PUT',
  `/api/comment/update/${commentId}`,
  { content: this.editingCommentContent }
)
      .then(response => {
        const updated = response.data;
        const comment = this.comments.find(c => c.id === commentId);
        if (comment) {
          comment.content = updated.content || this.editingCommentContent;
        }
        this.cancelEditComment();
      })
      .catch(error => {
        alert('Ошибка при обновлении комментария');
      });
  }

  deleteComment(commentId: number): void {
    if (!confirm('Удалить комментарий?')) return;
    this.axiosService.request('DELETE', `/api/comment/delete/${commentId}`, null)
      .then(() => {
        this.comments = this.comments.filter(c => c.id !== commentId);
      })
      .catch(error => {
        alert('Ошибка при удалении комментария');
      });
  }

}
