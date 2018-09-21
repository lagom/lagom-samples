# Lagom Recipe: How to upload a file

Lagom is designed with RPC in mind and abstracts from the transport used to exchange messages between services but it defaults to an HTTP/Json transport. File upload is a feature often requested that requires exploiting the power of one of Lagom's building blocks: the Play Framework.

This recipe demonstrates how to add a side-car `Controller` using pure Play code to handle file uploads next to your Lagom `Service`'s. This recipe is inspired on Play's example application [Play Java File Upload Example](https://github.com/playframework/play-java-fileupload-example/tree/2.6.x) that is part of [Play's great collection of examples](https://www.playframework.com/download#examples).

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

1. Add a new [Play controller](https://www.playframework.com/documentation/2.6.x/JavaActions) named `FileUploadController`
2. Create a new [`routes` file](https://www.playframework.com/documentation/2.6.x/JavaRouting) and add a new route pointing to our side-car `FileUploadController`
3. Fall back to Play's routing instead of using Lagom's default `Router`

There are few more details on the recipe worth mentioning:

1. Service ACLs setup
2. Automated Tests


##### Code details: Add a new Play controller

This steps only requires creating a new controller based on [Play's Upload File example](https://www.playframework.com/documentation/2.6.x/JavaFileUpload). The final controller is [`FileUploadController`](./fileupload-impl/src/main/java/com/example/play/controllers/FileUploadController.java)

##### Code details: Create a new `routes` file
  * we then create a file named `routes` in `./fileupload-impl/src/main/resources`. You can learn more about that file and it's syntax on the [docs](https://www.playframework.com/documentation/2.6.x/JavaRouting). In our case we want the `routes` file to contain only one entry:

            POST    /api/files      com.example.play.controllers.FileUploadController.upload()

##### Code details: Fall back to Play's routing instead of using Lagom's default `Router`

  * in the `build.sbt` project definiton we have to make a few changes. All changes need to be applied on the project definition of the implementation module. First we enable the [`PlayJava`](./build.sbt#L21) plugin that will let us use `routes`-based routing. Then, we're configuring Play to use the [`Injected routes generator`](https://www.playframework.com/documentation/2.6.x/JavaDependencyInjection#Injected-routes-generator). Finally, we  need to [disable](./build.sbt#L22) the `PlayLayoutPlugin` because we're using `sbt`'s default project structure instead of [Play's project structure](https://www.playframework.com/documentation/2.6.x/Anatomy). The last step is only required because we want to maintain Lagom's project structure.
  * next, in [`FileUploadModule`](./fileupload-impl/src/main/java/com/example/FileUploadModule.java#L15) we add a additional router (_Lagom 1.5+_) that the `PlayJava` sbt plugin will create for us from the `src/main/resources/routes` file we created above. This class `Routes` is created on the fly by the `PlayJava` plugin considering the `InjectedRoutesGenerator` setting in `build.sbt`.

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
