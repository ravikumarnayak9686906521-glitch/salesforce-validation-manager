import { Component } from '@angular/core';
import { SalesforceService } from '../../services/salesforce.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  constructor(private salesforceService: SalesforceService) {}

  login() {
    this.salesforceService.getAuthUrl().subscribe(response => {
      window.location.href = response.authUrl;
    });
  }
}
