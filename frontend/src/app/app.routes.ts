import { Routes } from '@angular/router';
import { LoginFormComponent } from './login-form/login-form.component';
import { RecipesPageComponent } from './recipes-page/recipes-page.component';


export const routes: Routes = [  
    { path: 'login', component: LoginFormComponent },
    { path: '**', redirectTo: 'login', pathMatch: 'full' }, // Перенаправляет на /login, если страница не найдена
    { path: 'recipes', component: RecipesPageComponent },
  ];
