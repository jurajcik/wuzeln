import {Component, OnInit} from '@angular/core';
import {Location} from "@angular/common";
import {ActivatedRoute} from "@angular/router";
import {WuzelnService} from "../../../services/http/wuzeln.service";
import {MatchDetailedDto, PersonDto, Position, TeamDto} from "../../../services/http/httpModel";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {DialogComponent} from "../../../module/dialog/dialog.component";
import Utils from "../../../utils";

@Component({
  selector: 'app-match-detail',
  templateUrl: './match-detail.component.html',
  styleUrls: ['./match-detail.component.scss']
})
export class MatchDetailComponent implements OnInit {

  match: MatchDetailedDto;
  personsBlue: PersonDto[] = new Array(4);
  personsRed: PersonDto[] = new Array(4);
  ownGoal: boolean;
  lastGoalPerson: PersonDto;

  constructor(
    private route: ActivatedRoute,
    private location: Location,
    private wuzelnService: WuzelnService,
    private dialog: MatDialog
  ) {
  }

  ngOnInit() {
    const matchId = +this.route.snapshot.paramMap.get('id');
    this.loadMatchData(matchId)
  }

  loadMatchData(matchId: number) {
    this.wuzelnService.getMatch(matchId)
      .subscribe(match => {
        this.refreshMatch(match)
      });
  }

  refreshMatch(match: MatchDetailedDto): void {
    this.match = match;
    this.wuzelnService.findAllPersons().subscribe(persons => {
      this.initTeam(this.personsBlue, match.blueTeam, persons);
      this.initTeam(this.personsRed, match.redTeam, persons);
      if (match.lastGoal) {
        this.lastGoalPerson = Utils.getPersonFromPersons(persons, match.lastGoal.playerPersonId)
      } else {
        this.lastGoalPerson = undefined;
      }
    });
  }

  startMatch(): void {
    this.wuzelnService.startMatch(this.match.id).subscribe(() => {
      this.loadMatchData(this.match.id)
    });
  }

  kickGoal(person: PersonDto) {
    this.wuzelnService.kickGoal(this.match.id, person.id, {own: this.ownGoal}).subscribe(
      match => {
        this.refreshMatch(match);
        this.ownGoal = false;
      }
    );
  }

  revertLastGoal() {

    this.wuzelnService.findAllPersons().subscribe(persons => {
      const personId = this.match.lastGoal.playerPersonId;
      const lastGoalPerson = Utils.getPersonFromPersons(persons, personId);
      const own = this.match.lastGoal.own ? "own " : "";

      const dialogConfig = new MatDialogConfig();
      dialogConfig.disableClose = true;
      dialogConfig.autoFocus = true;
      dialogConfig.data = {
        title: "Revert last " + own + "goal from " + lastGoalPerson.name,
        showConfirmButton: true,
        showCloseButton: true
      };

      const dialogRef = this.dialog.open(DialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(
        data => {
          if (data) {
            this.wuzelnService.revertLastGoal(this.match.id).subscribe(
              match => {
                this.refreshMatch(match)
              }
            );
          }
        }
      );
    });
  }

  initTeam(persons: PersonDto[], team: TeamDto, allPersons: PersonDto[]) {
    persons[0] = this.getPlayer(team, 'GOALKEEPER', allPersons);
    persons[1] = this.getPlayer(team, 'DEFENDER', allPersons);
    persons[2] = this.getPlayer(team, 'MIDFIELDER', allPersons);
    persons[3] = this.getPlayer(team, 'FORWARD', allPersons);
  }

  getPlayer(team: TeamDto, position: Position, persons: PersonDto[]): PersonDto {
    const player = team.players.find(player => player.currentPosition === position);
    return player ? Utils.getPersonFromPersons(persons, player.personId) : undefined;
  }

}
