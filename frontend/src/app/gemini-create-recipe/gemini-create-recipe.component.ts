import { Component, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatDialog } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AxiosService } from '../axios.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-gemini-search',
  standalone: true,
  templateUrl: './gemini-create-recipe.component.html',
  imports: [CommonModule, FormsModule, MatDialogModule],
})
export class GeminiCreateRecipeComponent {
  prompt: string = '';
  recipeId: number | null = null;
  loading: boolean = false;
  error: string = '';

  constructor(
    private dialogRef: MatDialogRef<GeminiCreateRecipeComponent>,
    private axiosService: AxiosService,
    private matDialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit() {
    console.log('Диалог открыт');
  }
  
  sendPrompt() {
    if (!this.prompt.trim()) return;
    this.loading = true;
    this.error = '';
  
    this.axiosService.request('POST', '/api/script/create_recipe', { prompt: this.prompt })
      .then((response) => {
        this.recipeId = response.data;
        console.log("Рецепт: " + this.recipeId);
        this.dialogRef.close(response.recipe); 
        this.router.navigate(['/recipe', this.recipeId]);
      })
      .catch((error) => {
        this.error = 'Ошибка при обработке промпта';
        console.error(error);
        this.loading = false;
      });
  }
  

  close() {
    this.dialogRef.close();
  }
}
