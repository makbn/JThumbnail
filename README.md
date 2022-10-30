## JThumbnail

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/17bbe0b4242d4f02a5d1a0288a6e6cbb)](https://app.codacy.com/app/makbn/JThumbnail?utm_source=github.com&utm_medium=referral&utm_content=makbn/JThumbnail&utm_campaign=Badge_Grade_Dashboard)
[![](https://jitpack.io/v/makbn/JThumbnail.svg)](https://jitpack.io/#makbn/JThumbnail)

JThumbnail is a Java library for creating Thumbnails of common types of file including `.doc`, `.docx`, `.pdf` , `.mp4` and etc. [full list](#supported-file-formats)

-  **Project is under development!**
-  Check the `v1` branch for Java 8 compatible version.
-  Check the `dev` branch for latest commits.

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

-  `openoffice_ports` tcp ports for open office.
-  `openoffice_dir` open office home dir.
-  `temp_dir temp` directory for saving thumb files.
-  `thumb_height` thumbnail height size in px.
-  `thumb_width` thumbnail width size in px.

## Requirements

-  Java JRE **18**
-  OpenOffice 4 or LibreOffice _(optional)_

## Supported File Formats

-  Office files (`doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`)
  -  There is a problem with most xlsx files
-  OpenOffice files (all of them)
-  Text files (`txt`, `pdf`, `rtf`, `html`)
-  Image files (`jpg`, `png`, `bmp`, `gif`)
-  AutoCad files (`dwg`)
-  MP3 files (user album-art as thumbnail)
-  MPEG files (generate gif file)

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

-  [x] Update all dependencies from jar to maven
-  [x] update project old and deprecated dependencies
-  [x] Change the structure of project
-  [x] Add new Configuration method to config OpenOffice dir and port
-  [x] Add Async multi-thread support
-  [x] replace Thumbnailers for Microsoft Office documents with e-iceblue
-  [ ] Fix problem with Java 1.8 (current version is 18)
-  [ ] Fix problem with xlsx files
-  [ ] Improve code quality
-  [ ] Improve current Exception handling system

## Original project

**JThumbnail** is based on an [old project](https://github.com/benjaminpick/java-thumbnailer) of the university of Siegen for the benefit of [come_IN Computerclubs](http://www.computerclub-comein.de). and thanks alot to @benjaminpick
