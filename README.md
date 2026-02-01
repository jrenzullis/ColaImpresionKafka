README — Sistema de Cola de Impresión con Kafka

Este ejercicio implementa un sistema de mensajería con Kafka + Java para gestionar una cola de impresión avanzada.

Los empleados envían documentos en formato JSON. El sistema los guarda, los transforma en páginas y los envía a colas de impresión separadas para B/N y Color, simulando varias impresoras trabajando en paralelo.

Arquitectura de Topics

El sistema usa 4 topics Kafka:

documents.raw
documents.transformed
print.color
print.bn


Flujo de mensajes:

DocumentProducer
   ↓
documents.raw
   ↓
DocumentProcessor
   ├── guarda JSON original en disco
   └── divide documento en páginas
        ↓
documents.transformed
        ↓
PrintRouter
   ├── print.color → impresoras color
   └── print.bn → impresoras B/N

Formato del mensaje de entrada

Los productores envían JSON con esta estructura:

{
  "titulo": "Titulo",
  "documento": "Texto a imprimir",
  "tipo": "Color o B/N",
  "sender": "Nombre empleado"
}

 Puesta en marcha en local
Instala requisitos

Instala:

Java 17+

Maven

Kafka 4.x

Descomprime Kafka en una ruta sin espacios.

Arranca Kafka

En carpeta bin/windows:

.\kafka-storage.bat random-uuid


Copia el UUID y ejecuta:

.\kafka-storage.bat format --standalone -t TU_UUID -c ..\..\config\server.properties


Arranca el servidor:

.\kafka-server-start.bat ..\..\config\server.properties

Crear Topics

En bin/windows ejecuta:

.\kafka-topics.bat --create --topic documents.raw --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

.\kafka-topics.bat --create --topic documents.transformed --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

.\kafka-topics.bat --create --topic print.color --bootstrap-server localhost:9092 --partitions 2 --replication-factor 1

.\kafka-topics.bat --create --topic print.bn --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1


Comprueba:

.\kafka-topics.bat --list --bootstrap-server localhost:9092

Ejecutar el sistema

Compila:

mvn clean package


Ejecuta cada clase en una terminal distinta (desde el IDE es más fácil):

Orden recomendado:

1 DocumentProcessor
2 PrintRouter
3 ColorPrinter   (puedes lanzar 2 instancias)
4 BnPrinter      (puedes lanzar 3 instancias)
5 DocumentProducer

Paralelismo

El procesador guarda el documento original y lo transforma en páginas en paralelo usando hilos.

Varias impresoras usan el mismo consumer-group para repartirse el trabajo.

Carpetas generadas

El sistema crea automáticamente:

data/originals/{sender}/
data/printed/color/
data/printed/bn/


Los originales se guardan completos

Las páginas impresas se guardan como archivos de texto

 Reiniciar el sistema
Parar Kafka

Ctrl+C en la ventana del servidor.

Borrar topics
.\kafka-topics.bat --delete --topic documents.raw --bootstrap-server localhost:9092
.\kafka-topics.bat --delete --topic documents.transformed --bootstrap-server localhost:9092
.\kafka-topics.bat --delete --topic print.color --bootstrap-server localhost:9092
.\kafka-topics.bat --delete --topic print.bn --bootstrap-server localhost:9092


Luego vuelve a crearlos.

Limpiar datos generados

Borra la carpeta:

data/

Reiniciar offsets de consumidores (si hace falta)

Cambia el group.id en el código o usa:

--from-beginning


en consumidores de prueba.

🔧 Notas para mantenimiento

Si un consumidor cae, Kafka no pierde mensajes.

Puedes añadir más impresoras lanzando más instancias.

El reparto depende de las particiones del topic.

replication-factor=1 porque el entorno es local de desarrollo.
