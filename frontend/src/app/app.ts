import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Notice } from './shared/notice/notice';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly notice = inject(Notice);
}
