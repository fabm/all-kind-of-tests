CREATE KEYSPACE nu3w WITH replication={'class' : 'SimpleStrategy', 'replication_factor':1};
USE nu3w;

CREATE TABLE user(
    id varchar,
    name varchar,
    login varchar,
    password varchar,
    email varchar,
    PRIMARY KEY(id));