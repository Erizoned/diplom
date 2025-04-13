import { Component, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatDialog } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AxiosService } from '../axios.service';

@Component({
  selector: 'app-gemini-search',
  standalone: true,
  templateUrl: './gemini-search.component.html',
  imports: [CommonModule, FormsModule, MatDialogModule],
})
export class GeminiSearchComponent {
  prompt: string = '';
  recipes: any[] = [];
  loading: boolean = false;
  error: string = '';

  constructor(
    private dialogRef: MatDialogRef<GeminiSearchComponent>,
    private axiosService: AxiosService,
    private matDialog: MatDialog
  ) {}

  ngOnInit() {
    console.log('Диалог открыт');
  }
  
  sendPrompt() {
    if (!this.prompt.trim()) return;
    this.loading = true;
    this.error = '';
    this.recipes = [];
  
    this.axiosService.request('POST', '/api/gemini', { prompt: this.prompt })
      .then((response) => {
        console.log(this.recipes);
        this.dialogRef.close(response.data); 
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
