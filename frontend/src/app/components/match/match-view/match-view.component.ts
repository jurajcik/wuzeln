import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PersonDto} from "../../../services/http/httpModel";

@Component({
  selector: 'app-match-view',
  templateUrl: './match-view.component.html',
  styleUrls: ['./match-view.component.scss']
})
export class MatchViewComponent implements OnInit {

  @Input()
  personsBlue: PersonDto[];
  @Input()
  personsRed: PersonDto[];
  @Input()
  viewOnly: boolean;

  @Output()
  kicked = new EventEmitter();

  constructor() {
  }

  ngOnInit() {
  }

  kickGoal(person: PersonDto) {
    this.kicked.emit(person)
  }

}
