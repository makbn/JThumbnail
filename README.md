## JThumbnail

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/17bbe0b4242d4f02a5d1a0288a6e6cbb)](https://app.codacy.com/app/makbn/JThumbnail?utm_source=github.com&utm_medium=referral&utm_content=makbn/JThumbnail&utm_campaign=Badge_Grade_Dashboard)
[![](https://jitpack.io/v/makbn/JThumbnail.svg)](https://jitpack.io/#makbn/JThumbnail)

JThumbnail is a Java library for creating Thumbnails of common types of file including `.doc`, `.docx`, `.pdf` , `.mp4` etc. [full list](#supported-file-formats)

- **Project is under development!**
- Check the `v1` branch for Java 8 compatible version. 
- Check the `dev/**` branches for latest commits on different under development version.
- Check the `release/**` branches for stable release versions
- `master` branch will always point to the latest available release version

Project Source: https://github.com/makbn/JThumbnail

## How to use

```jshelllanguage

String[] args = new String[]{};

JThumbnailer jThumbnailer = JThumbnailerStarter.init(args);

File in = new File("/inputFile.docx");

ThumbnailCandidate candidate = new ThumbnailCandidate(in,"unique_code");

jThumbnailer.run(candidate, new ThumbnailListener() {
     @Override
     public void onThumbnailReady(String hash, File thumbnail) {
        Files.copy(thumbnail.toPath(), Path.of("my_thumbnail_folder", thumbnail.getName()), StandardCopyOption.REPLACE_EXISTING);
     }

     @Override
     public void onThumbnailFailed(String hash, String message, int code) {
        // handle the situation
     }
});

// close thumbnailer
jThumbnailer.close();

```

## Configuration Args

| Configuration Args                       | Description                                                         |
| ---------------------------------------- |---------------------------------------------------------------------|
| **OpanAPI properties**                   |                                                                     |
| jthumbnailer.openapi.name                | Application name, e.g. Java Thumbnail Generator in Swagger UI       |
| jthumbnailer.openapi.desc                | Description of the application in Swagger UI                        |
| jthumbnailer.openapi.license             | License of the application as displayed in Swagger UI               |
| jthumbnailer.openapi.url                 | URL pointing to the license text for Swagger UI                     |
| **OPENOFFICE Properties**                |                                                                     |
| jthumbnailer.openoffice.ports            | Ports used by OpenOffice for document conversion                    |
| jthumbnailer.openoffice.timeout          | Timeout for OpenOffice document conversion tasks                    |
| jthumbnailer.openoffice.max_tasks_per_process | Maximum number of conversion tasks allowed per OpenOffice process   |
| jthumbnailer.openoffice.office_home      | Directory path to the OpenOffice installation                       |
| jthumbnailer.openoffice.working_dir      | Working directory path for OpenOffice                               |
| jthumbnailer.openoffice.tmp_dir          | Directory path for temporary files generated and used by OpenOffice |
| **JTHUMBNAILER Properties**              |                                                                     |
| jthumbnailer.thumbnail.thumb_width       | Width of generated thumbnails                                       |
| jthumbnailer.thumbnail.thumb_height      | Height of generated thumbnails                                      |
| **JTHUMBNAILER Async Properties          |                                                                     |
| jthumbnailer.async.core_pool_size        | Core pool size for the asynchronous processing tasks                |
| jthumbnailer.async.max_pool_size         | Maximum pool size for the asynchronous processing tasks             |
| **JTHUMBNAILER Server Properties**       |                                                                     |
| jthumbnailer.server.upload_directory     | Directory used to store uploads when using API                      |
| jthumbnailer.server.max_waiting_list_size| Queue for files to be processed                                     |
| **Spring**                               |                                                                     |
| spring.servlet.multipart.max-file-size   | Maximum allowed file size for multipart file uploads                |
| spring.servlet.multipart.max-request-size| Maximum allowed request size for multipart file uploads             |
| server.port                              | Port on which the Web application will be hosted                    |
| **Spring Doc**                           |                                                                     |
| springdoc.api-docs.path                  | Path for accessing the API documentation in JSON format             |
| springdoc.swagger-ui.path                | Path for accessing the Swagger UI for interactive API documentation |

- All parameters can be passed through environment variables. To pass a param as environment variable you need to replace the dots with underscore and use uppercase. For example, `jthumbnailer.openoffice.office_home` should be 
`JTHUMBNAILER_OPENOFFICE_OFFICEHOME` (see [Spring documentation](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding).

## Requirements

- Java JRE **21**
- OpenOffice 4.x or LibreOffice 7.x

## Supported File Formats

- Office files (`doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`)
  -  There is a problem with most xlsx files 
- OpenOffice files (all of them)
- Text files (`txt`, `pdf`, `rtf`, `html`)
- Image files (`jpg`, `png`, `bmp`, `gif`)
- AutoCad files (`dwg`)
- MP3 files (user album-art as thumbnail)
- MPEG files (generate gif file)

## Adding Repository

### Maven

---
**Step 1**. Add the dependency to the `pom` file:

```xml
<dependency>
  <groupId>io.github.makbn</groupId>
  <artifactId>jthumbnail</artifactId>
  <version>2.2.1</version>
</dependency>
```

### Gradle

---

**Step 1**. Add the dependency to the `build.gradle` file:

```gradle
compile "io.github.makbn:jthumbnail:2.2.1"
```

## TODO

- [x] Update all dependencies from jar to maven
- [x] update project old and deprecated dependencies
- [x] Change the structure of project
- [x] Add new Configuration method to config OpenOffice dir and port
- [x] Add Async multi-thread support
- [x] replace Thumbnailers for Microsoft Office documents with e-iceblue
- [x] Fix problem with xlsx files
- [x] Improve code quality
- [x] Improve current Exception handling system
- [ ] Fixing issue with running test with the GitHub action: use `UNIX domain sockets` instead of TCP to run openoffice for tests
- [ ] add rate limit to the project API 

## Original project

**JThumbnail** is based on an [old project](https://github.com/benjaminpick/java-thumbnailer) of the university of Siegen for the benefit of [come_IN Computerclubs](http://www.computerclub-comein.de). and thanks a lot to @benjaminpick
