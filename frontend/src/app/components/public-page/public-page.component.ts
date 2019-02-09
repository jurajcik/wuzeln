import {Component, NgZone, OnInit} from '@angular/core';
import {OAuthService} from "../../services/oauth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-public-page',
  templateUrl: './public-page.component.html',
  styleUrls: ['./public-page.component.scss']
})
export class PublicPageComponent implements OnInit {

  constructor(
    protected oAuthService: OAuthService,
    protected router: Router,
    public zone: NgZone
  ) {
  }

  ngOnInit() {
  }

  authenticate() {
    this.oAuthService.isAuthenticated()
      .subscribe(authenticated => {
          this.oAuthService.getUserAccount()
            .subscribe(userAcount => {

              this.zone.run(() => {
                if (userAcount.registered) {
                  this.router.navigate(['/']);
                } else {
                  this.router.navigate(['/signup']);
                }
              });

            })
        }
      )
  }
}
