import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AxiosService } from './../axios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.css']
})
export class LoginFormComponent {
create() {
  this.router.navigate(['/user/registration']);
}
  email: string = "";
  password: string = "";
  error: boolean = false;

  constructor(private axiosService: AxiosService, private router: Router) {}

  onSubmitLogin(): void {
    this.axiosService.request("POST", "/login", {
      email: this.email,
      password: this.password
    })
    .then(response => {
      if (response.data && response.data.token) {
        this.axiosService.setAuthToken(response.data.token);
        this.router.navigate(['/recipes']);
      } else {
        console.error("Ошибка: сервер не вернул токен!");
        this.error = true;
      }
    })
    .catch(error => {
      console.error("Ошибка при входе:", error);
      this.error = true;
    });
  }
}
