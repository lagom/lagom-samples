# File Download

This recipe demonstrates both custom message serializers and adding headers to a response, by implementing a simple CSV download service intended for web browsers to access directly.

# Testing the recipe

## Automated tests

You can test this recipe using the provided tests:

```bash
sbt test
```

## Manual tests

You can also test this recipe manually.

In a terminal, start the service:

```bash
sbt runAll
```

Wait for the messages confirming that the service has started:

```
[info] Service file-download-impl listening for HTTP on 0:0:0:0:0:0:0:0:53836
[info] (Service started, press enter to stop and go back to the console...)
```

Then, visit http://localhost:9000/downloadEmployees in a web browser. It will download a file called `employees.csv`.
