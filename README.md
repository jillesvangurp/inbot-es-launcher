[![Build Status](https://travis-ci.org/Inbot/inbot-es-launcher.svg?branch=master)](https://travis-ci.org/Inbot/inbot-es-launcher)

# Introduction

Simple Java class to simplify getting an embedded Elasticsearch node running in tests. Also see [inbot-es-http-client](https://github.com/Inbot/inbot-es-http-client), for which this project is a test dependency.

While starting elasticsearch is easy programmatically, there are a few gotchas. This project addresses this by providing a convenient wrapper to manage the elasticsearch lifecycle with some sane defaults.
- creates the index directory if it does not exist
- start method waits for elasticsearch green status
- create `ElasticSearchNodeHolder` with your own settings object or with our *sane* defaults that you probably did not think about.
- simply choose where to store your index, what port to run on and whether or not you want the jvm to have a shutdown hook and start elasticsearch.

# Maven

```
<dependency>
  <groupId>io.inbot</groupId>
  <artifactId>inbot-es-launcher</artifactId>
  <version>1.0</version>
</dependency>
```

# Usage

```
ElasticSearchNodeHolder nodeholder = ElasticSearchNodeHolder.createWithDefaults("myindexdir", 9299, false);
nodeholder.start();

assertThat(nodeholder.node().client().admin().cluster().prepareHealth().get().getStatus()).as("es should be green immediately after start() returns").isEqualTo(ClusterHealthStatus.GREEN);

....

// when you are done
nodeholder.close()

```

Just do this before your tests run, in your Spring configuration, or wherever you initialize your system or tests and you will be able to control the elasticsearch lifecycle easily.

See [inbot-es-http-client](https://github.com/Inbot/inbot-es-http-client) for some example use with TestNG.

# License

See [LICENSE](LICENSE).

The license is the [MIT license](http://en.wikipedia.org/wiki/MIT_License), a.k.a. the expat license. The rationale for choosing this license is that I want to maximize your freedom to do whatever you want with the code while limiting my liability for the damage this code could potentially do in your hands. I do appreciate attribution but not enough to require it in the license (beyond the obligatory copyright notice).
