import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AxiosService } from './../axios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  // Поля формы
  login: string = "";
  email: string = "";
  password: string = "";
  matchingPassword: string = "";

  // Для отображения возможной ошибки
  error: string = "";

  constructor(private axiosService: AxiosService, private router: Router) {}

  onSubmitRegister(): void {
    // Формируем объект с данными пользователя
    const userData = {
      username: this.login,
      email: this.email,
      password: this.password,
      matchingPassword: this.matchingPassword
    };

    // Отправляем POST-запрос на сервер
    this.axiosService.request("POST", "/register", userData)
      .then(response => {
        console.log("Регистрация прошла успешно:", response.data);
        // После успешной регистрации можно перенаправить на страницу логина
        this.router.navigate(['/login']);
      })
      .catch(error => {
        console.error("Ошибка при регистрации:", error);
        this.error = "Произошла ошибка при регистрации. Попробуйте ещё раз.";
      });
  }
}
