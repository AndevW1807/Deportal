import { NgOptimizedImage } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  imports: [NgOptimizedImage],
  selector: 'app-header',
  styleUrl: './header.css',
  templateUrl: './header.html',
})
export class Header {}
