import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';

import { ClientService } from '../../services/client.service';
import { Client } from '../../interfaces/client';

// Standard user@domain.tld format (Validators.email alone accepts "user@host")
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit {

  @ViewChild(FormGroupDirective) formDirective!: FormGroupDirective;

  readonly fieldLabels: { [key: string]: string } = {
    firstName: 'First Name',
    lastName: 'Last Name',
    phone: 'Phone',
    email: 'Email'
  };

  form: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
    email: ['', [Validators.required, Validators.email, Validators.pattern(EMAIL_PATTERN)]]
  });

  errorMessage = '';

  displayedColumns: string[] = ['id', 'firstName', 'lastName', 'phone', 'email', 'actions'];
  dataSource = new MatTableDataSource<Client>([]);

  constructor(private fb: FormBuilder, private clientService: ClientService) { }

  ngOnInit(): void {
    this.getClients();
  }

  onSave(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = this.buildErrorMessage();
      return;
    }
    this.errorMessage = '';

    const client: Client = { id: 0, ...this.form.value };

    // On failure ClientService already alerts the user; the typed data is kept so they can retry
    this.clientService.saveClient(client).subscribe({
      next: () => {
        // resetForm also clears the "submitted" state so fields don't show red
        this.formDirective.resetForm();
        this.getClients();
      },
      error: () => {}
    });
  }

  onDelete(client: Client): void {
    this.clientService.deleteClient(client.id).subscribe({
      next: () => this.getClients(),
      error: () => {}
    });
  }

  getClients(): void {
    this.clientService.getClients().subscribe((data: Client[]) => {
      this.dataSource.data = data;
    });
  }

  private buildErrorMessage(): string {
    const missing: string[] = [];
    const invalid: string[] = [];

    Object.keys(this.fieldLabels).forEach(key => {
      const errors = this.form.get(key)?.errors;
      if (!errors) { return; }
      if (errors['required']) {
        missing.push(this.fieldLabels[key]);
      } else {
        invalid.push(this.fieldLabels[key]);
      }
    });

    const parts: string[] = [];
    if (missing.length) { parts.push(`Required fields missing: ${missing.join(', ')}.`); }
    if (invalid.length) { parts.push(`Invalid format: ${invalid.join(', ')}.`); }
    return parts.join(' ');
  }
}
