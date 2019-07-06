# Cached translation

### Caching service for google translate API


## Installation and running up

* install java version 11
* install docker
* Clone project
* Copy to project directory google application credential json file
* In project directory execute:
> ./gradlew installDist

> docker-compose build

> docker-compose up

* For operations with service possible use command line client from python version of project

[Cached translation python version](https://github.com/medvecky/cached_translation)
 
 
 ## Test run
 
 ### Unit tests
 
 > ./gradlew test --tests UnitTestsSuite
 
 ### Integration tests
 
 Set up GOOGLE_APPLICATION_CREDENTIALS variable
 
 >GOOGLE_APPLICATION_CREDENTIALS=[path to json file with credentials received from google]
 
 >./gradlew test --tests IntegrationTestsSuite 
 