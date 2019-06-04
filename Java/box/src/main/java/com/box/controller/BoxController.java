package com.box.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.box.service.BoxService;



@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class BoxController {
	
	@Autowired	
	BoxService boxService;
	
	@GetMapping("/")
	public ResponseEntity<String> getFilesandFolders(@RequestParam("path") String path) {		
		return new ResponseEntity<String>(boxService.getFilesandFolders(path),HttpStatus.OK);
	}
	
	@GetMapping("/fileDownloadUI")
	public ResponseEntity<String> fileDownloadUI(@RequestParam("path") String path) {		
		return new ResponseEntity<String>(boxService.fileDownloadUI(path),HttpStatus.OK);
	}
	
	@GetMapping("/fileDownload")
	public ResponseEntity<Resource> fileDownload(@RequestParam("path") String path) throws IOException {
		HttpHeaders headers = new HttpHeaders(); 
		File file = boxService.fileDownload(path, headers);
		Path filePath = Paths.get(file.getAbsolutePath());
	    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));
	    file.delete();
		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
    }

    @PostMapping("/fileUpload")
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file, @RequestParam("path") String path) {
    	return new ResponseEntity<String>(boxService.uploadFile(file, path),HttpStatus.OK);
    }
    
	/*
	 * @ExceptionHandler(Exception.class) public ResponseEntity<String>
	 * exceptionHandler(Exception e){ JsonObject jsonObject = new JsonObject();
	 * jsonObject.addProperty("message", e.getMessage().toString()); return new
	 * ResponseEntity<String>(jsonObject.toString(), HttpStatus.BAD_REQUEST); }
	 */	
}
