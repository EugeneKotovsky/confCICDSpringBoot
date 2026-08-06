# TODO README

Локально тут: `C:\MYFILES\NetologyJava\confCICDSpringBoot`.

Локально запуск: `mvn spring-boot:run`

Локально запуск в docker: `docker build -t todo .` потом `docker run -p 8080:8080 todo`

api: `/tasks`, проверка: `/actuator/health`. и всё...

На VPS: `http://194.67.119.228:8080`, health: `http://194.67.119.228:8080/actuator/health`.
