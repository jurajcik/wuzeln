import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateMatchPageComponent} from './create-match-page.component';

describe('CreateMatchPageComponent', () => {
  let component: CreateMatchPageComponent;
  let fixture: ComponentFixture<CreateMatchPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateMatchPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateMatchPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
