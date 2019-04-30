package com.estsoft.api.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.estsoft.api.domain.File;
import com.estsoft.api.repository.FileRepository;
import com.estsoft.api.security.SecurityConfig;
import com.estsoft.util.ApiUtils;
import com.estsoft.util.FileUtils;

@Controller
@RequestMapping("/board")
@Transactional
public class FileController {

	// Log
	private Logger log = LoggerFactory.getLogger(FileController.class);
		
	@Autowired
	private FileRepository fileRepository;
	
	// 업로드 경로
	static final String uploadDir = "C:/upload";
	
	// 업로드 개수 제한
	static final int UPLOAD_LIMIT = 5;
	
	/**
	 * 전체 첨부파일 리스트 불러오기
	 * @param bNo
	 * @return 전체 첨부파일 List
	 */
	@GetMapping("/file/{bNo}")
	@ResponseBody 
	public List<File> getFile(@PathVariable int bNo) {
		return fileRepository.findAllByBoardNoOrdering(bNo);
	}
	
	/**
	 * 업로드한 첨부파일 저장
	 * @param file
	 * @return Response Entity
	 * @throws IOException
	 * @throws Exception
	 */
	@PostMapping("/uploadAjax")
	@ResponseBody
	public ResponseEntity<String> uploadAjax(MultipartFile file) throws IOException, Exception {
		return new ResponseEntity<String>(FileUtils.uploadFile(uploadDir, file.getOriginalFilename(), file.getBytes()), HttpStatus.OK);
	}
	
	
	/**
	 * 업로드 첨부파일 등록 
	 * @param uploadData
	 * @return 업로드 결과
	 */
	@PostMapping("/upload")
	@ResponseBody 
	public String upload(@RequestBody Map<String, String> uploadData) {
		
		// 파일 업로드 확인
		String uploadStatus = "N";
		
		// 연관 글번호
		int boardNo = Integer.parseInt(uploadData.get("boardNo"));
		
		// 등록시간
		Date date = new Date();
		
		// 첨부파일 리스트
		Iterator<String> iterator = uploadData.keySet().iterator();
		
        while(iterator.hasNext()) {
        	
        	String key = iterator.next();
        	
        	// 붙여넣기 한 첨부파일
            if(key.indexOf("url") != -1) {
            	
            	File file = new File();
            	
            	// 다운로드 경로
            	String fileURL = uploadData.get(key);
            	
            	// 파일명
            	String fileName = "img_" + System.currentTimeMillis() + "_" + boardNo; 
            	
            	// 확장자
            	String extension = "png";
            	
            	// 지정된 확장자가 있으면 해당 확장자로 변경
            	if(fileURL.substring(fileURL.lastIndexOf("/")+1, fileURL.length()).contains(".")) {
            		// URL에 포함된 파라미터 삭제
            		int checkURL = fileURL.indexOf("?", fileURL.lastIndexOf(".")+1);
            		if(checkURL == -1) {
            			extension = fileURL.substring(fileURL.lastIndexOf(".")+1, fileURL.length());
            		} else {
            			extension = fileURL.substring(fileURL.lastIndexOf(".")+1, checkURL);
            		}
            	} 
            	
            	
            	String uploadedFileName = FileUtils.fileUrlDownload(fileURL, uploadDir, fileName + "." + extension);
            	
            	if(ApiUtils.isNotNullString(uploadedFileName)) {
            		
                	file.setBoardNo(boardNo);
                	file.setFileName(uploadedFileName);
                	file.setUrl(fileURL);
                	file.setRegDate(date);
                	file.setDelFlag("N");
                	
                	// 첨부파일 개수 제한 확인
    				if(checkFileCount(boardNo)) {
    					
    					fileRepository.save(file);
    					uploadStatus =  "Y";
    					
    				} else {
    				
    					return "OVER";
    				}
            	}
            }
            
            if(key.indexOf("uploaded") != -1) {
            	
            	File file = new File();
            	
            	// 파일명
            	String fileName = uploadData.get(key); 
            	
            	file.setBoardNo(boardNo);
            	file.setFileName(fileName);
            	file.setRegDate(date);
            	file.setDelFlag("N");
            	
            	// 첨부파일 개수 제한 확인
				if(checkFileCount(boardNo)) {
					
					fileRepository.save(file);
					uploadStatus =  "Y";
					
				} else {
				
					return "OVER";
				}
            }
        }
		
        return uploadStatus;
	}
	
	
	/**
	 * 첨부파일 삭제
	 * @param fNo
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@DeleteMapping("/deleteFile/{fNo}")
	@ResponseBody
	public String delete(@PathVariable int fNo) {
		
		// 기존 데이터는 유지하되, delFlag = 'Y' 업데이트
		File file = fileRepository.findOne(fNo);
		
		file.setDelFlag("Y");
		fileRepository.save(file);
		
		return "/board/detail/" + file.getBoardNo();
	}	
	
	
	/**
	 * 첨부파일 업로드 개수 제한
	 * @param boardNo
	 * @return true / false
	 */
	private boolean checkFileCount(int boardNo) {
		
		// 첨부파일 개수 제한
		int fileCnt = fileRepository.countByBoardNo(boardNo);
		
		log.info("[FILE COUNT] " + fileCnt);
		
		if(fileCnt < UPLOAD_LIMIT) {
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * 파일 다운로드
	 * @param fNo
	 * @param response
	 * @throws IOException
	 */
	@GetMapping("/download/{fNo}")
	public void download(@PathVariable int fNo, HttpServletResponse response) throws IOException {
		
		File file = fileRepository.findOne(fNo);
		
		if(file != null) {

			java.io.File downloadFile = new java.io.File(uploadDir + file.getFileName());
			String fileName = file.getFileName().substring(12);
			
			response.setContentLength((int)downloadFile.length());
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ";");
			response.setHeader("Content-Transfer-Encoding", "binary");
			
			OutputStream out = response.getOutputStream();
			FileInputStream fis = null;
			
			try {
				
				fis = new FileInputStream(downloadFile);
				FileCopyUtils.copy(fis, out);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				
				if(fis != null) {
					fis.close();
				}
			}
			
			out.flush();
			
		}
	}
	
	


}
