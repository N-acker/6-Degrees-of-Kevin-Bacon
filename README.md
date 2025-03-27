# 🎬 Six Degrees of Kevin Bacon

A backend service that calculates the shortest path between actor Kevin Bacon and any other actor in the database, illustrating the "six degrees of separation" concept within the film industry.

## 🛠️ Project Overview

This project implements a RESTful API using **Java** to determine the minimal connection steps between Kevin Bacon and other actors based on their shared movie appearances. It leverages the **Neo4j** graph database to model and query relationships efficiently.

## 🎯 Key Features

- **Actor and Movie Management**: Add and retrieve actor and movie data through dedicated API endpoints.
- **Relationship Mapping**: Establish and query relationships between actors and movies to build the connection graph.
- **Bacon Number Calculation**: Compute the "Bacon Number," representing the number of connections between Kevin Bacon and a specified actor.
- **Shortest Path Determination**: Identify the shortest path of collaborations linking Kevin Bacon to another actor.

## 🔧 Technologies Used

- **Java**: Core programming language for implementing the backend logic.
- **Neo4j**: Graph database utilized to store and manage actor-movie relationships.
- **Robot Framework**: Employed for testing the functionality and reliability of the API endpoints.
- **Postman**: Used to evaluate and debug the status of each endpoint during development.

## 🚀 Getting Started

To set up and run this project locally:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/N-acker/6-Degrees-of-Kevin-Bacon.git

