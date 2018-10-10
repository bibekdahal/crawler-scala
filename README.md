# Parallel Web Crawler

This is a parallel web crawler developed in Scala using the actor model.

## Build

The project is a `sbt` project. To build it with `sbt-assembly`, simpy run:

```bash
sbt assembly
```

Which will generate the necessary jar file inside the target directory.

## Run

One can use the built jar file or sbt to run the program. The supported command line sitches are:

```
crawler \
    --max-depth <number> \
    --output <folder> \
    --scale <number> \
    <input_file>
```

Where, all except the `input_file` is optional.

* `max-depth` is the maximum depth the crawler can go before stopping. Defaults to 2.
* `output` is the folder where the pages are downloaded. If not provided, the pages are not downloaded.
* `scale` is the factor to scale the number of workers. This is multiplied with the number of processors available in the system. Defaults to 2.
* `input_file` is a files containing the list of seed urls in separate lines.

### Example usage

An example usage using `sbt` is as follows:

```
sbt run --max-depth 3 --output out seed-urls.txt
```
