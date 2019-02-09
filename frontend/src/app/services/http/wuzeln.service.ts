import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';

import {
  ConfigurationDto,
  GoalCreationDto,
  IdleScoreDto,
  MatchBasicStatDto,
  MatchCreationDto,
  MatchCreationMethod,
  MatchDetailedDto,
  MatchDto,
  PersonalScoreDto,
  PersonDto,
  RegistrationDto,
  RegistrationUpdateDto,
  WuzelnController
} from './httpModel';
import {Observable, of} from 'rxjs';
import {map, share} from "rxjs/operators";
import {Constants} from "./constants";

@Injectable({
  providedIn: 'root',
})
export class WuzelnService implements WuzelnController {

  personsObs: Observable<PersonDto[]>;
  persons: PersonDto[];

  constructor(private http: HttpClient) {
  }

  createMatch(arg0: MatchCreationDto): Observable<number> {
    return this.http.post<number>(Constants.PATH_MATCHES, arg0)
  }

  findAllMatches(): Observable<MatchDto[]> {
    return this.http.get<MatchDto[]>(Constants.PATH_MATCHES);
  }

  getMatch(matchId: number): Observable<MatchDetailedDto> {
    return this.http.get<MatchDetailedDto>(Constants.PATH_MATCHES + matchId);
  }

  findAllPersons(): Observable<PersonDto[]> {
    this.http.get<PersonDto[]>(Constants.PATH_PERSONS);

    if (this.persons) {
      return of(this.persons);

    } else if (this.personsObs) {
      return this.personsObs;

    } else {
      this.personsObs = this.http.get<PersonDto[]>(Constants.PATH_PERSONS).pipe(
        map(persons => {
          this.persons = persons;
          return persons
        }),
        share()
      );
      return this.personsObs;
    }
  }

  getMatchStats(matchId: number): Observable<MatchBasicStatDto> {
    return this.http.get<MatchBasicStatDto>(Constants.PATH_MATCHES + matchId + '/statistics');
  }


  kickGoal(matchId: number, personId: number, goal: GoalCreationDto): Observable<MatchDetailedDto> {
    return this.http.post<MatchDetailedDto>(Constants.PATH_MATCHES + matchId + '/persons/' + personId + '/goal', goal);
  }

  revertLastGoal(matchId: number): Observable<MatchDetailedDto> {
    return this.http.delete<MatchDetailedDto>(Constants.PATH_MATCHES + matchId + '/goals/last');
  }

  startMatch(matchId: number): Observable<void> {
    return this.http.post<void>(Constants.PATH_MATCHES + matchId + '/start', null);
  }

  createRegistration(name: string): Observable<number> {
    return this.http.post<number>(Constants.PATH_REGISTRATIONS, name);
  }

  findAllRegistrations(): Observable<RegistrationDto[]> {
    return this.http.get<RegistrationDto[]>(Constants.PATH_REGISTRATIONS);
  }

  getRegistration(registrationId: number): Observable<RegistrationDto> {
    return this.http.get<RegistrationDto>(Constants.PATH_REGISTRATIONS + registrationId);
  }

  updateRegistration(registrationId: number, arg1: RegistrationUpdateDto): Observable<RegistrationDto> {
    return this.http.put<RegistrationDto>(Constants.PATH_REGISTRATIONS + registrationId, arg1);
  }

  getPersonalScoresHelp(personIds: number[], from: Date, to: Date): Observable<PersonalScoreDto[]> {
    return this.getPersonalScores({
      personId: personIds,
      from: from.getTime(),
      to: to.getTime()
    })
  }

  getPersonalScores(queryParams?: { personId?: number[]; from?: number; to?: number }): Observable<PersonalScoreDto[]> {
    let params = this.mapRange(queryParams);
    return this.http.get<PersonalScoreDto[]>(Constants.PATH_PERSONS + 'scores', {params: params});
  }

  getProposal(registrationId: number, method: MatchCreationMethod): Observable<MatchCreationDto> {
    return this.http.get<MatchCreationDto>(Constants.PATH_REGISTRATIONS + registrationId + '/proposals/' + method);
  }

  getConfiguration(): Observable<ConfigurationDto> {
    return this.http.get<ConfigurationDto>(Constants.PATH_CONFIGURATION);
  }

  getIdleScoresHelp(personIds: number[], from: Date, to: Date): Observable<IdleScoreDto[]> {
    return this.getIdleScores({
      personId: personIds,
      from: from.getTime(),
      to: to.getTime()
    })
  }

  getIdleScores(queryParams?: { personId?: number[]; from?: number; to?: number }): Observable<IdleScoreDto[]> {
    let params = this.mapRange(queryParams);
    return this.http.get<IdleScoreDto[]>(Constants.PATH_PERSONS + 'idleScore', {params: params});
  }


  private mapRange(queryParams?: { personId?: number[]; from?: number; to?: number }): HttpParams{
    let params = new HttpParams()
      .set('from', String(queryParams.from))
      .set('to', String(queryParams.to));

    queryParams.personId.forEach(one => {
      params = params.append('personId', String(one))
    });

    return params;
  }
}
