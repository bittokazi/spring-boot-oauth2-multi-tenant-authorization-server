import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { AuthService } from 'src/app/@base/authentication/services/auth.service';
import { CloseOpenMenu } from 'src/app/@base/shared/js/DashboardMenuToggle';

@Component({
  selector: 'app-dashboard-verticle-nav-menu',
  templateUrl: './dashboard-verticle-nav-menu.component.html',
  styleUrls: ['./dashboard-verticle-nav-menu.component.css'],
})
export class DashboardVerticleNavMenuComponent implements OnInit {
  public accessMenu: any = [];
  public role: any = {};
  public docuActive: boolean = false;

  constructor(private authService: AuthService, private router: Router) {
    this.router.events.subscribe((val) => this.onRouteChange(val));
  }

  ngOnInit(): void {
    // this.accessMenu = this.authService.userInfo.accessMenu;
    this.accessMenu = this.authService.getMenu();
    if (this.authService.userInfo.user?.roles?.length > 0) {
      this.role = this.authService.userInfo.user.roles[0];
    }
    this.changeActiveMenu(this.router.url);
  }

  onRouteChange(value) {
    if (value instanceof NavigationEnd) {
      if (this.accessMenu) this.changeActiveMenu(value.url);
      CloseOpenMenu();
    }
  }

  changeActiveMenu(url) {
    this.accessMenu.map((menu) => {
      if (menu.path == url) {
        this.docuActive = false;
        menu.active = true;
      } else {
        menu.active = false;
      }
      menu.subMenu.map((subMenu) => {
        if (subMenu.path == url) {
          this.docuActive = false;
          subMenu.active = true;
        } else {
          subMenu.active = false;
        }
      });
    });
  }
}
