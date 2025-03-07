import { Component, Input } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './header/header.component';
import { ContentComponent } from "./content/content.component";
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root', 
  standalone: true,
  imports: [HeaderComponent, ContentComponent, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
}
