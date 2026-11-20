package com.exam.test.service;

import java.util.List;
import com.exam.test.model.dtos.ClientDto;

public interface IClientService {
	public List<ClientDto> findAll();
	 public ClientDto save(ClientDto client);
	 /** @throws com.exam.test.exception.ClientNotFoundException if the client does not exist */
	 public void deleteById(int id);
}
