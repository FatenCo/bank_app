package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
