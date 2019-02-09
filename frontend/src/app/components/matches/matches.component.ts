import {Component, OnInit, ViewChild} from '@angular/core';
import {WuzelnService} from "../../services/http/wuzeln.service";
import {MatchDto, PersonDto, RegistrationDto} from "../../services/http/httpModel";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import {Router} from "@angular/router";
import Utils from "../../utils";
import * as moment from "moment";
import {StatService} from "../../services/stat.service";

@Component({
  selector: 'app-matches',
  templateUrl: './matches.component.html',
  styleUrls: ['./matches.component.scss']
})
export class MatchesComponent implements OnInit {

  @ViewChild('matchSort')
  matchSort: MatSort;
  @ViewChild('matchPaginator')
  matchPaginator: MatPaginator;

  matchDisplayedColumns = ['name', 'createdDate', 'stateSort'];
  matchDataSource = new MatTableDataSource<MatchDtoView>([]);
  matches: MatchDtoView[];

  registration: RegistrationDto;

  topPlayer: PersonTopDto;
  topOffensive: PersonTopDto;
  topDefensive: PersonTopDto;
  topOwnGoals: PersonTopDto;

  dateFormat = Utils.DATE_TIME_FORMAT;

  constructor(
    private wuzelnService: WuzelnService,
    private statService: StatService,
    public router: Router) {
  }

  ngOnInit() {
    this.matchDataSource.sort = this.matchSort;
    this.matchDataSource.paginator = this.matchPaginator;

    this.wuzelnService.findAllRegistrations().subscribe(regs => {
      this.registration = regs[0];
    });

    this.wuzelnService.findAllMatches().subscribe(matches => {
      this.matches = matches as MatchDtoView[];
      this.addStateSorting();
      this.matchDataSource.data = this.matches;
    });

    const from = moment().startOf('isoWeek').toDate(); // .add(7, 'days')
    const to = moment().endOf('isoWeek').toDate();

    this.statService.getAllScores(from, to, (scores, persons) => {

      const topPlayers = StatService.getTopPlayers(scores);

      this.topPlayer = this.createTopPerson(topPlayers.playerValue, persons, topPlayers.playerId);
      this.topOffensive = this.createTopPerson(topPlayers.offensiveValue, persons, topPlayers.offensiveId);
      this.topDefensive = this.createTopPerson(topPlayers.defensiveValue, persons, topPlayers.defensiveId);
      this.topOwnGoals = this.createTopPerson(topPlayers.ownGoalsValue, persons, topPlayers.ownGoalsId);

    });

  }

  createTopPerson(score: number, persons: PersonDto[], personId: number,): PersonTopDto {
    return {person: Utils.getPersonFromPersons(persons, personId), score: score}
  }

  addStateSorting() {
    this.matches.forEach(match => {
      switch (match.state) {
        case 'CREATED':
          match.stateSort = 1;
          break;
        case 'STARTED':
          match.stateSort = 2;
          break;
        case 'FINISHED':
          match.stateSort = 3;
          break;
      }
    });
  }

  createRegistration() {
    this.wuzelnService.createRegistration(Utils.getDefaultMatchName()).subscribe(
      regId => {
        this.router.navigate(['registrations/' + regId]);
      }
    )
  }

}

interface MatchDtoView extends MatchDto {
  stateSort: number
}

interface PersonTopDto {
  person: PersonDto;
  score: number;
}
