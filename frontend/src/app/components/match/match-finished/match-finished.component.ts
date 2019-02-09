import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {MatchBasicStatDto, MatchDetailedDto, PlayerStatDto} from "../../../services/http/httpModel";
import {WuzelnService} from "../../../services/http/wuzeln.service";
import {MatSort, MatTableDataSource} from "@angular/material";
import Utils from "../../../utils";
import {forkJoin} from "rxjs/internal/observable/forkJoin";
import {StatService, TopPlayers} from "../../../services/stat.service";

@Component({
  selector: 'app-match-finished',
  templateUrl: './match-finished.component.html',
  styleUrls: ['./match-finished.component.scss']
})
export class MatchFinishedComponent implements OnInit {

  @Input()
  matchDetail: MatchDetailedDto;

  @ViewChild(MatSort)
  sort: MatSort;

  match: MatchBasicStatDto;
  displayedColumns = ['name', 'teamColor', 'goals', 'goalsOwn', 'inGoalMillis'];
  players = new MatTableDataSource<PlayerStatDtoView>([]);

  dateFormat = Utils.DATE_TIME_FORMAT;
  topPlayers: TopPlayers;
  idlePersonNames: String[] = [];

  scoreWinners: number;
  scoreLoosers: number;

  constructor(
    private wuzelnService: WuzelnService,
    private statService: StatService) {
  }

  ngOnInit(): void {
    this.getMatch(this.matchDetail.id);
    this.players.sort = this.sort;

    this.scoreWinners = this.matchDetail.blueTeam.goals > this.matchDetail.redTeam.goals ? this.matchDetail.blueTeam.goals : this.matchDetail.redTeam.goals;
    this.scoreLoosers = this.matchDetail.blueTeam.goals < this.matchDetail.redTeam.goals ? this.matchDetail.blueTeam.goals : this.matchDetail.redTeam.goals;
  }

  getMatch(matchId: number): void {
    const matchObs = this.wuzelnService.getMatchStats(matchId);
    const personsObs = this.wuzelnService.findAllPersons();

    forkJoin(matchObs, personsObs).subscribe(combined => {
      const [match, persons] = combined;
      this.match = match;
      this.players.data = match.players.map(one => {
          const view = one as PlayerStatDtoView;
          view.name = Utils.getPersonFromPersons(persons, one.personId).name;
          return view;
        }
      );

      match.idlePersons.forEach(personId => {
        const name = Utils.getPersonFromPersons(persons, personId).name;
        this.idlePersonNames.push(name)
      });

      this.wuzelnService.getPersonalScoresHelp(match.players.map(one => one.personId), new Date(match.startDate), new Date(match.startDate)).subscribe(scores => {
        this.topPlayers = StatService.getTopPlayers(scores);
      });
    });

  }

  convertMillisToTime = Utils.convertMillisToTime
}

interface PlayerStatDtoView extends PlayerStatDto {
  name: string;
}
