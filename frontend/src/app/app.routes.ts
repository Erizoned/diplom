import { Routes } from '@angular/router';
import { LoginFormComponent } from './login-form/login-form.component';
import { RecipesPageComponent } from './recipes-page/recipes-page.component';
import { RecipeDetailsComponent } from './recipe-details/recipe-details.component';


export const routes: Routes = [  
  { path: 'login', component: LoginFormComponent },
    { path: 'recipes', component: RecipesPageComponent },
    { path: 'recipe/:id', component: RecipeDetailsComponent },
    { path: '**', redirectTo: 'login', pathMatch: 'full' }, // Перенаправляет на /login, если страница не найдена
  ];
