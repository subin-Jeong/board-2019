package com.estsoft.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.estsoft.domain.Board;
import com.estsoft.domain.File;
import com.estsoft.repository.FileRepository;

@Controller
@RequestMapping("/board")
@Transactional
public class FileController {

	@Autowired
	private FileRepository fileRepository;
	
	static final String uploadDir = "./src/main/resources/static/upload/";
	
	//───────────────────────────────────────
	// 첨부파일 리스트
	//───────────────────────────────────────
	@PostMapping("/getFile/{bNo}")
	@ResponseBody 
	public List<File> getFile(@PathVariable int bNo) {
	
		return fileRepository.findByBoardNo(bNo);
	}
	
	
	//───────────────────────────────────────
	// 첨부파일 저장
	//───────────────────────────────────────
	@PostMapping("/upload")
	@ResponseBody 
	public String upload(@RequestBody Map<String, String> uploadData) {
		
		// 연관 글번호
		int boardNo = Integer.parseInt(uploadData.get("boardNo"));
		
		// 등록시간
		Date date = new Date();
		
		// 붙여넣기한 파일 src
		Iterator<String> iterator = uploadData.keySet().iterator();
		
		// 결과값 반환을 위한 JSON Object
		JSONObject resultObj = new JSONObject();

        while(iterator.hasNext()) {
        	
        	String key = iterator.next();
            if(key.indexOf("url") != -1) {
            	
            	File file = new File();
            	
            	// 다운로드 경로
            	String fileURL = uploadData.get(key);
            	
            	// 파일명
            	String filename = "img_" + System.currentTimeMillis() + "_" + boardNo; 
            	
            	// 확장자
            	String extension = "png";
            	
            	// 지정된 확장자가 있으면 해당 확장자로 변경
            	if(fileURL.substring(fileURL.lastIndexOf("/")+1, fileURL.length()).contains(".")) {
            		extension = fileURL.substring(fileURL.lastIndexOf(".")+1, fileURL.length());
            	} 
            	
            	
            	fileUrlDownload(fileURL,  uploadDir + filename + "." + extension);
            	
            	file.setBoardNo(boardNo);
            	file.setFilename(filename + "." + extension);
            	file.setUrl(fileURL);
            	file.setRegDate(date);
            	
            	try {
            		
            		
            		
            		fileRepository.save(file);
					resultObj.put("result", "OK");
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
        }
		
		return resultObj.toString();
	}

	public void fileUrlDownload(String url, String download_path) {
	
		OutputStream outStream = null;
		URLConnection uCon = null;
		InputStream is = null;

		int ByteRead, ByteWritten = 0;
		
		// Create a new trust manager that trust all certificates
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        public void checkClientTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    }
		};

		// Activate the new trust manager
		try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}


		try {
			
			URL Url = new URL(url);
			outStream = new FileOutputStream(download_path);
			
			uCon = Url.openConnection();
			is = uCon.getInputStream();
			
			byte[] buf = new byte[2048];
			
			while ((ByteRead = is.read(buf)) != -1) {
				
				outStream.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
				
			}
			
			System.out.println("total-size:"+ByteWritten+" byte");
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				
				if(is != null)is.close();
				if(outStream != null)outStream.close();
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		
		}


	}



}
