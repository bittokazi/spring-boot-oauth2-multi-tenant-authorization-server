import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from 'src/app/@base/authentication/services/auth.service';
import { EventBusService } from 'src/app/@base/shared/event/event-bus.service';
import { UserInfoChange } from 'src/app/@base/shared/event/user/UserInfoChange';
import { UploadObject } from 'src/app/@base/shared/models/file/UploadObject';
import { User } from 'src/app/@base/shared/models/user/User';
import { environment } from 'src/environments/environment';
import { AccountSettingsView } from './components/interfaces/AccountSettingsView';
import { UpdateMyProfileView } from './components/interfaces/UpdateMyProfileView';
import { UpdatePasswordView } from './components/interfaces/UpdatePasswordView';

@Injectable({
  providedIn: 'root',
})
export class AccountSettingsService {
  public accountSettingsView: AccountSettingsView;
  public updateMyProfileView: UpdateMyProfileView;
  public updatePasswordView: UpdatePasswordView;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private eventBusService: EventBusService
  ) {}

  getUserInfo() {
    this.authService
      .checkAuthentication()
      .then((response: any) => {
        this.accountSettingsView.onUserInfoFetchSuccess(response);
      })
      .catch((error: any) => {
        this.accountSettingsView.onUserInfoFetchError(error);
      })
      .finally(() => {
        this.accountSettingsView.onLoadComplete();
      });
  }

  updateMyProfile(user: User) {
    this.http
      .put<User>(`${environment.baseUrl}/api/users/${user.id}`, user)
      .toPromise()
      .then((response) => {
        this.updateMyProfileView.onUserInfoUpateSuccess(response);
        this.eventBusService
          .emit<UserInfoChange>('DashboardVerticleHeaderComponent')
          ?.onInfoChange(response);
      })
      .catch((error) => this.updateMyProfileView.onUserInfoUpdateError(error))
      .finally(() => this.updateMyProfileView.onLoadComplete());
  }

  updatePassword(user: User) {
    this.http
      .put<User>(
        `${environment.baseUrl}/api/users/${user.id}/update/password`,
        user
      )
      .toPromise()
      .then((response) => {
        this.updatePasswordView.onUserPasswordUpateSuccess(response);
      })
      .catch((error) =>
        this.updatePasswordView.onUserPasswordUpdateError(error)
      )
      .finally(() => this.updatePasswordView.onLoadComplete());
  }

  addUserImage(data) {
    this.http
      .post<UploadObject>(`${environment.baseUrl}/api/users/add/image`, data)
      .toPromise()
      .then((response) =>
        this.updateMyProfileView.onUserImageUploadSuccess(response)
      )
      .catch((error) => this.updateMyProfileView.onUserImageUploadError(error))
      .finally(() => this.updateMyProfileView.onLoadComplete());
  }
}
