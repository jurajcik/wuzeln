import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {OAuthService} from "../../services/oauth.service";
import {AdministrativeService} from "../../services/http/administrative.service";
import {UserAccountDto} from "../../services/http/httpModel";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import Utils, {ColumnNameAndPosition} from "../../utils";

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
  userAccountsDisplayedColumns = ['username', 'googleAccountId', 'createdDate', 'registered', 'admin', 'save'];
  userAccountsDataSource = new MatTableDataSource<UserAccountDto>([]);

  userAccounts: UserAccountDto[] = [];

  dateFormat = Utils.DATE_TIME_FORMAT;

  constructor(
    private oAuthService: OAuthService,
    private administrativeService: AdministrativeService
  ) {
  }

  ngOnInit() {

    this.userAccountsDataSource.sort = this.userAccountsSort;
    this.userAccountsDataSource.paginator = this.userAccountsPaginator;

    Utils.hideColumnsInSmallScreen(this.userAccountsDisplayedColumns, this.hideColumns)

    this.loadUserAccounts();
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

    const change = {registered: userAccount.registered, admin: userAccount.admin};

    this.administrativeService.updateUserAccount(userAccount.username, change)
      .subscribe(() => {
        this.loadUserAccounts();
      })
  }

}
