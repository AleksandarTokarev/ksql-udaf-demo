# KSQL Custom Functions

## How to build and run locally
- mvn clean install
- copy the generated jar from target to docker/data/kafka-sqldb-data/udfs
- go inside `docker` folder and run `docker-compose up -d`
- ssh into the container with `docker exec -ti kafka-ksqldb ksql`
- open `localhost:8080` in the browser to get to redpanda (kafka ui like interface)
- create `users.items` topic with lets say 12 partitions
- in the ksql terminal (3 commands above) run `CREATE TABLE USERS_ITEMS_INITIAL (F_KEY STRING PRIMARY KEY, USERID BIGINT, NAME STRING, DESCRIPTION STRING, AMOUNT DOUBLE, UPDATED BIGINT) WITH (KAFKA_TOPIC='users.items', KEY_FORMAT='KAFKA', VALUE_FORMAT='JSON');`
- in the ksql terminal run also
```
CREATE TABLE IF NOT EXISTS ITEMS_PER_USER_TABLE WITH (KAFKA_TOPIC='ITEMS_PER_USER_TABLE', PARTITIONS=12, VALUE_FORMAT='JSON') AS SELECT USERS_ITEMS_INITIAL.USERID USERID,
  COLLECT_ITEMS(STRUCT(`NAME`:=USERS_ITEMS_INITIAL.NAME, `DESCRIPTION`:=USERS_ITEMS_INITIAL.DESCRIPTION, `AMOUNT`:=USERS_ITEMS_INITIAL.AMOUNT, `UPDATED`:=USERS_ITEMS_INITIAL.UPDATED)) ACTIVE_ITEMS
FROM USERS_ITEMS_INITIAL
GROUP BY USERS_ITEMS_INITIAL.USERID
EMIT CHANGES;
```
- do some insertions in `users.items` topic

```
Key
10-T-TOM

Value
{
    "name": "TOMATOES",
    "description": "They are tasty",
    "amount": 1,
    "updated": 1905906822314,
    "userId": 10
}

Key
10-T-POT

Value
{
    "name": "Potatoes",
    "description": "They are tasty too",
    "amount": 2,
    "updated": 1905906822314,
    "userId": 10
}


Key
10-T-POT

Value
{
    "name": "Potatoes",
    "description": "They are tasty too",
    "amount": 3,
    "updated": 1905906822314,
    "userId": 10
}
```
- inspect the `ITEMS_PER_USER_TABLE` kafka topic in RedPanda UI to see how the items are being aggregated
- KSQL_KSQL_EXTENSION_DIR - this is the environment variable which is used by KSQLDB docker image to `read` the ksql custom functions from the created jar (see docker file how `/data/udfs` is being mounted)
