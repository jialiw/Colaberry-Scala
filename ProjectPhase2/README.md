## Project phase 2: read from Kafka topic 1 to Kafa topic 2 
This project consumes Kafka stream from Topic 1, processes data, and sends enhanced data to Topic 2 
---
How to run:
Git clone this project, direct to the project location in local drive, and run the project with following command:
```scala
sbt run
```
---
Specification:

| ~ | ~ |
| --- | --- |
| `IP address` | `192.168.99.100:9092` |
| `Topic 1` (initial topic) | `MessageProcessor` |
| `Topic 2` (new topic) | `MessageProcessor2` |
