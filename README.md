# Microshed IT with Mail Server
Simple Microshed Integration Testing Scenario: Open Liberty + Mock API + Mail Server

# Scenario
To validate user identity and empower security, a service sends a One Time Password (OTP) to a trusted email address. Retrieve OTP service retrieves the OTP from the sent email for a given user id.
 
The email stuff (address, box) is owned by a generic email provider (Gmail, Outlook, etc), the association between user and email address is owned by an external API (UserAPI).

# Project
The main purpose of the project is to set up an integration tests suite for the Retrieve OTP service mocking the external resources: mail server and external API. 

Microshed for Open Liberty has been used as testing framework, Microshed itself is built on top of Testcontainers, some of their features used in the project are:

* Networking: all the containers share the same virtual network so they can use logical names and exposed ports
* Mock server: Testcontainers facility used to mock the external API
* Generic container: Testcontainers facility to use a generic docker image, used to mock the mail server

Testcontainer does not have a built in mail server container so Greenmail has been used as SMTP and IMAP/IMAPS server.

# Environment
This project has been tested up and running with the following:
* Java 1.8.0_181
* Apache Maven 3.5.4
* Docker 20.10.0

# Resources
* [Microshed for Open Liberty](https://openliberty.io/guides/microshed-testing.html)
* [Microshed](https://microshed.org/microshed-testing/)
* [Testcontainers](https://www.testcontainers.org)
* [Greenmail](https://greenmail-mail-test.github.io/greenmail)