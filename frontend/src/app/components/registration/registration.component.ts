import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {WuzelnService} from "../../services/http/wuzeln.service";
import {
  MatchCreationDto,
  MatchCreationMethod,
  PersonDto,
  RegistrationDto,
  RegPersonDto
} from "../../services/http/httpModel";
import {forkJoin} from "rxjs/internal/observable/forkJoin";
import {MatSlideToggleChange} from "@angular/material";
import {CreateMatchComponent} from "../creation/create-match/create-match.component";
import Utils from "../../utils";
import {ConfigurationService} from "../../services/configuration.service";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit {

  minPlayers = 6;
  persons: PersonDtoView[] = [];
  registration: RegistrationDto;
  allSelected = false;
  edit = false;
  proposalCalculationMonths: number;

  matchCreationDto: MatchCreationDto;

  personsBlue: PersonDto[] = new Array(4);
  personsRed: PersonDto[] = new Array(4);
  personsIdle: PersonDto[] = [];

  @ViewChild(CreateMatchComponent)
  createMatchComponent: CreateMatchComponent;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private wuzelnService: WuzelnService,
    private configurationService: ConfigurationService
  ) {
  }

  ngOnInit() {

    this.proposalCalculationMonths = this.configurationService.getConfig().proposalCalculationMonths;

    const regId = +this.route.snapshot.paramMap.get('id');

    const regObs = this.wuzelnService.getRegistration(regId);
    const personsObs = this.wuzelnService.findAllPersons();

    forkJoin(regObs, personsObs).subscribe(combined => {
      const [reg, persons] = combined;

      this.persons = persons
        .filter(one => one.active)
        .sort((a, b) => (a.name > b.name) ? 1 : -1) as PersonDtoView[];
      this.refreshRegistration(reg)
    });
  }

  refreshRegistration(reg: RegistrationDto) {
    this.registration = reg;
    let selected = true;
    this.persons.forEach(one => {
      const found = reg.persons.find(person => person === one.id);
      if (found) {
        one.registered = true;
      } else {
        one.registered = false;
      }
      selected = selected && one.registered;
    });
    this.allSelected = selected;
  }

  updateRegistration(event: MatSlideToggleChange, person: PersonDtoView) {
    const persons = [{id: person.id, register: event.checked}];
    this.updateRegistrationExecute(null, persons);
    this.matchCreationDto = undefined;
  }

  updateRegistrationAll(event: MatSlideToggleChange) {
    const persons: RegPersonDto[] = this.persons.map(one => {
      return {id: one.id, register: event.checked}
    });
    this.updateRegistrationExecute(null, persons);
    this.matchCreationDto = undefined;
  }

  updateName() {
    this.updateRegistrationExecute(this.registration.name, []);
  }

  updateRegistrationExecute(name: string, persons: RegPersonDto[]) {
    this.wuzelnService.updateRegistration(this.registration.id, {
      name: name,
      persons: persons
    }).subscribe(reg => {
      this.refreshRegistration(reg)
    })
  }

  disableGenerateButton(): boolean {
    return this.persons.filter(one => one.registered).length < this.minPlayers
  }

  generateMatch(method: MatchCreationMethod) {
    this.wuzelnService.getProposal(this.registration.id, method).subscribe(match => {

      this.matchCreationDto = match;

      this.initTeamPersons(this.personsBlue, match.teamBlue);
      this.initTeamPersons(this.personsRed, match.teamRed);
      this.personsIdle = match.idlePersons
        .map(one => Utils.getPersonFromPersons(this.persons, one))
        .sort((a, b) => a.name.localeCompare(b.name));

      this.createMatchComponent.refresh(match)
    })
  }

  initTeamPersons(persons: PersonDto[], team: number[]) {
    Utils.handleIncompleteTeams(team).forEach((item, index) => {
      persons[index] = Utils.getPersonFromPersons(this.persons, item)
    });
  }

  createMatch() {
    this.wuzelnService.createMatch(this.matchCreationDto).subscribe(
      matchId => {
        this.router.navigate(['matches/' + matchId]);
      }
    )
  }
}


interface PersonDtoView extends PersonDto {
  registered: boolean
}
