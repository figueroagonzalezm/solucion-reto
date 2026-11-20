package com.exam.test.respository;

import java.util.List;

import com.exam.test.model.Client;

public interface IClientRepository {
	
  public List<Client> findAll(); 
  public int  save(Client client);
  public int  deleteById(int id);
  
}
