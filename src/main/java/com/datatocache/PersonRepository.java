package com.datatocache;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface PersonRepository extends CrudRepository<Person, BigInteger> {

    Person getById(@Param("id") BigInteger id);



}

