# Relatorio

## Como correr o programa

1. Search Model
2. Barrels
3. Downloaders (opcional)
4. Clientes

## Design Choices

- Falar sobre as garantias que metemos no UDP: timeouts e assim
- Usar varios downloaders em vez de um ultithreaded para simular que os downloaders podem estar em maquinas diferentes simulando a vida real. Isto torna a implementacao mais facil e é mais facil saber quantos estao a atrabalhar para os stats

## Base de dados

JAR: postgressql-42.5.4.jar

```java
// Setup
String url = "jdbc:postgresql://localhost/<nome_da_bd>";
String username = "<user>";
String password = "<password>";

DriverManager.registerDriver(new org.postgresql.Driver());
connection = DriverManager.getConnection(url, username, password);
System.out.println("Connected to database");

// Update DB Imports por fazer
// java.sql.connection
// java.sql.driverblabla

String sql = "insert into url_info (url,titulo,citacao) values(?,?,?)";
PreparedStatement stament = connection.prepareStatement(sql);
stament.setString(1, url);
stament.setString(2, titulo);
stament.setString(3, citacao);
stament.executeUpdate();
```

## Palavras para nao processar

Guardar por exemplo num num hashset de palavras as palavras que nao sao para ser processadas (EN & PT)
StopWords
