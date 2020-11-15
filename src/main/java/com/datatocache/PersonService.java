package com.datatocache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Cacheable("PersonCache")
    public Person getPerson(BigInteger id) {
        return repository.getById(id);
    }

}