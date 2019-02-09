[![Build Status](https://travis-ci.org/jurajcik/wuzeln.svg?branch=master)](https://travis-ci.org/jurajcik/wuzeln)

# Wuzeln Manager

## Building with Maven
Below are lested several options how to build the project:

- Build the whole project also with installing npm and building the angular frontend. 
By default uses the configuration for the local DB. 
You must specify path to your external configuration in the property `externConfigRoot`.
```
mvn clean package -DexternConfigRoot=path/to/configuration
```

- Build only the backend without tests (with local DB):
```
mvn clean package -DskipFrontend -DskipTests
``` 

- Build with embedded in-memory H2 DB: 
```
mvn clean package -P h2
```

- Build and run in local jetty (with local DB):
``` 
mvn clean appengine:devserver
``` 

- Build and deploy to google cloud:
```
mvn clean appengine:update -P gcp
``` 

## Domain Model

![picture](doc/domain_model.png)

## ER Diagram

![picture](doc/er_diagram.png)
