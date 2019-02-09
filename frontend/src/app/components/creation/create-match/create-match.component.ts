import {Component, Input, OnInit} from '@angular/core';
import {DropEvent} from "ng-drag-drop";
import {WuzelnService} from "../../../services/http/wuzeln.service";
import {MatchCreationDto, MatchCreationMethod, PersonDto} from "../../../services/http/httpModel";
import {Router} from '@angular/router';
import Utils from "../../../utils";
import {ConfigurationService} from "../../../services/configuration.service";

@Component({
  selector: 'app-create-match',
  templateUrl: './create-match.component.html',
  styleUrls: ['./create-match.component.scss']
})
export class CreateMatchComponent implements OnInit {

  @Input()
  matchCreationDto: MatchCreationDto;

  players: PersonDtoView[] = [];
  playersBlue: PersonDtoView[] = new Array(4);
  playersRed: PersonDtoView[] = new Array(4);
  method: MatchCreationMethod;
  registrationId: number;

  name: string;

  constructor(
    private router: Router,
    private wuzelnService: WuzelnService,
    private configurationService: ConfigurationService
  ) {
  }


  ngOnInit() {
    this.name = Utils.getDefaultMatchName();
    this.setDefaultRegIdentifiers();
    this.refresh(this.matchCreationDto);
  }

  refresh(matchCreationDto: MatchCreationDto) {
    this.players.splice(0, this.players.length);

    this.wuzelnService.findAllPersons()
      .subscribe(persons => {

        if (matchCreationDto) {
          this.matchCreationDto = matchCreationDto;
          this.name = matchCreationDto.name;
          this.setUiFromRegistration(persons as PersonDtoView[]);

        } else {
          this.addPersonsToPool(persons);
        }
      });
  }

  setUiFromRegistration(personsInput: PersonDtoView[]) {

    let persons = personsInput.map(x => Object.assign({}, x));

    Utils.handleIncompleteTeams(this.matchCreationDto.teamBlue)
      .forEach((item, index) => {
        this.playersBlue[index] = this.getPersonAndRemove(persons, item);
      });

    Utils.handleIncompleteTeams(this.matchCreationDto.teamRed)
      .forEach((item, index) => {
        this.playersRed[index] = this.getPersonAndRemove(persons, item);
      });

    this.matchCreationDto.idlePersons.forEach((item, index) => {
      const person = this.getPersonAndRemove(persons, item);
      person.canNotPlay = true;
      this.addToPool(person);
    });

    this.addPersonsToPool(persons);
    // could be edited and does not guarantee that it is the same as the registration
    // this.method = this.matchCreationDto.method;
    this.registrationId = this.matchCreationDto.registrationId;
  }

  getPersonAndRemove(persons: PersonDtoView[], id: number): PersonDtoView {
    const person = persons.find(one => one.id === id);
    const index = persons.indexOf(person, 0);
    if (index >= 0) {
      persons.splice(index, 1);
    }
    return person;
  }

  addPersonsToPool(persons: PersonDto[]) {
    persons.forEach(person => {
      const personDtoView = person as PersonDtoView;
      this.addToPool(personDtoView)
    })
  }


  setDefaultRegIdentifiers() {
    this.method = 'MANUAL';
    this.registrationId = null;
  }

  allSlotsAreFull() {
    for (let player of this.playersBlue) {
      if (!player) {
        return false;
      }
    }
    for (let player of this.playersRed) {
      if (!player) {
        return false;
      }
    }
    return true;
  }

  onDropInPool(e: DropEvent) {

    let item = e.dragData;


    if (this.isFromPool(item)) { // from pool
      return

    } else {
      const indexInSource = this.playersBlue.indexOf(item, 0);

      if (indexInSource > -1) { // from blue
        this.playersBlue[indexInSource] = null;

      } else {                  // from red
        const indexInSource = this.playersRed.indexOf(item, 0);
        this.playersRed[indexInSource] = null;
      }
    }

    this.addToPool(item)
  }

  onDragFromPool(target: any, player: PersonDtoView) {
    if (window.screen.width <= 768) {
      setTimeout(() => {
        target.scrollIntoView()
      }, 200)
    }
  }

  onDropInTeam(e: DropEvent, poolElement: any, target: PersonDtoView[], position: number) {

    let item = e.dragData;
    let currentItem = target[position];

    if (currentItem === item) { // placing on itself
      return
    }

    if (this.isFromPool(item)) { // from pool
      CreateMatchComponent.removeIfPresent(item, this.players);
      if (currentItem != null) {
        this.addToPool(currentItem)
      }
      poolElement.scrollIntoView();

    } else {
      const indexInSource = this.playersBlue.indexOf(item, 0);

      if (indexInSource > -1) { // from blue
        this.playersBlue[indexInSource] = currentItem;

      } else {                  // from red
        const indexInSource = this.playersRed.indexOf(item, 0);
        this.playersRed[indexInSource] = currentItem;
      }
    }

    target[position] = item;
  }


  isFromPool(item: PersonDtoView): boolean {
    return typeof this.players.find(x => x === item) != 'undefined';
  }

  addToPool(item: PersonDtoView) {
    this.players.push(item);
    this.players.sort((x1, x2) => {
        return (x1.name < x2.name) ? -1 : 1
    })
  }

  static removeIfPresent(element: any, source: any[]): boolean {
    const index = source.indexOf(element, 0);
    if (index > -1) {
      source.splice(index, 1);
      return true
    }
    return false
  }

  createMatch() {

    if (this.playersBlue.filter(player => player).length < 3 ||
      this.playersRed.filter(player => player).length < 3) {
      return
    }

    const teamBlue = this.playersBlue.filter(p => typeof p != 'undefined').map(p => p.id);
    const teamRed = this.playersRed.filter(p => typeof p != 'undefined').map(p => p.id);
    const idlePersons = this.players.filter(p => p.canNotPlay).map(p => p.id);

    this.wuzelnService.createMatch({
      name: this.name,
      teamBlue: teamBlue,
      teamRed: teamRed,
      idlePersons: idlePersons,
      method: this.method,
      registrationId: this.registrationId
    }).subscribe(
      matchId => {
        this.router.navigate(['matches/' + matchId]);
      }
    )
  }

}

interface PersonDtoView extends PersonDto {
  canNotPlay: boolean
}
