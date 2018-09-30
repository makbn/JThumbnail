## JThumbnail

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/17bbe0b4242d4f02a5d1a0288a6e6cbb)](https://app.codacy.com/app/makbn/JThumbnail?utm_source=github.com&utm_medium=referral&utm_content=makbn/JThumbnail&utm_campaign=Badge_Grade_Dashboard)

JThumbnail is a Java library for creating Thumbnails of common file types of file line `.doc`, `.docx`, `.pdf` and ...[full list](#supported-file-formats)

*   Project is under development! maven and gradle will be out soon.

## How to use

```java
try {
    Thumbnailer.start();
    File in = new File("/files/pdf/test1.pdf");
    if(in.exists()) {
        File out = Thumbnailer.createThumbnail(in);
        System.out.println("FILE created at : " + out.getAbsolutePath());
    }
} catch (FileDoesNotExistException e) {
    e.printStackTrace();
} catch (ThumbnailerException e) {
    e.printStackTrace();
} catch (IOException e) {
    e.printStackTrace();
}
```

## Requirements

*   Java JRE 1.8
*   (optional) OpenOffice 4 or LibreOffice

## Supported File Formats

*   Office files (`doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`)
*   OpenOffice files (all of them)
*   Text files (`txt`, `pdf`, `rtf`, `html`)
*   Image files (`jpg`, `png`, `bmp`, `gif`)
*   AutoCad files (`dwg`)

## TODO

*   [x] Update all dependenciesfrom jar to maven
*   [x] update project old and deprecated depencencies
*   [x] Change the structure of project
*   [ ] Add new Configuration method to confige OpenOffice dir and port 
*   [ ] Add Async multi-thread support

## Original project

**JThumbnail** is based on an [old project](https://github.com/benjaminpick/java-thumbnailer) of the university of Siegen for the benefit of [come_IN Computerclubs](http://www.computerclub-comein.de). and thanks alot to @ benjaminpick
