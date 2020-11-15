package com.datatocache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
public class RestServiceController {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private ApplicationContext ctx;


    @GetMapping("/loadDataToCache")
    public void sendMsg() throws Exception {
        new SendMsg().send();
    }

    @GetMapping("/loadDataToCacheWoMq")
    public void insertWoMq() throws Exception {
        triggerJob("loadDataToCache");
    }

    @GetMapping("/insertDataToDb")
    public void insert() throws Exception {
        triggerJob("insertDatatoDB");
    }

    @GetMapping("/check")
    public String check(@RequestParam(value = "rowNumber", defaultValue = "1") Integer rowNumber) throws Exception {

        String onScreen = "Sample data from: <br>" +
                "DB<br>";

        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", rowNumber);
        onScreen += jdbcTemplate.queryForObject(
                "SELECT ID,EMAIL,FIRST_NAME,JOINED_DATE,LAST_NAME FROM PERSON WHERE ID = :id;", namedParameters, new BeanPropertyRowMapper<>(Person.class));

        onScreen += "<br>Cache<br>"
                + ((SimpleValueWrapper) cacheManager.getCache("PersonCache").get(BigInteger.valueOf(rowNumber))).get();

        return onScreen;
    }

    private void triggerJob(String jobName) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
        Job job = ctx.getBean(jobName, Job.class);
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
    }

}



