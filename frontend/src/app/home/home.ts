import { NgOptimizedImage } from '@angular/common';
import { Component } from '@angular/core';
import { Header } from './header/header';
import { HomeFacts } from './home-facts/home-facts';
import { HomeFooter } from './home-footer/home-footer';
import { HomeIntro } from './home-intro/home-intro';
import { HomeSports } from './home-sports/home-sports';

@Component({
  imports: [NgOptimizedImage, Header, HomeIntro, HomeFacts, HomeSports, HomeFooter],
  selector: 'app-home',
  styleUrl: './home.css',
  templateUrl: './home.html',
})
export class Home {}
