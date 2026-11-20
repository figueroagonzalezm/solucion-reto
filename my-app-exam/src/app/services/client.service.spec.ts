import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ClientService } from './client.service';
import { Client } from '../interfaces/client';
import { environment } from '../../environments/environment';

describe('ClientService', () => {
  let service: ClientService;
  let http: HttpTestingController;
  const api = environment.apiUrl;

  const client: Client = {
    id: 0,
    firstName: 'Ann',
    lastName: 'Lee',
    phone: '5551234',
    email: 'ann@example.com'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(ClientService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getClients should GET /clients', () => {
    const body = [{ ...client, id: 1 }];
    let result: any;

    service.getClients().subscribe(r => (result = r));

    const req = http.expectOne(`${api}/clients`);
    expect(req.request.method).toBe('GET');
    req.flush(body);
    expect(result).toEqual(body);
  });

  it('saveClient should POST the client (including email) to /client', () => {
    service.saveClient(client).subscribe();

    const req = http.expectOne(`${api}/client`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(client);
    expect(req.request.body.email).toBe('ann@example.com');
    req.flush({ success: true, message: 'Client saved with sucesss' });
  });

  it('deleteClient should send DELETE /clients/{id}', () => {
    let completed = false;

    service.deleteClient(7).subscribe({ complete: () => (completed = true) });

    const req = http.expectOne(`${api}/clients/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
    expect(completed).toBeTrue();
  });

  describe('error handling', () => {
    beforeEach(() => spyOn(window, 'alert'));

    it('should show the API error message on a 404 from DELETE', () => {
      let failed = false;

      service.deleteClient(99).subscribe({ error: () => (failed = true) });

      http.expectOne(`${api}/clients/99`).flush(
        { status: 404, error: 'Not Found', message: 'Client with id 99 not found' },
        { status: 404, statusText: 'Not Found' }
      );

      expect(failed).toBeTrue();
      expect(window.alert).toHaveBeenCalledTimes(1);
      const shown = (window.alert as jasmine.Spy).calls.mostRecent().args[0] as string;
      expect(shown).toContain('404');
      expect(shown).toContain('Client with id 99 not found');
    });

    it('should fall back to the HTTP error message when the body has no message', () => {
      service.getClients().subscribe({ error: () => {} });

      http.expectOne(`${api}/clients`).flush('boom', { status: 500, statusText: 'Server Error' });

      const shown = (window.alert as jasmine.Spy).calls.mostRecent().args[0] as string;
      expect(shown).toContain('500');
    });
  });
});
