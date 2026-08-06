# todo thing

лежит тут: `C:\MYFILES\NetologyJava\confCICDSpringBoot`.

запуск локально: `mvn spring-boot:run`

или docker: `docker build -t todo .` потом `docker run -p 8080:8080 todo`

api: `/tasks`, проверка: `/actuator/health`. ну всё.

на сервере оно тут: `http://194.67.119.228:8080`, health: `http://194.67.119.228:8080/actuator/health`.
