import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.scss']
})
export class DialogComponent implements OnInit {

  showCloseButton: boolean = false;
  showConfirmButton: boolean = false;

  title: string;
  text: string;

  constructor(
    private dialogRef: MatDialogRef<DialogComponent>,
    @Inject(MAT_DIALOG_DATA) data
  ) {
    this.title = data.title;
    this.text = data.text;
    if(data.showCloseButton){
      this.showCloseButton = true;
    }
    if(data.showConfirmButton){
      this.showConfirmButton = true;
    }
  }

  ngOnInit() {

  }

  confirm() {
    this.dialogRef.close(true);
  }

  close() {
    this.dialogRef.close(false);
  }

}
