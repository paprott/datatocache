package com.datatocache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigInteger;
import java.sql.Date;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

	@Override
	public Person process(final Person person) throws Exception {
		final BigInteger id = person.getId();
		final String firstName = person.getFirstName();
		final String lastName = person.getLastName();
		final String email = person.getEmail();
		final Date joinedDate = person.getJoinedDate();

		final Person transformedPerson = new Person(id, firstName, lastName, email, joinedDate);

		return transformedPerson;
	}

}
