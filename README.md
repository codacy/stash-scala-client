[![Circle CI](https://circleci.com/gh/codacy/stash-scala-client/tree/master.svg?style=shield)](https://circleci.com/gh/codacy/stash-scala-client/tree/master)
[![Codacy Badge](https://www.codacy.com/project/badge/grade/3c6fbd37c5ec45eeadab6e98d7c55b27)](https://www.codacy.com/app/Codacy/stash-scala-client)
[![Codacy Badge](https://api.codacy.com/project/badge/coverage/3c6fbd37c5ec45eeadab6e98d7c55b27)](https://www.codacy.com/app/Codacy/coverage-parser)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/stash-scala-client_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/stash-scala-client_2.11)

# Stash Scala client

This is a simple library that aims to have basic functions of the Stash API.
This library was meant to support Codacy when interacting with Stash.
It is in a very early stage and all the contributions are welcome.

### Usage

Import on SBT:

```
"com.codacy" %% "stash-scala-client" % "1.0.0-beta4"
```

Usage:

```
val client = new StashClient("baseUrl", "consumerKey", "consumerSecret", "token", "secretToken")

val repoServices = new RepositoryServices(client)

val response = repoServices.getRepositories("projectKey")

```

### Creators

1. Rodrigo Fernandes

### Contributors

1. Pedro Rijo

### License

stash-scala-client is available under the The Apache Software License, Version 2.0.
