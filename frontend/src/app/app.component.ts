import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {MatSpinner} from "@angular/material";
import {OAuthService} from "./services/oauth.service";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  public matSpinner = MatSpinner;
  registered = false;
  admin = false;
  username: String;

  constructor(
    public router: Router,
    public oAuthService: OAuthService,
  ) {
  }

  ngOnInit(): void {
    this.oAuthService.onUserChange()
      .subscribe(userAccount => {
        this.registered = userAccount.registered;
        this.admin = userAccount.admin;
        this.username = userAccount.username;
      })
  }

}
