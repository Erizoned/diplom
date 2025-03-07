import { Component } from '@angular/core';
import { AxiosService } from './../axios.service';
import { Router } from '@angular/router';
import { LoginFormComponent } from "../login-form/login-form.component";

@Component({
  selector: 'app-content',
  imports: [LoginFormComponent],
  templateUrl: './content.component.html',
  styleUrls: ['./content.component.css']
})
export class ContentComponent {
  componentToShow: string = "welcome";

  constructor(private axiosService: AxiosService, private router: Router) {}

  onLogin(input: any) {
    this.axiosService.request("POST", "/login", {
      email: input.email,
      password: input.password
    })
    .then(response => {

      if (response.data && response.data.token) {
        console.log("Сохраняем токен:", response.data.token);
        this.axiosService.setAuthToken(response.data.token);
        this.router.navigate(['/recipes']);
      } else {
        console.error("Ошибка: сервер не вернул токен!");
      }
    })
    .catch(error => {
      console.error("Ошибка при входе:", error);
    });
  }

  onRegister(input: any) {
    this.axiosService.request("POST", "/register", {
      login: input.login,
      email: input.email,
      password: input.password,
      matchingPassword: input.password
    })
    .then(response => {
      if (response.data && response.data.token) {
        console.log("Сохраняем токен:", response.data.token);
        this.axiosService.setAuthToken(response.data.token);
      } else {
        console.error("Ошибка: сервер не вернул токен!");
      }
    })
    .catch(error => {
      console.error("Ошибка при регистрации:", error);
    });
  }
}
