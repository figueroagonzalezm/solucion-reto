import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { of, throwError } from 'rxjs';

import { RegistrationComponent } from './registration.component';
import { ClientService } from '../../services/client.service';
import { Client } from '../../interfaces/client';

describe('RegistrationComponent', () => {
  let component: RegistrationComponent;
  let fixture: ComponentFixture<RegistrationComponent>;
  let service: jasmine.SpyObj<ClientService>;

  const existing: Client[] = [
    { id: 1, firstName: 'Ann', lastName: 'Lee', phone: '5551234', email: 'ann@example.com' }
  ];

  const el = (): HTMLElement => fixture.nativeElement;
  const submit = () => {
    (el().querySelector('button[type=submit]') as HTMLButtonElement).click();
    fixture.detectChanges();
  };
  const fillValid = () =>
    component.form.setValue({
      firstName: 'Ann',
      lastName: 'Lee',
      phone: '5551234',
      email: 'ann@example.com'
    });

  beforeEach(async () => {
    service = jasmine.createSpyObj<ClientService>('ClientService', ['getClients', 'saveClient', 'deleteClient']);
    service.getClients.and.returnValue(of(existing));
    service.saveClient.and.returnValue(of({ success: true }));
    service.deleteClient.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      declarations: [RegistrationComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatTableModule
      ],
      providers: [{ provide: ClientService, useValue: service }]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load the clients table', () => {
    expect(component).toBeTruthy();
    expect(service.getClients).toHaveBeenCalledTimes(1);
    expect(el().querySelectorAll('mat-row').length).toBe(1);
    expect(el().querySelector('mat-row')!.textContent).toContain('ann@example.com');
  });

  // Requirement 1 + 2: error message and red required fields
  describe('saving with missing required fields', () => {
    beforeEach(() => submit());

    it('should not call the API', () => {
      expect(service.saveClient).not.toHaveBeenCalled();
    });

    it('should show a message listing the missing required fields', () => {
      const message = el().querySelector('.form-error')!.textContent!;
      expect(message).toContain('Required fields missing');
      ['First Name', 'Last Name', 'Phone', 'Email'].forEach(f => expect(message).toContain(f));
    });

    it('should only list the fields that are actually missing', () => {
      component.form.patchValue({ firstName: 'Ann', phone: '5551234' });
      submit();

      const message = el().querySelector('.form-error')!.textContent!;
      expect(message).toContain('Last Name, Email');
      expect(message).not.toContain('First Name');
      expect(message).not.toContain('Phone');
    });

    it('should mark every required field in red', () => {
      expect(el().querySelectorAll('.mat-form-field-invalid').length).toBe(4);
      expect(el().querySelectorAll('mat-error').length).toBeGreaterThanOrEqual(4);
    });

    it('should not mark a completed field in red', () => {
      component.form.patchValue({ firstName: 'Ann' });
      fixture.detectChanges();

      const first = el().querySelector('input[formcontrolname=firstName]')!.closest('mat-form-field')!;
      expect(first.classList).not.toContain('mat-form-field-invalid');
    });
  });

  // Requirement 5: numeric phone
  describe('phone validation', () => {
    ['5551234', '0', '1234567890'].forEach(value =>
      it(`should accept "${value}"`, () => {
        component.form.patchValue({ phone: value });
        expect(component.form.get('phone')!.valid).toBeTrue();
      })
    );

    ['555-1234', '12ab', 'abc', '+5551234', '555 1234', '12.5'].forEach(value =>
      it(`should reject "${value}"`, () => {
        component.form.patchValue({ phone: value });
        expect(component.form.get('phone')!.hasError('pattern')).toBeTrue();
      })
    );

    it('should report an invalid phone as invalid format, not as missing', () => {
      component.form.patchValue({ firstName: 'Ann', lastName: 'Lee', phone: '12ab', email: 'ann@example.com' });
      submit();

      const message = el().querySelector('.form-error')!.textContent!;
      expect(message).toContain('Invalid format: Phone');
      expect(message).not.toContain('Required fields missing');
      expect(service.saveClient).not.toHaveBeenCalled();
    });
  });

  // Requirement 6: email format
  describe('email validation', () => {
    ['ann@example.com', 'first.last@sub.domain.org', 'a+tag@x.io'].forEach(value =>
      it(`should accept "${value}"`, () => {
        component.form.patchValue({ email: value });
        expect(component.form.get('email')!.valid).toBeTrue();
      })
    );

    ['ann', 'ann@', '@example.com', 'ann@host', 'ann @example.com', 'ann@example.'].forEach(value =>
      it(`should reject "${value}"`, () => {
        component.form.patchValue({ email: value });
        expect(component.form.get('email')!.invalid).toBeTrue();
      })
    );

    it('should block saving and report an invalid email', () => {
      component.form.patchValue({ firstName: 'Ann', lastName: 'Lee', phone: '5551234', email: 'ann@host' });
      submit();

      expect(el().querySelector('.form-error')!.textContent).toContain('Invalid format: Email');
      expect(service.saveClient).not.toHaveBeenCalled();
    });
  });

  // Requirement 4: email field present
  it('should render an Email input and an Email column', () => {
    expect(el().querySelector('input[formcontrolname=email]')).not.toBeNull();
    expect(el().querySelector('mat-header-row')!.textContent).toContain('Email');
  });

  // Requirement 3: clear after save
  describe('saving a valid client', () => {
    beforeEach(() => {
      fillValid();
      submit();
    });

    it('should send the client with the email to the API', () => {
      expect(service.saveClient).toHaveBeenCalledOnceWith({
        id: 0,
        firstName: 'Ann',
        lastName: 'Lee',
        phone: '5551234',
        email: 'ann@example.com'
      });
    });

    it('should clear the form', () => {
      expect(component.form.value).toEqual({ firstName: null, lastName: null, phone: null, email: null });
      expect(el().querySelector<HTMLInputElement>('input[formcontrolname=firstName]')!.value).toBe('');
    });

    it('should not leave fields marked in red after the reset', () => {
      expect(el().querySelectorAll('.mat-form-field-invalid').length).toBe(0);
    });

    it('should clear any previous error message', () => {
      expect(el().querySelector('.form-error')).toBeNull();
    });

    it('should reload the table', () => {
      expect(service.getClients).toHaveBeenCalledTimes(2);
    });
  });

  it('should keep the typed data when the API call fails', () => {
    service.saveClient.and.returnValue(throwError(() => 'boom'));
    fillValid();

    component.onSave();

    expect(component.form.value.firstName).toBe('Ann');
  });

  // Requirement 7: delete
  describe('deleting a client', () => {
    it('should render a Delete button per row', () => {
      const buttons = Array.from(el().querySelectorAll('mat-row button')) as HTMLButtonElement[];
      expect(buttons.length).toBe(1);
      expect(buttons[0].textContent).toContain('Delete');
    });

    it('should call the API with the client id and reload the table', () => {
      (el().querySelector('mat-row button') as HTMLButtonElement).click();
      fixture.detectChanges();

      expect(service.deleteClient).toHaveBeenCalledOnceWith(1);
      expect(service.getClients).toHaveBeenCalledTimes(2);
    });

    it('should not submit the form when Delete is clicked', () => {
      (el().querySelector('mat-row button') as HTMLButtonElement).click();

      expect(service.saveClient).not.toHaveBeenCalled();
      expect(el().querySelector('.form-error')).toBeNull();
    });
  });
});
