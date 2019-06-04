package com.box.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class BoxService {
	
	private static final Logger logger = Logger.getLogger(BoxService.class);
	private JsonObject jsonObject;
	private HttpResponse httpResponse;
	private final static String FILES_FOLDERS = "filesandfolders";
	private final static String DOWNLOAD = "download";
	private final static String UPLOAD = "upload";
	private boolean isLink = false;
	private File file;
	
	public String getFilesandFolders(String path) {
		StringBuffer sb = new StringBuffer();
		String url = getURL(FILES_FOLDERS, path, isLink);
		HttpEntity httpEntity = getGetResponseEntity(url, isLink);
		try {
			sb.append(EntityUtils.toString(httpEntity));			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return sb.toString();
	}
	
	public String fileDownloadUI(String path) {
		StringBuffer sb = new StringBuffer();
		String url = getURL(DOWNLOAD, path, isLink);
		HttpEntity httpEntity = getGetResponseEntity(url, isLink);
		try {
			sb.append(EntityUtils.toString(httpEntity));				
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return sb.toString();
	}
	
	public File fileDownload(String path, HttpHeaders headers) {
		StringBuffer sb = new StringBuffer();
		String url = getURL(DOWNLOAD, path, isLink);
		HttpEntity httpEntity = getGetResponseEntity(url, isLink);
		try {
			jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(httpEntity));
			sb.append(jsonObject.get("cloudElementsLink").getAsString());						
			
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return writeDataToFile(sb.toString(), headers, path);
	}

	public File writeDataToFile(String link, HttpHeaders headers, String path) {
		HttpEntity httpEntity = getGetResponseEntity(link, true);
		try {
			InputStream is = httpEntity.getContent();
			headers.setContentLength(httpEntity.getContentLength()); 
			headers.add("Content-Type", httpEntity.getContentType().getValue());
			headers.add("Content-Disposition", "attachment; filename=" + FilenameUtils.getName(path));
			file = File.createTempFile(FilenameUtils.removeExtension(path), "."+FilenameUtils.getExtension(path));
			FileOutputStream fos = new FileOutputStream(file);
			int inByte;
			while((inByte = is.read()) != -1)
			     fos.write(inByte);
			is.close();
			fos.close();
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return file;
	}
	
	public String uploadFile(MultipartFile file, String path) {
		StringBuffer resString = new StringBuffer();
		StringBuffer pathUri = new StringBuffer();
		if(path.isEmpty()) {
			pathUri.append("/");
		}else {
			pathUri.append(path);
			pathUri.append("/");
		}
		pathUri.append(file.getOriginalFilename());
		String url = getURL(UPLOAD, pathUri.toString(), isLink);
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(".");
			String prefix= FilenameUtils.removeExtension(file.getOriginalFilename());
			sb.append(FilenameUtils.getExtension(file.getOriginalFilename()));
			File tempFile = File.createTempFile(prefix, sb.toString());

			tempFile.deleteOnExit();

			file.transferTo(tempFile);

			HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("file", new FileBody(tempFile))
                    .build();

			HttpPost request = new HttpPost(url);
			request.setHeader("Authorization", "User X8wWBSY8mfE5yfqMiujApXxkGTOWPbpeXBLrY2a7OGY=, Organization 7efe3f247d5c7960979d8671083a37db, Element UTqvCqyOE+YiBj1QllBrkYhREP0SBmKlUqmT1/hdASE=\"");
			request.setEntity(entity);
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(request);
			JsonObject result = (JsonObject) new JsonParser().parse(EntityUtils.toString(response.getEntity()));
			resString.append(result.toString());
			tempFile.delete();

		} catch (Exception e) {
			logger.error(e.getMessage());			
		} 
		return resString.toString();
	}
	
	public String getURL(String reqType, String reqContent, boolean link) {
		StringBuffer sb = new StringBuffer();
		StringBuffer uriString = new StringBuffer();
		if(!link) {
			if(reqType.equals("filesandfolders")) {
				sb.append("folders/contents?path=");			
			}else if(reqType.equals("download")) {
				sb.append("files/links?path=");
							
			}else if(reqType.equals("upload")) {
				sb.append("files?path=");
			}
			try {
				if(!reqContent.isEmpty()) {
					sb.append(URLEncoder.encode(reqContent, "UTF-8"));
				}else {
					sb.append("/");
				}			
			}catch (Exception e) {
				logger.error(e.getMessage());
			}
			UriComponents uri = UriComponentsBuilder
	                .fromHttpUrl("https://staging.cloud-elements.com/elements/api-v2/{extention}")
	                .buildAndExpand(sb.toString());
			uriString.append(uri.toUriString());
		}else {
			uriString.append(reqContent);
		}
		return uriString.toString();
	}
	
	public HttpEntity getGetResponseEntity(String url, boolean link) {
		try {
			HttpGet request = new HttpGet(url);
	         
	        //Set the API media type in http accept header
	        if(!link) {
	        	request.addHeader("Authorization", "User X8wWBSY8mfE5yfqMiujApXxkGTOWPbpeXBLrY2a7OGY=, Organization 7efe3f247d5c7960979d8671083a37db, Element UTqvCqyOE+YiBj1QllBrkYhREP0SBmKlUqmT1/hdASE=\"");
	        }
	          
	        //Send the request; It will immediately return the response in HttpResponse object
	        HttpClient client = HttpClientBuilder.create().build();
			httpResponse = client.execute(request);
	         
	        //verify the valid error code first
	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if (statusCode != 200)
	        {
	        	logger.info("Failed with HTTP error code : " + statusCode);
	            throw new RuntimeException("Failed with HTTP error code : " + statusCode);	            
	        }	        
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return httpResponse.getEntity();
	}
			
}
