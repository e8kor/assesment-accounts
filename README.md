# Accounts API
Application provide RESTful API for inserting transactions related to account and accessing its balance.

## Design
Transactions are appended to history table, actual balance can be accessed via view that aggregates history. Application heavily relies on IO monad and effects.
Application uses hexagon architecture pattern.

Application provides two types of backend:
* inmemory hashmap
* h2 database

Main frameworks used:
* http4s - http layer
* doobie - database access layer
* flyway - schema migration logic
* circe - json manipulation
* h2 - inmemory database


## End points
The end points are:

Method | Url           | Description
------ | ------------- | -----------
GET    | /balance/{id} | Returns balance of account
POST   | /transactions | Process list of transactions
GET    | /build-info   | Deployed application meta

## How To
Create a transactions:
```curl -X POST --header "Content-Type: application/json" --data '[{"id": 1, "delta": 100}]' http://localhost:4140/transactions```

Get account balance:
```curl http://localhost:4140/balance/1```

Get account balance:
```curl http://localhost:4140/build-info```

## Run Application
Default http port used in application: 4140. Updated `application.conf` in order to modify it or via environment variable `HTTP_PORT`
Application can started by:
 * **RECOMMENDED** `sbt assembly` and `java -jar app.jar`. Latest assembled version of application are in root of repository
 * `sbt docker` and `cd deployment; docker-compose up`
 * `sbt run`, may have potential issues with applying migrations
  ```
  2018-10-19 09:31:19 [scala-execution-context-global-93] WARN  o.f.c.i.u.s.c.ClassPathScanner - Unable to resolve location classpath:db/migration
  ```
