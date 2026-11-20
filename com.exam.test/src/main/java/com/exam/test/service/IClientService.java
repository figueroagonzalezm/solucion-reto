package com.exam.test.service;

import java.util.List;
import com.exam.test.model.dtos.ClientDto;

public interface IClientService {
	public List<ClientDto> findAll();
	 public int  save(ClientDto client);
	 /** @return true if a client was deleted, false if it did not exist */
	 public boolean deleteById(int id);
}
