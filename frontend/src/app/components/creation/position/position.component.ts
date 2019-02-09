import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PersonDto} from "../../../services/http/httpModel";
import {DropEvent} from "ng-drag-drop";

@Component({
  selector: 'app-position',
  templateUrl: './position.component.html',
  styleUrls: ['./position.component.scss']
})
export class PositionComponent implements OnInit {
  @Input()
  title: String;
  @Input()
  customClass: String;
  @Input()
  teamArray: Array<PersonDto>;
  @Input()
  position: String;
  @Output()
  onDrop: EventEmitter<any> = new EventEmitter();

  constructor() {
  }

  ngOnInit() {
  }

  onDropListener(e: DropEvent) {
    this.onDrop.emit(e)
  }

}
