import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MatchFinishedComponent} from './match-finished.component';

describe('MatchFinishedComponent', () => {
  let component: MatchFinishedComponent;
  let fixture: ComponentFixture<MatchFinishedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchFinishedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchFinishedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
