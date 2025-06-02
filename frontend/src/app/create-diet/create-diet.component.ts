import { Component } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AxiosService } from '../axios.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-gemini-search',
  standalone: true,
  templateUrl: './create-diet.component.html',
  imports: [CommonModule, FormsModule, MatDialogModule]
})
export class CreateDietComponent {
  prompt: string = '';
  loading: boolean = false;
  error: string = '';
  recipes: any[] = []; 

  constructor(
    private dialogRef: MatDialogRef<CreateDietComponent>,
    private axiosService: AxiosService,
    private router: Router
  ) {}

  ngOnInit() {}

  sendPrompt() {
    if (!this.prompt.trim()) return;
    this.loading = true;
    this.error = '';

    this.axiosService
      .request('POST', '/api/script/diet', { prompt: this.prompt })
      .then((response) => {
        const createdDiet = response.data;
        const newDietId = createdDiet.id;
        this.dialogRef.close(createdDiet);
        this.router.navigate(['/diet', newDietId]);
        this.loading = false;
      })
      .catch(() => {
        this.error = 'Ошибка при создании диеты';
        this.loading = false;
      });
  }

  close() {
    this.dialogRef.close();
  }
}
