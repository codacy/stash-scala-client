[![Circle CI](https://circleci.com/gh/codacy/stash-scala-client/tree/master.svg?style=shield)](https://circleci.com/gh/codacy/stash-scala-client/tree/master)
[![Codacy Badge](https://www.codacy.com/project/badge/grade/3c6fbd37c5ec45eeadab6e98d7c55b27)](https://www.codacy.com/app/Codacy/stash-scala-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/stash-scala-client_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/stash-scala-client_2.11)

# Stash Scala client

This is a simple library that aims to have basic functions of the Stash API.
This library was meant to support Codacy when interacting with Stash.
It is in a very early stage and all the contributions are welcome.

### Usage

Import on SBT:

```
"com.codacy" %% "stash-scala-client" % "1.1.5"
```

Usage:

```scala
val client = new StashClient("baseUrl", "consumerKey", "consumerSecret", "token", "secretToken")

val repoServices = new RepositoryServices(client)

val response = repoServices.getRepositories("projectKey")
```

### Creators

1. Rodrigo Fernandes

### Contributors

1. Pedro Rijo

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
- Auto-comments on Commits and Pull Requests
- Integrations with Slack, HipChat, Jira, YouTrack
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.

### License

stash-scala-client is available under the The Apache Software License, Version 2.0.
