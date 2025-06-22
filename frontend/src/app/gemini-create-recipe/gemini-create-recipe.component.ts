import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
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
  loading: boolean = false;
  error: string = '';

  constructor(
    private dialogRef: MatDialogRef<GeminiCreateRecipeComponent>,
    private axiosService: AxiosService,
    private router: Router
  ) {}

  ngOnInit() {
    console.log('Диалог открыт');
  }

  sendPrompt() {
    if (!this.prompt.trim()) {
      this.error = 'Пожалуйста, введите запрос';
      return;
    }

    this.loading = true;
    this.error = '';

    this.axiosService
      .request('POST', '/api/script/create_recipe', { prompt: this.prompt })
      .then(response => {
        const id = response.data;
        // Если сервер не вернул валидный ID — ошибка
        if (!id || typeof id !== 'number') {
          this.error = 'Ошибка: рецепт не был создан';
        } else {
          this.dialogRef.close();
          this.router.navigate(['/recipe', id]);
        }
      })
      .catch(error => {
        console.error(error);
        this.error = 'Ошибка при обработке промпта';
      })
      .finally(() => {
        this.loading = false;
      });
  }

  close() {
    this.dialogRef.close();
  }
}
