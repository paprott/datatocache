package com.datatocache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonItemWriter<T> implements ItemWriter<T> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemWriter.class);

    @Autowired
    CacheManager cacheManager;

    public void write(List<? extends T> items) {
        items.stream().map(item -> (Person) item).forEach(person -> cacheManager.getCache("PersonCache").put(person.getId(), person));
    }
}

