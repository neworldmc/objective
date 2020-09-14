# Objective [![LICENSE](https://img.shields.io/github/license/neworldmc/objective)]() [![CircleCI](https://circleci.com/gh/neworldmc/objective.svg?style=shield)](https://circleci.com/gh/neworldmc/objective) [![BCH compliance](https://bettercodehub.com/edge/badge/neworldmc/objective?branch=master)](https://bettercodehub.com/)

![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/neworldmc/objective)
![GitHub repo size](https://img.shields.io/github/repo-size/neworldmc/objective)

A high performance async Minecraft Anvil chunk storage server and library 
designed to achieve high throughput and consistent latency

IMPORTANT NOTICE: This project is still at an early stage. API will remain unstable
and functionalities may not be fully implementation before we officially bump to 1.0.0

## 0. License
This project is under the GNU LESSER GENERAL PUBLIC License. Feel free to link or use the tools and libraries
provided by this project UNMODIFIED. For details, please refer to the license.

Please bear in mind that this project is not directly or indirectly supported Mojang and is not subject to their
quality control standards. By offering this project under the terms of LGPL-3 also implies that we do not have any
liability to your data (see GPL3 section 15, 16, 17). Rest assure that we will do our best to ensure the correctness
of the implementation and to implement data protection methods, but please DO evaluate your choice and operate carefully
when using any of the functionalities provided by this project.

The above paragraphs only servers as a kind reminder for any potential and current consumers of this project and does
not carry any legal liability. If there is any inconsistency between the above words and the license, please refer
to the license.

## 1. Prerequisites
This project is powered by Kotlin/JVM technology. We use Netty for networking, Koin for managing components and log4j
for managing logging activities. The following is the basic requirements for this project to function properly:

Java EE or Graal CE/EE or OpenJDK. Java 11 compatibility required

An adaquate amout of disk space for your data. SSDs, especially NVME storage is preferred for consistent access latency

At least 1GiB of system memory for adaquate performance on standalone use

Systems with native support for non-blocking file IO is preferred as JVM simulated implementation will exhaust system
resources fairly quickly under heavy load (around 10^6 concurrent requests on 12 thread i7). 
As usual, please tune your memory settings on a pre-usage basis.

## 2. Structure
Core: The actual anvil db implementation along with some utilities

Network: Shared code for Client/Server mode networking

Embedded: Interface for embedding into other projects

Server: Standalone server of the anvil db

Shell: Interactive shell for basic data query manipulation 

For each module, to avoid having deep hierarchy in the project view will use the following
directory convention instead of the default layout:

/module: directory for module source

/resources: directoy for module resources

PROJECT_ROOT/build/MODULE_NAME: build directory for module

Otherwise default gradle convention implies.

## 3. Documentation
TBD

## 4. Roadmap
TBD

## 5. Contributing
TBD

## 6. Q&A
TBD

## 7. References
TBD
