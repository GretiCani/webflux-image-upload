# webflux-image-upload
multfile upload using spring boot , webflux, mongodb and thymeleaf 
```java
public Mono<Void> createImage(Flux<FilePart> files) {
		return files.flatMap(file -> {
			Mono<Image> createDatabaseImage = imageRepository
					.save(new Image(UUID.randomUUID().toString(), file.filename()))
					.log("createImage-save");

			Mono<Void> copyImage = Mono.just(Paths.get(UPLOAD_ROOT, file.filename()).toFile())
					.log("createImage-pickTarget")
					.map(destfile -> {
						try {
							destfile.createNewFile();
							return destfile;
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}).log("createImage-path")
					.flatMap(file::transferTo)
					.log("createImage-flatMap");
			return Mono.when(createDatabaseImage, copyImage);
		}).then()
				.log("createImage-done");
	}
  ```
