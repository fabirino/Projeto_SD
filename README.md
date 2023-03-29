## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `info`: the folder to store all Object Files
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

# Running the Program
To run the program, you need to run:
1. SearchModule.jar
2. IndexStorageBarrel.jar
3. Downloader.jar
4. Client.jar
## Notes
- It is possible to Run only the Search Module and the Client if the client only wants to see the Stats
- There can be more than one Downloader or Barrel running at the same time

# Devs
`Eduardo Figueiredo` 2020213717
`Fábio Santos` 2020212310
