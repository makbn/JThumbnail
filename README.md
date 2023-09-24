## JThumbnail

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/17bbe0b4242d4f02a5d1a0288a6e6cbb)](https://app.codacy.com/app/makbn/JThumbnail?utm_source=github.com&utm_medium=referral&utm_content=makbn/JThumbnail&utm_campaign=Badge_Grade_Dashboard)
[![](https://jitpack.io/v/makbn/JThumbnail.svg)](https://jitpack.io/#makbn/JThumbnail)

JThumbnail is a Java library for creating Thumbnails of common types of file including `.doc`, `.docx`, `.pdf` , `.mp4` etc. [full list](#supported-file-formats)

- **Project is under development!**
- Check the `v1` branch for Java 8 compatible version. 
- Check the `dev/**` branches for latest commits on different under development version.
- Check the `release/**` branches for stable release versions
- `master` branch will always point to the latest available release version

## How to use

```java

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

### OPENOFFICE Properties

- **jthumbnailer.name:** Java Thumbnail Generator
  - Description: A thumbnail generation Java library for Office, PDF, HTML, Text, MP3, MPEG, and Image documents.

- **jthumbnailer.openoffice.port:** 2002, 2003, 2004
  - Description: Ports used by OpenOffice for document conversion.

- **jthumbnailer.openoffice.timeout:** 300,000 milliseconds (5 minutes)
  - Description: Timeout for OpenOffice document conversion tasks.

- **jthumbnailer.openoffice.max_tasks_per_process:** 25
  - Description: Maximum number of conversion tasks allowed per OpenOffice process.

- **jthumbnailer.openoffice.dir:** [OPEN_OFFICE_DIRECTORY]
  - Description: Directory path to the OpenOffice installation.

- **jthumbnailer.openoffice.tmp:** /private/tmp/jt
  - Description: Directory path for temporary files used by OpenOffice.

### JTHUMBNAILER Properties

- **jthumbnailer.thumb_width:** 400
  - Description: Width of generated thumbnails.

- **jthumbnailer.thumb_height:** 535
  - Description: Height of generated thumbnails.

- **jthumbnailer.async.core_pool_size:** 10
  - Description: Core pool size for the asynchronous processing tasks.

- **jthumbnailer.async.max_pool_size:** 32
  - Description: Maximum pool size for the asynchronous processing tasks.

### Spring

- **spring.servlet.multipart.max-file-size:** 30MB
  - Description: Maximum allowed file size for multipart file uploads.

- **spring.servlet.multipart.max-request-size:** 30MB
  - Description: Maximum allowed request size for multipart file uploads.

- **server.port:** 8081
  - Description: Port on which the application will be hosted.

### Spring Doc

- **springdoc.api-docs.path:** /api-docs
  - Description: Path for accessing the API documentation in JSON format.

- **springdoc.swagger-ui.path:** /swagger-ui.html
  - Description: Path for accessing the Swagger UI for interactive API documentation.


## Requirements

- Java JRE **17**
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

**Step 1**. Add the JitPack repository to your build file

```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

**Step 2**. Add the dependency

```xml
	<dependency>
	    <groupId>com.github.makbn</groupId>
	    <artifactId>JThumbnail</artifactId>
	    <version>${project version}</version>
	</dependency>
```

### Gradle

---

**Step 1**. Add it in your root build.gradle at the end of repositories:

```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2**. Add the dependency

```gradle
	dependencies {
	        implementation 'com.github.makbn:JThumbnail:${project version}'
	}
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
- - [ ] Fix problem with Java 1.8 (current version is 17)

## Original project

**JThumbnail** is based on an [old project](https://github.com/benjaminpick/java-thumbnailer) of the university of Siegen for the benefit of [come_IN Computerclubs](http://www.computerclub-comein.de). and thanks a lot to @benjaminpick
