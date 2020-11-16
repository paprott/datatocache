# Data to cache

## Table of contents
* [General info](#general-info)
* [Technologies](#technologies)
* [Prerequirements](#prerequirements)
* [Setup](#setup)
* [Control](#Control)


## General info
This is a REST controlled project that puts request on RabbitMQ queue. Listener on the queue triggers batch job which reads data from DB and loads it to cache. Data for DB is provided in .CSV file.
	
## Technologies
Project is created with:
* SpringBoot: 2.4.0
* Java: 8
* HSQLDB 2.5.1

## Prerequirements
To download and run this project install below software:
* Java: 8
* Git: newer than 2.0
* Maven: newer than 3.6.1 (Optional)
* RabbitMQ server 3.8.9

## Setup
To run this project, install it locally following below steps: 

#### 1. Download project from github
To copy project from github run below command in your terminal in location you want to save the project:

```
$ git clone <project link.git>
```

#### 2. Run Maven command to create \*.war file
*\*While running below commands you need to be in the same directory as the \*.war file* <br/>
Running below command will generate \.war file which can be locally deployed:
```
$ ./mvnw package
```
(Without local Maven): 
```
$ mvn package
```

If you want to build your \*.war file skipping tests run:
```
$ ./mvnw package -DskipTests
```
(Without local Maven): 
```
$ mvn package -DskipTests
```

#### 3. Deploy \*.war file
*\*While running below commands you need to be in the same directory as the \*.war file* <br/>
To quickly deploy this project you can run below command. 
```
$ java -jar datatocache-0.0.1-SNAPSHOT.war
```

## Control

Please call:

1. http://[host]:8080/insertDataToDb <br/>
To load data form .CSV to DB 

2. http://[host]:8080/loadDataToCache <br/>
To send message to queue and trigger the job which will load data from DB to cache.

3. http://[host]:8080/loadDataToCacheWoMq <br/>
To trigger job loading data to cache directly without MQ.

4. http://[host]:8080/check <br/>
To test if data matches. <br/>
This will return same row of data from DB and from cache for comparison. <br/>
You can enter 'rowNumber' parameter to specify the row within range of given data. <br/>
Default is set to 1. <br/>

5. You can update or swap sample-data.csv in classpath to change the sample data.
