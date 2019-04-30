package com.estsoft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import com.estsoft.api.security.SecurityConfig;

public class FileUtils {

	// Log
	private static Logger log = LoggerFactory.getLogger(FileUtils.class);
		
	/**
	 * URL 로 파일 다운로드
	 * @param url
	 * @param uploadPath
	 * @param fileName
	 */
	public static String fileUrlDownload(String url, String uploadPath, String fileName) {
	
		OutputStream outStream = null;
		URLConnection urlConnection = null;
		InputStream is = null;

		int byteRead = 0;
		int byteWritten = 0;
		
		// 업로드할 디렉토리(날짜별 폴더) 생성 
		String savedPath = calcPath(uploadPath);
		
		// 업로드 경로 + 날짜별 디렉토리 + 파일명 (업로드 성공 시에만 확인 가능)
		String uploadedFileName = null;
		
		try {
			
			URL Url = new URL(url);
			outStream = new FileOutputStream(uploadPath + savedPath + fileName);
			
			urlConnection = Url.openConnection();
			is = urlConnection.getInputStream();
			
			byte[] buf = new byte[2048];
			
			while((byteRead = is.read(buf)) != -1) {
				
				outStream.write(buf, 0, byteRead);
				byteWritten += byteRead;
				
			}
			
			log.info("[FILE TOTAL SIZE] " + byteWritten + " BYTE");
			
			// 파일 사이즈 확인
			if(byteWritten > 0) {
				uploadedFileName = savedPath + fileName;
			}
		
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				
				if(is != null) {
					is.close();
				}
				
				if(outStream != null) {
					outStream.close();
				}
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		return uploadedFileName;
	}
	
	/**
	 * 업로드한 파일 생성
	 * @param uploadPath
	 * @param originalName
	 * @param fileData
	 * @return 업로드된 전체 파일경로
	 * @throws Exception 
	 */
	public static String uploadFile(String uploadPath, String originalName, byte[] fileData) throws Exception {
		
		// 업로드 경로 + 날짜별 디렉토리 + 파일명 (업로드 성공 시에만 확인 가능)
		String uploadedFileName = null;
				
		// 업로드할 디렉토리(날짜별 폴더) 생성 
		String savedPath = calcPath(uploadPath);
		
		// 저장할 파일명
		String fileName = "img_" + System.currentTimeMillis() + "_" + originalName;
		
		// 확장자
		String extension = originalName.substring(originalName.lastIndexOf(".") + 1);
		
		// ** JPG, PNG, GIF 만 허용
		if(MediaUtils.getMediaType(extension) != null) {
			
			uploadedFileName = savedPath + fileName;
			
			// 파일 경로(기존의 업로드경로 + 날짜별경로), 파일명을 받아 파일 객체 생성
			File target = new File(uploadPath + uploadedFileName);
			
			// 임시 디렉토리에 업로드된 파일을 지정된 디렉토리로 복사
			FileCopyUtils.copy(fileData, target);
		}

		return uploadedFileName;
	}
	
	/**
	 * 날짜별 디렉토리 생성
	 * @param uploadPath
	 * @return 생성된 디렉토리 경로
	 */
	private static String calcPath(String uploadPath) {
		
		Calendar cal = Calendar.getInstance();
		
		// File.separator : 디렉토리 구분자(\\)
		// 연도, ex) \\2017 
		String yearPath = "/" + cal.get(Calendar.YEAR);
		log.info(yearPath);
		
		// 월, ex) \\2017\\03
		String monthPath = yearPath + "/" + new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
		log.info(monthPath);
		
		// 날짜, ex) \\2017\\03\\01
		String datePath = monthPath + "/" + new DecimalFormat("00").format(cal.get(Calendar.DATE)) + "/";
		log.info(datePath);
		
		// 디렉토리 생성 메서드 호출
	    makeDir(uploadPath, yearPath, monthPath, datePath);
	    return datePath;
	}
	
	/**
	 * 디렉토리 생성
	 * @param uploadPath
	 * @param paths
	 */
	private static void makeDir(String uploadPath, String... paths) {
		
		// 디렉토리가 존재하면
		if(new File(paths[paths.length - 1]).exists()){
		    return;
		}
		
		// 디렉토리가 존재하지 않으면
		for(String path : paths) {
		    
		    File dirPath = new File(uploadPath + path);
		    
		    // 디렉토리가 존재하지 않으면
		    if(!dirPath.exists()) {
		    	// 디렉토리 생성
		        dirPath.mkdir(); 
		    }
		}
	}    

}
