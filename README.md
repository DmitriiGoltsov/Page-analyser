### Hexlet tests and linter status:
[![Actions Status](https://github.com/DmitriiGoltsov/java-project-72/workflows/hexlet-check/badge.svg)](https://github.com/DmitriiGoltsov/java-project-72/actions)

### CodeClimate maintainability status:
[![Maintainability](https://api.codeclimate.com/v1/badges/0384964c95093dd6ab20/maintainability)](https://codeclimate.com/github/DmitriiGoltsov/java-project-72/maintainability)

### CodeClimate test coverage based on Jacoco:
[![Test Coverage](https://api.codeclimate.com/v1/badges/0384964c95093dd6ab20/test_coverage)](https://codeclimate.com/github/DmitriiGoltsov/java-project-72/test_coverage)

### Link on working app:
[![Render.com](https://render.com/images/render-banner.png)](https://site-analyzer.onrender.com/)

## Description
The study project for learning how to create a java MVC application and how to use Javalin framework, Thymeleaf and other technologies for that purpose.

The application provides a service that is able to collect, analyse url-s for their SEO and store results of this analyze using postgreSQL database. 

Particularly it provides simple validation for input url-s, assigns them unique ids and also collect and store basic data about them:

1) Their availability (the app checks the status code of url's server response);
2) The content of title, h1 and description tags of url's main page.

## Used technologies
* Javalin
* Thymeleaf
* Bootstrap
* H2
* PostgreSQL
* JUnit
* MockWebServer
* Docker (for deploy)
* Lombok
* And others

## Requirements

* JDK 20
* Gradle 8.2
* GNU Make

## Setup

```zsh
make setup
```

## Run server

```zsh
make start
# Open http://localhost:8085
```