import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AxiosService } from './../axios.service';
import { LoginFormComponent } from '../login-form/login-form.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [LoginFormComponent],
  template: `
    <div class="login-container">
      <app-login-form (onSubmitLoginEvent)="onLogin($event)"></app-login-form>
    </div>
  `,
  styleUrls: ['./login-form.component.css']
})
export class LoginPageComponent {
  constructor(private axiosService: AxiosService, private router: Router) {}

  onLogin(input: any): void {
    this.axiosService.request("POST", "/login", {
      email: input.email,
      password: input.password
    })
    .then(response => {
      if (response.data && response.data.token) {
        console.log("Сохраняем токен:", response.data.token);
        this.axiosService.setAuthToken(response.data.token);
        console.log("Перенаправляем на /recipes...");
        this.router.navigate(['/recipes']);
      } else {
        console.error("Ошибка: сервер не вернул токен!");
      }
    })
    .catch(error => {
      console.error("Ошибка при входе:", error);
    });
  }
}
