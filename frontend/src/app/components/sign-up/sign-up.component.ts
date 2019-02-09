import {Component, NgZone, OnInit} from '@angular/core';
import {OAuthService, UserAccount} from "../../services/oauth.service";
import {Router} from "@angular/router";
import {AdministrativeService} from "../../services/http/administrative.service";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {DialogComponent} from "../../module/dialog/dialog.component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ConfigurationService} from "../../services/configuration.service";

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss']
})
export class SignUpComponent implements OnInit {

  fullName: string;
  googleAccountId: string;

  registerForm: FormGroup;

  usernameValidationText: String;

  constructor(
    private zone: NgZone,
    private router: Router,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private oAuthService: OAuthService,
    private administrativeService: AdministrativeService,
    private configurationService: ConfigurationService
  ) {
  }

  ngOnInit() {

    this.usernameValidationText = this.configurationService.getConfig().validationUsernameText;

    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.pattern(this.configurationService.getConfig().validationUsernamePattern)]]
    });


    this.oAuthService.getUserAccount().subscribe((userAccount: UserAccount) => {
      this.fullName = userAccount.givenName + " " + userAccount.familyName;
      this.googleAccountId = userAccount.googleAccountId;

      if (userAccount.registered) {
        this.zone.run(() => {
          this.router.navigate(['/']);
        });
      } else if (userAccount.username) {
        this.matDialog.open(DialogComponent, this.dialogConfig(
          'Confirmation Pending',
          'Your registration have been received. Please, wait for the confirmation by the administrator.'));
      }
    });

  }

  submit() {
    if (this.registerForm.invalid) {
      return;
    }

    this.administrativeService.createUserAccount({username: this.f.username.value})
      .subscribe(number => {
        this.matDialog.open(DialogComponent, this.dialogConfig(
          'Sign up successfully requested!',
          'Your request needs to be approved by the administrator. Please return later.'));
      });
  }

  private dialogConfig(title: string, text: string): MatDialogConfig {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      title: title,
      text: text
    };
    return dialogConfig;
  }

  // convenience getter for easy access to form fields
  get f() {
    return this.registerForm.controls;
  }

}
