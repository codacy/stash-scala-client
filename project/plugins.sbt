// HACK: Needed to resolve ohnosequences:ivy-s3-resolver:0.13.0 -> sbt 0.13
resolvers += "Spring Plugin Releases" at "https://repo.spring.io/plugins-release/"

addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "17.1.4")
