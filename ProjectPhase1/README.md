## Project phase 1: From CSV to Kafka 

This project has 2 functions: 
1. Read raw data from local CSV file and send data to Kafka specific topic line by line 
2. Consume Kafka stream in one specific topic 
---
How to run:

Git clone this project, direct to the project location, and run the project with following command:
``` scala 
sbt run
```
---
Specification:

| ~ | ~ |
|---|---|
| `IP address` | `192.168.99.100` |
| `Port` | `9092` |
| `CSV Filename` | `C:\Users\Dan\Desktop\1000-genomes Fother Fsample_info Fsample_info.csv` |
| `Topic` | `MessageProcessor` |

To extract CSV file data requires the absoulte path on your PC/Mac, make corresponding changes to your file store location. 



