import { Routes } from '@angular/router';
import { LoginFormComponent } from './login-form/login-form.component';
import { RecipesPageComponent } from './recipes-page/recipes-page.component';
import { RecipeDetailsComponent } from './recipe-details/recipe-details.component';
import { UpdateRecipeComponent } from './update-recipe/update-recipe.component';
import { RegisterComponent } from './register/register.component';
import { CreateRecipeComponent } from './create-recipe-form/create-recipe-form.component';


export const routes: Routes = [  
  { path: 'login', component: LoginFormComponent },
    { path: 'recipes', component: RecipesPageComponent },
    { path: 'recipe/:id', component: RecipeDetailsComponent },
    { path: 'update_recipe/:id', component: UpdateRecipeComponent },
    { path: 'user/registration', component: RegisterComponent},
    { path: 'create_recipe', component: CreateRecipeComponent},
    { path: '**', redirectTo: 'login', pathMatch: 'full' }, // Перенаправляет на /login, если страница не найдена
  ];
