import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TenantService {
  constructor(private http: HttpClient) {}

  public addTenant(tenant: any) {
    return this.http
      .post<any>(`${environment.baseUrl}/api/tenants`, tenant)
      .toPromise();
  }

  public getTenants() {
    return this.http.get<any>(`${environment.baseUrl}/api/tenants`).toPromise();
  }

  public getTenant(id: any) {
    return this.http
      .get<any>(`${environment.baseUrl}/api/tenants/${id}`)
      .toPromise();
  }

  public updateTenant(tenant: any) {
    return this.http
      .put<any>(`${environment.baseUrl}/api/tenants/${tenant.id}`, tenant)
      .toPromise();
  }

  generateForm(tenant: any) {
    let form = {
      name: new FormControl(tenant?.name, [
        Validators.required,
        Validators.minLength(1),
      ]),
      companyKey: new FormControl(tenant?.companyKey, [
        Validators.required,
        Validators.minLength(3),
        Validators.pattern('^[a-z0-9_]+'),
      ]),
      domain: new FormControl(tenant?.domain, [
        Validators.required,
        Validators.minLength(5),
      ]),
      enabled: new FormControl(tenant?.enabled, [Validators.required]),
    };
    if (tenant?.id)
      form['id'] = new FormControl(tenant.id, [Validators.required]);
    return new FormGroup(form);
  }

  generateError() {
    return {
      // user: [
      //   {
      //     key: 'exist',
      //     message: 'Username already exist',
      //   },
      // ],
      // email: [
      //   {
      //     key: 'exist',
      //     message: 'Email already exist',
      //   },
      // ],
    };
  }
}
