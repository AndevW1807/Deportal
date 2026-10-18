import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Court } from './court.model';

@Component({
  selector: 'app-delete-court',
  templateUrl: './delete-court.html',
  styleUrl: './delete-court.css',
})
export class DeleteCourtDialog {
  protected readonly court = inject<Court>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<DeleteCourtDialog, boolean>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
