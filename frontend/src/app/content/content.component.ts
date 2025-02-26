import { AxiosService } from './../axios.service';
import { Component, Input } from '@angular/core';
import { RecipesPageComponent } from "../recipes-page/recipes-page.component";
import { LoginFormComponent } from "../login-form/login-form.component";

@Component({
  selector: 'app-content',
  imports: [LoginFormComponent],
  templateUrl: './content.component.html',
  styleUrl: './content.component.css'
})
export class ContentComponent {

  constructor(private axiosService: AxiosService){}

  onLogin(input: any) {
    this.axiosService.request(
      "POST",
      "/login",
      {
        login: input.login,
        password: input.password
      }
    )
}

  onRegister(input: any){
    this.axiosService.request(
      "POST",
      "/register",
      {
        login: input.login,
        email: input.email,
        password: input.password,
        matchingPassword: input.password
      }
    )
  }

}
