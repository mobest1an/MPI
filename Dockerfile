# Используем официальный образ OpenJDK для сборки
FROM openjdk:17.0.2

# Указываем рабочую директорию внутри контейнера
WORKDIR /

# Копируем файл jar из локальной сборки в контейнер
COPY build/libs/MPI-0.0.1-SNAPSHOT.jar app.jar
COPY private_key.der private_key.der
COPY public_key.der public_key.der

# Указываем команду запуска приложения
CMD ["java", "-jar", "app.jar"]

# Указываем порт, который будет слушать приложение
EXPOSE 8080
