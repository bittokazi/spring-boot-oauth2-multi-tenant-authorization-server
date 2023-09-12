import { Component, OnInit } from '@angular/core';
import { ClientService } from '../client.service';
import { SweetAlartService } from 'src/app/@base/shared/components/alerts/sweet-alert/sweet-alart.service';
import { FeatherIconSetterInjector } from 'src/app/@base/shared/js/FeatherIconSetter';

@Component({
  selector: 'app-list-client',
  templateUrl: './list-client.component.html',
  styleUrls: ['./list-client.component.css'],
})
export class ListClientComponent implements OnInit {
  public clients: any[] = [];

  constructor(
    public clientService: ClientService,
    private sas: SweetAlartService
  ) {}

  ngOnInit(): void {
    this.clientService
      .getAll()
      .then((res) => {
        this.clients = res;
      })
      .catch((e) => {
        this.sas.successDialog('Error', 'Error');
      })
      .finally(() => {
        this.onLoad();
      });
  }

  @FeatherIconSetterInjector()
  onLoad() {}
}
