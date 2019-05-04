import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {OAuthService} from "../../services/oauth.service";
import {AdministrativeService} from "../../services/http/administrative.service";
import {UserAccountDto} from "../../services/http/httpModel";
import {MatDialog, MatDialogConfig, MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import Utils, {ColumnNameAndPosition} from "../../utils";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ConfigurationService} from "../../services/configuration.service";
import {DialogComponent} from "../../module/dialog/dialog.component";

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {

  @ViewChild('userAccountsSort')
  userAccountsSort: MatSort;
  @ViewChild('userAccountsPaginator')
  userAccountsPaginator: MatPaginator;

  hideColumns: ColumnNameAndPosition[] = [
    {position: 1, name: 'googleAccountId'},
    {position: 2, name: 'createdDate'}];
  userAccountsDisplayedColumns = ['username', 'googleAccountId', 'createdDate', 'registered', 'active', 'admin', 'save'];
  userAccountsDataSource = new MatTableDataSource<UserAccountDto>([]);

  userAccounts: UserAccountDto[] = [];

  dateFormat = Utils.DATE_TIME_FORMAT;

  registerForm: FormGroup;
  usernameValidationText: String;

  constructor(
    private oAuthService: OAuthService,
    private administrativeService: AdministrativeService,
    private configurationService: ConfigurationService,
    private formBuilder: FormBuilder,
    private matDialog: MatDialog
  ) {
  }

  ngOnInit() {

    this.userAccountsDataSource.sort = this.userAccountsSort;
    this.userAccountsDataSource.paginator = this.userAccountsPaginator;

    Utils.hideColumnsInSmallScreen(this.userAccountsDisplayedColumns, this.hideColumns)

    this.loadUserAccounts();

    this.usernameValidationText = this.configurationService.getConfig().validationUsernameText;

    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.pattern(this.configurationService.getConfig().validationUsernamePattern)]]
    });
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: Event) {
    Utils.hideColumnsInSmallScreen(this.userAccountsDisplayedColumns, this.hideColumns)
  }

  loadUserAccounts() {
    this.administrativeService.getUserAccounts()
      .subscribe(userAccounts => {
        this.userAccounts = userAccounts;
        this.userAccountsDataSource.data = this.userAccounts;
      })
  }

  updateUserAccount(userAccount: UserAccountDto) {

    this.administrativeService.updateUserAccount(userAccount.username, userAccount)
      .subscribe(() => {
        this.loadUserAccounts();
      })
  }

  createNewUserAccount() {
    if (this.registerForm.invalid) {
      return;
    }

    this.administrativeService.createUserAccountForOtherUser({username: this.f.username.value})
      .subscribe(number => {
        this.loadUserAccounts();
        this.matDialog.open(DialogComponent, this.dialogConfig(
          'User Created Successfully',
          'The user is activated and ready to play.'));
      });
  }

  private dialogConfig(title: string, text: string): MatDialogConfig {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = false;
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
