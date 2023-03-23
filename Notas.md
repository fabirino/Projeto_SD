# Relatorio

## Como correr o programa

1. Search Model
2. Barrels
3. Downloaders (opcional)
4. Clientes

## Design Choices

- Falar sobre as garantias que metemos no UDP: timeouts e assim
- Usar varios downloaders em vez de um ultithreaded para simular que os downloaders podem estar em maquinas diferentes simulando a vida real. Isto torna a implementacao mais facil e é mais facil saber quantos estao a atrabalhar para os stats
- De forma a nao processar urls repetidos, se o url ja estiver na QUEUE, nao se adiciona. Isto pode levar a repetir um URL se ele ja estiver sido processado mas nao estiver mais na QUEUE. Uma vez que os Downloaders nao tem acesso ao Index, esta e uma maneira de nao repetir tantas vezes um link

## Protocolo Multicast 
Para ter garantias de entrega
Arranjar maneira de descobrir o numBarrels para saber quantos ack vai receber

1. o Downloader manda um pacote aos Barrels a dizer que vai mandar um pacote de tamanho x
2. Os ISB mandam um ack
3. Downloaders mandam um datagram packet com o objeto em si
4. Recebe um ack de todos

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
