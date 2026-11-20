package com.exam.test.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exam.test.model.Client;

/** Spring Data JPA repository: findAll, save, existsById and deleteById come from {@link JpaRepository}. */
@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
}
