import {PersonDto} from "./services/http/httpModel";

export default class Utils {

  public static DATE_FORMAT = 'EE, d.M.yyyy';
  public static DATE_TIME_FORMAT = 'EE, d.M.yyyy, H:mm';
  public static BOOTSTRAP_MD = 768;

  static getPersonFromPersons(persons: PersonDto[], id: number): PersonDto {
    return persons.find(one => one.id === id);
  }

  static getDefaultMatchName(): string {
    var today = new Date();
    return "Daily Stand-Up " + today.getDate() + "." + (today.getMonth() + 1) + ".";
  }

  static handleIncompleteTeams(team: number[]): number[] {

    if (team.length == 3) {
      const teamCopy = team.slice();
      teamCopy.splice(1, 0, undefined);
      return teamCopy;

    } else {
      return team;
    }
  }

  static convertMillisToTime(millis: number): string {
    if (!millis) {
      return ''
    }

    let delim = ' ';
    let result = '';
    let hours = Math.floor(millis / (1000 * 60 * 60) % 60);
    let minutes = Math.floor(millis / (1000 * 60) % 60);
    let seconds = Math.floor(millis / 1000 % 60);

    if (hours > 0) {
      result = hours + 'h' + delim
    }
    return result + minutes + 'm' + delim + seconds + 's';
  }

  static hideColumnsInSmallScreen(columns: string[], columnsToHide: ColumnNameAndPosition[]) {
    const hide = window.innerWidth < Utils.BOOTSTRAP_MD;
    Utils.hideColumns(columns, hide, columnsToHide)
  }

  static hideColumns(columns: string[], hide: boolean, columnsToHide: ColumnNameAndPosition[]) {
    columnsToHide.forEach(column => {
      if (hide) {
        if (columns.includes(column.name)) {
          columns.splice(columns.indexOf(column.name), 1)
        }
      } else {
        if (!columns.includes(column.name)) {
          columns.splice(column.position, 0, column.name)
        }
      }
    })
  }
}

export interface ColumnNameAndPosition {
  position: number,
  name: string
}
