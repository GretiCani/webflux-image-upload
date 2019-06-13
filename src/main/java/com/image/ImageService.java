package com.image;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ImageService {

	public static String UPLOAD_ROOT = "upload-dir";

	final private ResourceLoader resourceLoader;
	final private ImageRepository imageRepository;

	public ImageService(ResourceLoader resourceLoader, ImageRepository imageRepository) {
		this.resourceLoader = resourceLoader;
		this.imageRepository = imageRepository;
	}

	public Flux<Image> findAllImges() {
		return imageRepository.findAll().log("findAll");
	}
	
	public Mono<Resource> findOneImage(String filename){
		return Mono.fromSupplier(()->
		            resourceLoader.getResource("file:"+UPLOAD_ROOT+"/"+filename+"/raw"))
				.log("findOneImage");
	}

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

	public Mono<Void> deleteImage(String filename) {
		Mono<Void> deletImageDatabase = imageRepository.findByName(filename).flatMap(imageRepository::delete);

		Mono<Void> deletFile = Mono.fromRunnable(() -> {
			try {
				Files.deleteIfExists(Paths.get(UPLOAD_ROOT, filename));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return Mono.when(deletImageDatabase, deletFile).then();
	}
	
	/*
	 * Copy some images on specified dir
	 */
	@Bean
	CommandLineRunner init()throws IOException{
		return (args)->{
			FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));
			Files.createDirectory(Paths.get(UPLOAD_ROOT));
			FileCopyUtils.copy("test-file-1", new FileWriter(UPLOAD_ROOT+"/image-1.png"));
			FileCopyUtils.copy("test-file-2", new FileWriter(UPLOAD_ROOT+"/image-2.png"));
			FileCopyUtils.copy("test-file-3", new FileWriter(UPLOAD_ROOT+"/image-3.png"));
			FileCopyUtils.copy("test-file-4", new FileWriter(UPLOAD_ROOT+"/image-4.png"));
			FileCopyUtils.copy("test-file-5", new FileWriter(UPLOAD_ROOT+"/image-5.png"));
			FileCopyUtils.copy("test-file-6", new FileWriter(UPLOAD_ROOT+"/image-6.png"));
			FileCopyUtils.copy("test-file-6", new FileWriter(UPLOAD_ROOT+"/image-7.png"));
			FileCopyUtils.copy("test-file-6", new FileWriter(UPLOAD_ROOT+"/image-8.png"));
			FileCopyUtils.copy("test-file-6", new FileWriter(UPLOAD_ROOT+"/image-9.png"));
			FileCopyUtils.copy("test-file-6", new FileWriter(UPLOAD_ROOT+"/image-10.png"));
		};
	}
}
