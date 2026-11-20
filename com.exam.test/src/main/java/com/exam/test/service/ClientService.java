package com.exam.test.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.test.exception.ClientNotFoundException;
import com.exam.test.model.Client;
import com.exam.test.model.dtos.ClientDto;
import com.exam.test.respository.ClientRepository;


@Service
public class ClientService implements IClientService {

	@Autowired
	private ClientRepository clientRepository;

	@Override
	@Transactional(readOnly = true)
	public List<ClientDto> findAll() {
		return clientRepository.findAll().stream().map(this::toDto).toList();
	}

	@Override
	@Transactional
	public ClientDto save(ClientDto clientdto) {
		Client client = new Client();
		client.setFirstName(clientdto.getFirstName());
		client.setLastName(clientdto.getLastName());
		client.setSurname(clientdto.getSurname());
		client.setPhone(clientdto.getPhone());
		client.setEmail(clientdto.getEmail());

		return toDto(clientRepository.save(client));
	}

	@Override
	@Transactional
	public void deleteById(int id) {
		// deleteById is a silent no-op for a missing id, so check explicitly to return 404
		if (!clientRepository.existsById(id)) {
			throw new ClientNotFoundException(id);
		}
		clientRepository.deleteById(id);
	}

	private ClientDto toDto(Client c) {
		return new ClientDto(c.getId(), c.getFirstName(), c.getLastName(), c.getSurname(), c.getPhone(), c.getEmail());
	}

}
