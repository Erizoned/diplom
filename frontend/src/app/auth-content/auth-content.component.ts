import { CommonModule } from '@angular/common';
import { AxiosService } from './../axios.service';
import { Component } from '@angular/core';

@Component({
  selector: 'app-auth-content',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './auth-content.component.html',
  styleUrl: './auth-content.component.css'
})
export class AuthContentComponent {
// Здесь хранится ответ с сервера
  data: string[] = [];

  constructor(private axiosService: AxiosService) { }

  ngOnInit():void{
    console.log("AuthContentComponent инициализирован");
    this.axiosService.request(
      "GET",
      "/message",
      null
    ).then(
      (response) => this.data = response.data
    )
  }

}
