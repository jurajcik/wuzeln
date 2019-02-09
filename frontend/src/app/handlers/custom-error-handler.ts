import {ErrorHandler, Injectable, NgZone} from "@angular/core";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {DialogComponent} from "../module/dialog/dialog.component";
import {Observable, throwError} from "rxjs";
import {HttpErrorResponse} from "@angular/common/http";

@Injectable()
export class CustomErrorHandler implements ErrorHandler {

  constructor(
    private matDialog: MatDialog,
    private zone: NgZone
  ) {
  }

  handleError(error) {

    var title;
    var text;

    if (error instanceof HttpErrorResponse) {
      title = error.statusText;
      text = error.error;

    } else if (error instanceof CustomError) {
      title = error.message;
      text = JSON.stringify(error.detail);

    } else if (error.rejection) {
      this.handleError(error.rejection);
      console.error(error);
      return;

    } else if (error instanceof Error) {
      title = 'Unexpected error';
      text = error.message

    } else {
      title = 'Unexpected error';
      text = error;
    }

    this.zone.run(() => {
      this.matDialog.open(DialogComponent, this.dialogConfig(title, text));
    });

    console.error(error)
  }

  private dialogConfig(title: string, text: string): MatDialogConfig {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      showConfirmButton: false,
      showCloseButton: true,
      title: title,
      text: text
    };
    return dialogConfig;
  }
}

export function throwCustomError(message: string, detail ?: string): Observable<never> {
  return throwError(new CustomError(message, detail))
}

class CustomError {
  constructor(
    public message: string,
    public detail: string
  ) {
  }

}
