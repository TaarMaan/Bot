package com.example.bot.model;


import jakarta.persistence.Entity;
import org.springframework.data.repository.CrudRepository;

public interface Repository extends CrudRepository<User, Long> {

}
