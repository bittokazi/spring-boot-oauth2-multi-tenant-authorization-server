import { Component, OnInit } from '@angular/core';
import { SweetErrorDialog } from 'src/app/@base/shared/components/alerts/sweet-alert/SweetErrorDialog';
import { SweetSuccessDialog } from 'src/app/@base/shared/components/alerts/sweet-alert/SweetSuccessDialog';
import FormErrorSetter from 'src/app/@base/shared/form-helpers/FormErrorHandler';
import { UpdateMyPasswordError } from 'src/app/@base/shared/form-helpers/user/AddUserFormHelper';
import { UpdateMyPasswordFormHelper } from 'src/app/@base/shared/form-helpers/user/UpdateUserFormHelper';
import { FeatherIconSetterInjector } from 'src/app/@base/shared/js/FeatherIconSetter';
import { User } from 'src/app/@base/shared/models/user/User';
import { AccountSettingsService } from '../account-settings.service';
import { AccountSettingsView } from '../components/interfaces/AccountSettingsView';
import { UpdatePasswordView } from '../components/interfaces/UpdatePasswordView';

@Component({
  selector: 'app-user-security-settings',
  templateUrl: './user-security-settings.component.html',
  styleUrls: ['./user-security-settings.component.css'],
})
export class UserSecuritySettingsComponent
  implements OnInit, AccountSettingsView, UpdatePasswordView
{
  public user: User;
  public form: any;
  public loading: boolean = true;
  public customErrors: any;

  constructor(private accountSettingsService: AccountSettingsService) {
    this.accountSettingsService.accountSettingsView = this;
    this.accountSettingsService.updatePasswordView = this;
    this.customErrors = UpdateMyPasswordError();
  }

  ngOnInit(): void {
    this.accountSettingsService.getUserInfo();
  }

  onSubmit() {
    this.loading = true;
    this.accountSettingsService.updatePassword(this.form.value);
  }

  onUserInfoFetchSuccess(user: User): void {
    this.user = user;
    this.form = UpdateMyPasswordFormHelper(user);
  }

  @SweetErrorDialog({
    title: 'Error',
    text: 'Error Fetching User Info',
  })
  onUserInfoFetchError(error: any): void {
    console.log('UserSecuritySettingsComponent > onUserInfoFetchError', error);
  }

  @SweetSuccessDialog({
    title: 'Success',
    text: 'Successfully updated password',
  })
  onUserPasswordUpateSuccess(user: User): void {}

  @SweetErrorDialog({
    title: 'Error',
    text: 'Error Updating Password',
  })
  onUserPasswordUpdateError(error: any): void {
    if (error.status == 400) FormErrorSetter(this.form, error.error);
    console.log(
      'UserSecuritySettingsComponent > onUserPasswordUpdateError',
      error
    );
  }

  @FeatherIconSetterInjector()
  onLoadComplete(): void {
    this.loading = false;
  }
}
