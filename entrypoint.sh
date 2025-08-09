#!/bin/sh
echo "Aguardando o MySQL iniciar..."
# espera até que o banco responda na porta 3306
while ! nc -z mysql 3306; do
  sleep 1
done
echo "MySQL está pronto. Iniciando a aplicação..."
exec java -jar app.jar