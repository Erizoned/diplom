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
  componentToShow: string = "welcome";

  constructor(private axiosService: AxiosService){}

  showComponent(ComponentToShow:string): void {
    this.componentToShow = this.componentToShow
  }
  onLogin(input: any) {
    this.axiosService.request(
      "POST",
      "/login",
      {
        login: input.login,
        password: input.password
      }
    ).then(response => {
      this.axiosService.setAuthToken(response.data.token);
      this.componentToShow = "messages";
    });
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
    ).then(response => {
      this.axiosService.setAuthToken(response.data.token);
      this.componentToShow = "messages";
    });
  }

}
