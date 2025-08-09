# Usamos a imagem oficial do eclipse foundation
FROM eclipse-temurin:21-jdk-alpine

# Cria uma pasta no container
WORKDIR /app

# Copia o arquivo JAR gerado para dentro do container
COPY target/ecommerce-api-sem-patterns-0.0.1-SNAPSHOT.jar app.jar
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

# Expõe a porta que a aplicação usa
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["./entrypoint.sh"]
