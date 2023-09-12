import { Component, OnInit } from '@angular/core';
import { DashboardApp } from './../../../shared/js/DashboardApp';
import { DashboardAppMenu } from './../../../shared/js/DashboardAppMenu';
import { DashboardCustom } from './../../../shared/js/DashboardCustom';
import { Collapsed } from './../../../shared/js/Collapsed';

let $ = window['$'];

@Component({
  selector: 'app-dashboard-verticle-light',
  templateUrl: './dashboard-verticle-light.component.html',
  styleUrls: ['./dashboard-verticle-light.component.css'],
})
export class DashboardVerticleLightComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {
    $('body').removeClass('vertical-layout');
    $('body').removeClass('vertical-menu-modern');
    $('body').removeClass('blank-page');
    $('body').removeClass('navbar-floating');
    $('body').removeClass('footer-static');
    $('body').addClass(
      'vertical-layout vertical-menu-modern navbar-floating footer-static'
    );
    $('body').data('menu', 'vertical-menu-modern');
    $('body').data('open', 'click');
    $('body').data('col', '');
    setTimeout(() => {
      DashboardAppMenu();
      DashboardApp();
      DashboardCustom();
      Collapsed();
    }, 200);
  }
}
