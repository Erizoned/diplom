import { Component } from '@angular/core';

@Component({
  selector: 'app-profile',
  imports: [],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
  user = {
    email: 'example@email.com',
    username: 'username',
    diet: {
      id: 1,
      name: 'Средиземноморская диета'
    }
  };

  recipes = [
    {
      id: 1,
      name: 'Овсяная каша',
      description: 'Полезный завтрак для всей семьи',
      timeToCook: 15,
      photos: [{ name: 'oatmeal.jpg' }]
    },
    {
      id: 2,
      name: 'Салат Цезарь',
      description: 'Классический салат с курицей',
      timeToCook: 20,
      photos: []
    }
  ];

  getPhotoUrl(photoName: string): string {
    return 'http://localhost:8081/file_system?file_name=' + encodeURIComponent(photoName);
  }
}
