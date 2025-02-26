import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login-form',
  imports: [FormsModule],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.css'
})
export class LoginFormComponent {

  @Output() onSubmitLoginEvent = new EventEmitter();

  login: string = "";
  password: string = "";

  onSubmitLogin(): void{
    this.onSubmitLoginEvent.emit({"email": this.login, "password": this.password});
  }
}
