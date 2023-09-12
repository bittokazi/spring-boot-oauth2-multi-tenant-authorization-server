import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-dashboard-verticle-footer',
  templateUrl: './dashboard-verticle-footer.component.html',
  styleUrls: ['./dashboard-verticle-footer.component.css'],
})
export class DashboardVerticleFooterComponent implements OnInit {
  public domain = environment.http + environment.domain;

  constructor() {}

  ngOnInit(): void {}
}
