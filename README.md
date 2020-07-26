# reactive-registration
1. Install redis: base instruction avaliable on https://redis.io/download;
2. Install postgreSQL database and create database;
3. Clone the whole project and specify properties in application.properties before run:
    * server.port= (noooooooot 8080 please)
    * spring.datasource.url=
    * spring.datasource.username=
    * spring.datasource.password=
    * db.driver= (should be: org.postgresql.Driver)
    * spring.redis.host= (default: localhost)
    * spring.redis.port= (default: 6379)

4.Run Maven Goals: clean package

5.Run application from it's main class (if you're using idea just press green play button) or by command line by running java -jar application_name.jar
