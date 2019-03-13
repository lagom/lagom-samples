# Lagom Recipe: How to upload a file

Lagom is designed with RPC in mind and abstracts from the transport used to exchange messages between services but it defaults to an HTTP/Json transport. File upload is a feature often requested that requires exploiting the power of one of Lagom's building blocks: the Play Framework.

This recipe demonstrates how to add a side-car Play Router using pure Play code to handle file uploads next to your Lagom `Service`'s. This recipe is inspired on Play's example application [Play Java File Upload Example](https://github.com/playframework/play-java-fileupload-example/tree/2.6.x) that is part of [Play's great collection of examples](https://www.playframework.com/download#examples) and is based on [JavaRoutingDsl](https://www.playframework.com/documentation/2.7.x/JavaRoutingDsl) that allows you to build a Play Router programmatically. It adds an extra path (`/api/files`) that receives POST calls for multipart-form data.

**Note:** This recipe supported only Lagom 1.5+

## Testing the recipe

##### unit tests

You can test this recipe using the provided tests:

```bash
sbt test
```

##### manual tests

You can also test this recipe manually using 2 separate terminals.

On one terminal start the service:

```bash
sbt runAll
```

On a separate terminal, use `curl` to POST a file (in this example we're posting `build.sbt`:

```bash
curl -X POST -F "data=@build.sbt" -v  http://localhost:9000/api/files
```

You can also exercise a regular Lagom endpoint that coexists with the file upload controller:

```bash
curl -X POST -H "Content-Type: text/plain" -d  "hello world" http://localhost:9000/api/echo
```



## Code details: TOC

The changes required on a Lagom service to handle File upload are:

1. A Play Router that can handles file upload
2. Append the Router to the main Lagom Server as an additional route

There are few more details on the recipe worth mentioning:

1. Service ACLs setup
2. Automated Tests


##### Code details: Add a new Play controller

This steps only requires creating a new Play Router based on [Play's Upload File example](https://github.com/playframework/play-scala-fileupload-example/blob/2.6.x/app/controllers/HomeController.scala#L26) and [JavaRoutingDsl](https://www.playframework.com/documentation/2.7.x/JavaRoutingDsl).

##### Code details: ACLs (optional)

We also set up the Service ACLs manually to add the `/api/files` endpoint on the ACLs' list so that the [Service Gateway](https://www.lagomframework.com/documentation/1.4.x/java/ServiceLocator.html#Service-Gateway) can reverse proxy external requests into the File Upload service.

 * In [FileUploadService](./fileupload-api/src/main/java/com/example/fileupload/api/FileUploadService.java) the Lagom Descriptor is built. There we added a dummy endpoint to demonstrate how the `FileUploadController` doesn't interfere with regular Service Implementations. The important detail on this `Service.Descriptor` is that a `ServiceAcl` for `/api/files` is added manually to the `Service.Descriptor`. 

##### Code details: tests

The recipe includes a couple of tests in `./fileupload-impl/src/test/java/` where you an see how the test code doesn't change when the additional router is added to the `Service`. Note though, how the tests for the file upload can't use the Lagom Client and use a plain `PlayWS` client to have complete control over the HTTP request built to [upload the file](https://www.playframework.com/documentation/2.6.x/JavaFileUpload). 

## To know more

This recipe uses default values that will limit the size of the uploaded file and doesn't dive deep into tuning options. Here's some resources in case you want to know more about the features Play provides to handle file upload in either the client or the server sides.

To know more about tuning file upload in Play see:

* [Writing a custom body parser](https://www.playframework.com/documentation/2.6.x/JavaBodyParsers#Writing-a-custom-body-parser)
* [Choosing a Body parser / Max content Length](https://www.playframework.com/documentation/2.6.x/JavaBodyParsers#Content-length-limits)
* [Handling File Upload](https://www.playframework.com/documentation/2.6.x/JavaFileUpload)

You may also be interested in the [Play-specific example](https://github.com/playframework/play-java-fileupload-example) on handling file uploads.
