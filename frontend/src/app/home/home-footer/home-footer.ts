import { NgOptimizedImage } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  imports: [NgOptimizedImage, RouterLink],
  selector: 'app-home-footer',
  styleUrl: './home-footer.css',
  templateUrl: './home-footer.html',
})
export class HomeFooter {}
