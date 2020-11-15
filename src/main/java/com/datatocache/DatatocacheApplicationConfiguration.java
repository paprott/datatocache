package com.datatocache;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@EnableCaching
public class DatatocacheApplicationConfiguration {

    //MQ configuration
    static final String topicExchangeName = "nt-exchange";
    static final String queueName = "nt-queue";

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("nordea.#");
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Listener listener) {
        return new MessageListenerAdapter(listener, "receiveMessage");
    }


    //Batch configuration
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;


    // Job for putting data into cache
    private static final String QUERY =
            "SELECT " +
                    "ID, " +
                    "EMAIL, " +
                    "FIRST_NAME, " +
                    "JOINED_DATE, " +
                    "LAST_NAME " +
                    "FROM PERSON;";

    @Bean
    public JdbcCursorItemReader<Person> job1Reader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Person>()
                .name("cursorItemReader")
                .dataSource(dataSource)
                .sql(QUERY)
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))
                .build();
    }

    @Bean
    public PersonItemProcessor job1processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public ItemWriter<Person> job1writer() {
        return new PersonItemWriter();
    }


    @Bean
    public Job loadDataToCache(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("loadDataToCache")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JdbcCursorItemReader<Person> reader) {
        return stepBuilderFactory.get("step1")
                .<Person, Person>chunk(100)
                .reader(reader)
                .processor(job1processor())
                .writer(job1writer())
                .build();
    }


    //Job for inserting data to DB from CSV
    @Bean
    public FlatFileItemReader<Person> job2eader(FieldSetMapper<Person> fieldSetMapper) {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names(new String[]{"id", "firstName", "email", "joinedDate", "lastName"})
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    @Bean
    FieldSetMapper<Person> fieldSetMapper() {
        BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Person.class);
        fieldSetMapper.setConversionService(ApplicationConversionService.getSharedInstance());
        return fieldSetMapper;
    }

    @Bean
    public PersonItemProcessor job2processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> job2writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO PERSON (id, email, first_name, joined_date, last_name) VALUES (:id, :email, :firstName, :joinedDate, :lastName)")
                .dataSource(dataSource)
                .build();
    }


    @Bean
    public Job insertDatatoDB(JobCompletionNotificationListener listener, Step step1forDbLoad) {
        return jobBuilderFactory.get("insertDatatoDB")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1forDbLoad)
                .end()
                .build();
    }

    @Bean
    public Step step1forDbLoad(JdbcBatchItemWriter<Person> writer, FlatFileItemReader<Person> job2eader) {
        return stepBuilderFactory.get("step1forDbLoad")
                .<Person, Person>chunk(100)
                .reader(job2eader)
                .processor(job2processor())
                .writer(writer)
                .build();
    }


    //Cache configuration
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("PersonCache");
    }


}
