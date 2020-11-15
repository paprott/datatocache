package com.datatocache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class Listener {

    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    @Autowired
    private ApplicationContext ctx;

    public void receiveMessage(String message) throws Exception {
        log.info("Received <" + message + ">");
        triggerJob();
    }

    private void triggerJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
        Job job = ctx.getBean("loadDataToCache", Job.class);
        JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
    }
}
