import {Injectable} from "@angular/core";
import {WuzelnService} from "./http/wuzeln.service";
import {PersonalScoreDto, PersonDto} from "./http/httpModel";

@Injectable({providedIn: 'root'})
export class StatService {

  constructor(
    private wuzelnService: WuzelnService) {
  }

  getAllScores(from: Date, to: Date, method: (scores: PersonalScoreDto[], persons: PersonDto[]) => void) {

    this.wuzelnService.findAllPersons().subscribe(persons => {
      const personIds = persons.map(one => one.id);
      this.wuzelnService.getPersonalScoresHelp(personIds, from, to).subscribe(scores => {
        method(scores, persons);
      });
    });
  }

  static getTopPlayers(scores: PersonalScoreDto[]): TopPlayers {

    if (!scores || scores.length == 0) {
      return undefined;
    }

    const topPlayer = scores.reduce((top, one) => one.scoreNormalized > top.scoreNormalized ? one : top, scores[0]);
    const topOffensive = scores.reduce((top, one) => one.scoreOffensiveNormalized > top.scoreOffensiveNormalized ? one : top, scores[0]);
    const topDefensive = scores.reduce((top, one) => one.scoreDefensiveNormalized > top.scoreDefensiveNormalized ? one : top, scores[0]);
    var topOwnGoals = scores.reduce((top, one) => one.goalsOwnSum > top.goalsOwnSum ? one : top, scores[0]);

    if (topOwnGoals.goalsOwnSum > 0) {
      var allWithMaxOwnGoals = scores.filter(one => one.goalsOwnSum == topOwnGoals.goalsOwnSum);

      if (allWithMaxOwnGoals.length > 1) {
        topOwnGoals = allWithMaxOwnGoals.reduce((min, one) => one.goalsSum < min.goalsSum ? one : min, allWithMaxOwnGoals[0]);
        allWithMaxOwnGoals = allWithMaxOwnGoals.filter(one => one.goalsSum == topOwnGoals.goalsSum);

        if (allWithMaxOwnGoals.length > 1) {
          topOwnGoals = allWithMaxOwnGoals.reduce((min, one) => one.scoreDefensiveNormalized < min.scoreDefensiveNormalized ? one : min, allWithMaxOwnGoals[0]);
        }
      }
    } else {
      topOwnGoals = undefined;
    }

    return {
      playerId: topPlayer.id,
      offensiveId: topOffensive.id,
      defensiveId: topDefensive.id,
      ownGoalsId: topOwnGoals ? topOwnGoals.id : undefined,
      playerValue: topPlayer.scoreNormalized,
      offensiveValue: topOffensive.scoreOffensiveNormalized,
      defensiveValue: topDefensive.scoreDefensiveNormalized,
      ownGoalsValue: topOwnGoals ? topOwnGoals.goalsOwnSum : undefined,
    };
  }

}

export interface TopPlayers {
  playerId: number;
  playerValue: number;

  offensiveId: number;
  offensiveValue: number;

  defensiveId: number;
  defensiveValue: number;

  ownGoalsId: number;
  ownGoalsValue: number;
}
