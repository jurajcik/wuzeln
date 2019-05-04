import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {MatSort, MatTableDataSource} from "@angular/material";
import {PersonalScoreDto} from "../../services/http/httpModel";
import {WuzelnService} from "../../services/http/wuzeln.service";
import {animate, state, style, transition, trigger} from "@angular/animations";
import Utils, {ColumnNameAndPosition} from "../../utils";
import {StatService, TopPlayers} from "../../services/stat.service";
import {ConfigurationService} from "../../services/configuration.service";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {OAuthService} from "../../services/oauth.service";

@Component({
  selector: 'app-scores',
  templateUrl: './scores.component.html',
  styleUrls: ['./scores.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('void', style({height: '0px', minHeight: '0', visibility: 'hidden'})),
      state('*', style({height: '*', visibility: 'visible'})),
      transition('void <=> *', animate('100ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class ScoresComponent implements OnInit {

  @ViewChild('sort')
  sort: MatSort;

  hideColumns: ColumnNameAndPosition[] = [{position: 1, name: 'matchesPlayed'}];
  displayedColumns = ['name', 'matchesPlayed', 'scoreIdle', 'scoreOffensive', 'scoreDefensive', 'scoreNormalized'];
  dataSource = new MatTableDataSource<PersonalScoreDtoView>([]);
  scores: PersonalScoreDtoView[];
  isExpansionDetailRow = (index, row) => row.hasOwnProperty('detailRow');

  now: Date;
  minFrom: Date;
  from: Date;
  maxTo: Date;
  to: Date;

  topPlayers: TopPlayers;

  idleScores: Map<number, number>;

  innerWidth: number;

  grafanaDashboardUrl: String;
  grafanaDashboardIndividualUrl: String;

  constructor(
    private wuzelnService: WuzelnService,
    private configurationService: ConfigurationService,
    private oAuthService: OAuthService
  ) {
  }

  ngOnInit() {
    this.now = new Date();
    this.from = new Date();
    this.from.setMonth(this.now.getMonth() - this.configurationService.getConfig().proposalCalculationMonths);
    this.to = this.now;
    this.maxTo = this.now;

    this.dataSource.sort = this.sort;

    Utils.hideColumnsInSmallScreen(this.displayedColumns, this.hideColumns)

    this.refresh();
    this.setGrafanaLinks()
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    Utils.hideColumnsInSmallScreen(this.displayedColumns, this.hideColumns)
  }


  refresh() {

    if (this.from > this.to || this.to > this.now || !this.from || !this.to) {
      return;
    }

    this.from.setHours(0, 0, 0, 0);
    this.to.setHours(23, 59, 59, 999);


    this.wuzelnService.findAllPersons().subscribe(persons => {
      const personIds = persons
        .filter( one => one.active)
        .map(one => one.id);
      this.wuzelnService.getPersonalScoresHelp(personIds, this.from, this.to).subscribe(scores => {

        this.loadIdleScores(personIds, this.from, this.to)
          .subscribe(idleScoreMap => {

            const scoresView = scores.map(one => {
              const view: PersonalScoreDtoView = one as PersonalScoreDtoView;
              view.name = Utils.getPersonFromPersons(persons, one.id).name;
              view.scoreIdle = idleScoreMap.get(one.id)
              return view;
            });

            this.scores = scoresView;
            this.dataSource.data = scoresView;
            this.topPlayers = StatService.getTopPlayers(scores);

          })
      });
    });

  }

  convertMillisToTime = Utils.convertMillisToTime

  loadIdleScores(personIds: number[], from: Date, to: Date): Observable<Map<number, number>> {
    return this.wuzelnService.getIdleScoresHelp(personIds, from, to).pipe(
      map(scores => {
          const map = new Map<number, number>();
          scores.forEach(one => {
            map.set(one.id, one.score)
          });
          return map;
        }
      ))
  }

  setGrafanaLinks() {
    this.grafanaDashboardUrl = this.configurationService.getConfig().grafanaLinkDashboard;

    this.oAuthService.onUserChange()
      .subscribe(userAccount => {
        this.grafanaDashboardIndividualUrl = this.configurationService.getConfig().grafanaLinkDashboardIndividual + userAccount.username;
      })
  }

}

interface PersonalScoreDtoView extends PersonalScoreDto {
  name: string;
  scoreIdle: number;
}
