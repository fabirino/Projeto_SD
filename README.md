## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies


# Requerimentos
1. PostgreSQL
2. Java

# DataBase

### Connect to database
```shell
psql -h localhost -p 5432 -d postgres -U postgres
```
### Create User
``` shell
create user postgres with encrypted password 'postgres'
```
### Create Database
``` shell
create database ProjetoSD
```
### Give Privileges to user
``` shell
grant all privileges on database ProjetoSD to postgres
```
### Run Sql files
```txt
1. init.sql
```

# Devs
Eduardo Figueiredo<br/>
Fábio Santos
