package com.estsoft.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.imgscalr.Scalr;
import org.springframework.util.FileCopyUtils;

public class FileUtils {

	/**
	 * URL 로 파일 다운로드
	 * @param url
	 * @param download_path
	 */
	public static void fileUrlDownload(String url, String download_path) {
	
		OutputStream outStream = null;
		URLConnection uCon = null;
		InputStream is = null;

		int ByteRead, ByteWritten = 0;
		
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
	
	/**
	 * 업로드한 파일 생성
	 * @param uploadPath
	 * @param originalName
	 * @param fileData
	 * @return 업로드된 전체 파일경로
	 * @throws Exception 
	 */
	public static String uploadFile(String uploadPath, String originalName, byte[] fileData) throws Exception {
		
		// UUID 발급
		UUID uuid = UUID.randomUUID();
		
		// 저장할 파일명 = UUID + 원본이름
		String savedName = uuid.toString() + "_" + originalName;
		
		// 업로드할 디렉토리(날짜별 폴더) 생성 
		String savedPath = calcPath(uploadPath);
		
		// 파일 경로(기존의 업로드경로+날짜별경로), 파일명을 받아 파일 객체 생성
		File target = new File(uploadPath + savedPath, savedName);
		
		// 임시 디렉토리에 업로드된 파일을 지정된 디렉토리로 복사
		FileCopyUtils.copy(fileData, target);
		
		// 썸네일을 생성하기 위한 파일의 확장자 검사
		// 파일명이 aaa.bbb.ccc.jpg일 경우 마지막 마침표를 찾기 위해
		String formatName = originalName.substring(originalName.lastIndexOf(".")+1);
		String uploadedFileName = null;
		
		// 이미지 파일은 썸네일 사용
		// ** JPG, PNG, GIF 허용
		if(MediaUtils.getMediaType(formatName) != null) {
		    // 썸네일 생성
		    uploadedFileName = makeThumbnail(uploadPath, savedPath, savedName);
		} else {
			// 아이콘 생성
	        //uploadedFileName = makeIcon(uploadPath, savedPath, savedName);
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
		String yearPath = File.separator + cal.get(Calendar.YEAR);
		System.out.println(yearPath);
		
		// 월, ex) \\2017\\03
		String monthPath = yearPath + File.separator + new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
		System.out.println(monthPath);
		
		// 날짜, ex) \\2017\\03\\01
		String datePath = monthPath + File.separator + new DecimalFormat("00").format(cal.get(Calendar.DATE));
		System.out.println(datePath);
		
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
		if (new File(paths[paths.length - 1]).exists()){
		    return;
		}
		
		// 디렉토리가 존재하지 않으면
		for (String path : paths) {
		    
		    File dirPath = new File(uploadPath + path);
		    
		    // 디렉토리가 존재하지 않으면
		    if (!dirPath.exists()) {
		    	// 디렉토리 생성
		        dirPath.mkdir(); 
		    }
		}
	}    
	
	/**
	 * 썸네일 이미지 생성
	 * @param uploadPath
	 * @param path
	 * @param fileName
	 * @return 썸네일 이미지명
	 * @throws Exception
	 */
	private static String makeThumbnail(String uploadPath, String path, String fileName) throws Exception {
		
		// 이미지를 읽기 위한 버퍼
		BufferedImage sourceImg = ImageIO.read(new File(uploadPath + path, fileName));
		
		// 100픽셀 단위의 썸네일 생성
		BufferedImage destImg = Scalr.resize(sourceImg, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_HEIGHT, 100);
		
		// 썸네일의 이름을 생성(원본파일명에 's_'를 붙임)
		String thumbnailName = uploadPath + path + File.separator + "s_" + fileName;
		File newFile = new File(thumbnailName);
		String formatName = fileName.substring(fileName.lastIndexOf(".") + 1);
		
		// 썸네일 생성
		ImageIO.write(destImg, formatName.toUpperCase(), newFile);
		
		// 썸네일의 이름을 리턴함
		return thumbnailName.substring(uploadPath.length()).replace(File.separatorChar, '/');
	}
	
	/**
	 * 아이콘 생성
	 * @param uploadPath
	 * @param path
	 * @param fileName
	 * @return 아이콘명
	 * @throws Exception
	 */
	private static String makeIcon(String uploadPath, String path, String fileName) throws Exception {
		
		// 아이콘의 이름
		String iconName = uploadPath + path + File.separator + fileName;
		
		// 아이콘 이름을 리턴
		// File.separatorChar : 디렉토리 구분자
		// 윈도우 \ , 유닉스(리눅스) /        
		return iconName.substring(uploadPath.length()).replace(File.separatorChar, '/');
	}
	



}
