package com.image;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ImageController {

	private static final String BASE_PATH="/upload-dir";
	private static final String FILENAME="{filename:.+}";
	
	private final ImageService imageService;
	
	public ImageController(ImageService imageService) {
		this.imageService=imageService;
	}
	
	@GetMapping("/")
	public Mono<String>getIndex(Model model){
		model.addAttribute("images",imageService.findAllImges());
		return Mono.just("index");
	}
	
	@GetMapping(value= BASE_PATH + "/" + FILENAME + "/raw", produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Mono<ResponseEntity<?>> getOneImage(@PathVariable String filename){
		return imageService.findOneImage(filename)
				.map(resource->{
					try {
						return ResponseEntity.ok()
								.contentLength(resource.contentLength())
								.body(new InputStreamResource(resource.getInputStream()));
					} catch (IOException e) {
						return ResponseEntity
						          .badRequest()
						            .body("Could not find "+filename+" ==>"+e.getMessage());
					}
				});
	}
	
	@PostMapping(BASE_PATH)
	public Mono<String>createImage(@RequestPart(name="file") Flux<FilePart>files){
		return imageService.createImage(files).then(Mono.just("redirect:/"));
	}
	
	@DeleteMapping(BASE_PATH +"/"+ FILENAME)
	public Mono<String> deleteImage(@PathVariable String filename) {
		return imageService.deleteImage(filename).then(Mono.just("redirect:/"));
	}
	
}

   
   
