import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register',
  imports: [FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {

  @Output() onSubmitRegisterEvent = new EventEmitter();

  login: string = "";
  email: string = "";
  password: string = "";
  matchingPassword: string = "";

  onSubmitRegister(): void {
    this.onSubmitRegisterEvent.emit({"login": this.login, "email": this.login, "password": this.password, "matchingPassword":this.matchingPassword});
  }
}
