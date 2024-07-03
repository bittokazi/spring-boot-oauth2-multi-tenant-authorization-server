import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { TenantService } from '../tenant.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { SweetAlartService } from 'src/app/@base/shared/components/alerts/sweet-alert/sweet-alart.service';
import { FileService } from 'src/app/@base/shared/file/file.service';
import { TenantFormInputComponent } from '../components/tenant-form-input/tenant-form-input.component';

@Component({
  selector: 'app-update-tenant',
  templateUrl: './update-tenant.component.html',
  styleUrls: ['./update-tenant.component.css'],
})
export class UpdateTenantComponent implements OnInit {
  public form: any;
  public customErrors: any;
  public loading: Boolean = true;

  @ViewChild('tenantForm') tenantForm: TenantFormInputComponent;

  constructor(
    private tenantService: TenantService,
    private router: Router,
    private sas: SweetAlartService,
    private route: ActivatedRoute,
    private fileService: FileService
  ) {
    this.form = tenantService.generateForm(null);
    this.customErrors = tenantService.generateError();
  }

  ngOnInit(): void {
    this.tenantService
      .getTenant(this.route.snapshot.params.id)
      .then((res) => {
        this.form = this.tenantService.generateForm(res);
      })
      .catch((e) => {
        this.sas.successDialog('Error', 'Error');
      })
      .finally(() => {
        this.loading = false;
      });
  }

  onSubmit(form: FormGroup) {
    this.loading = true;
    if (this.tenantForm.themeFile.nativeElement.files.length > 0) {
      this.fileService.uploadFile(
        this.tenantForm.themeFile.nativeElement.files,
        '.zip',
        5,
        (data) => {
          data.append(
            'uploadObject',
            new Blob(
              [
                JSON.stringify({
                  filename: form.value.companyKey,
                  absoluteFilePath: '',
                }),
              ],
              {
                type: 'application/json',
              }
            )
          );
          this.tenantService
            .uploadTemplate(data)
            .then((res) => {
              form.controls.customTemplateLocation.patchValue(
                form.value.companyKey
              );
              this.updateTenant(form.value);
            })
            .catch((e) => {
              this.sas.successDialog('Error', 'Error uploading template');
            })
            .finally(() => {
              this.loading = false;
            });
        },
        () => {
          this.loading = false;
        }
      );
      return;
    }
    this.updateTenant(this.form.value);
  }

  updateTenant(data: any) {
    this.tenantService
      .updateTenant(data)
      .then((res) => {
        this.sas.successDialog('Success', 'Updated');
        this.router.navigate(['/dashboard/tenants']);
      })
      .catch((e) => {
        this.sas.successDialog('Error', 'Error');
      })
      .finally(() => {
        this.loading = false;
      });
  }
}
