package com.estsoft.web;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.estsoft.auth.SecurityConfig;
import com.estsoft.domain.api.File;
import com.estsoft.repository.api.FileRepository;
import com.estsoft.util.FileUtils;

@Controller
@RequestMapping("/board")
@Transactional
public class FileController {

	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
		
	@Autowired
	private FileRepository fileRepository;
	
	// ���ε� ���
	static final String uploadDir = "./src/main/resources/static/upload/";
	
	// ���ε� ���� ����
	static final int UPLOAD_LIMIT = 5;
	
	/**
	 * ��ü ÷������ ����Ʈ �ҷ�����
	 * @param bNo
	 * @return ��ü ÷������ List
	 */
	@PostMapping("/getFile/{bNo}")
	@ResponseBody 
	public List<File> getFile(@PathVariable int bNo) {
		return fileRepository.findAllByBoardNoOrdering(bNo);
	}
	
	/**
	 * ���ε��� ÷������ ����
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
	 * ���ε� ÷������ ��� 
	 * @param uploadData
	 * @return ���ε� ���
	 */
	@PostMapping("/upload")
	@ResponseBody 
	public String upload(@RequestBody Map<String, String> uploadData) {
		
		// ���� ���ε� Ȯ��
		String UPLOAD_STATUS = "N";
		
		// ���� �۹�ȣ
		int boardNo = Integer.parseInt(uploadData.get("boardNo"));
		
		// ��Ͻð�
		Date date = new Date();
		
		// ÷������ ����Ʈ
		Iterator<String> iterator = uploadData.keySet().iterator();
		
        while(iterator.hasNext()) {
        	
        	String key = iterator.next();
        	
        	// �ٿ��ֱ� �� ÷������
            if(key.indexOf("url") != -1) {
            	
            	File file = new File();
            	
            	// �ٿ�ε� ���
            	String fileURL = uploadData.get(key);
            	
            	// ���ϸ�
            	String filename = "img_" + System.currentTimeMillis() + "_" + boardNo; 
            	
            	// Ȯ����
            	String extension = "png";
            	
            	// ������ Ȯ���ڰ� ������ �ش� Ȯ���ڷ� ����
            	if(fileURL.substring(fileURL.lastIndexOf("/")+1, fileURL.length()).contains(".")) {
            		// URL�� ���Ե� �Ķ���� ����
            		int checkURL = fileURL.indexOf("?", fileURL.lastIndexOf(".")+1);
            		if(checkURL == -1) {
            			extension = fileURL.substring(fileURL.lastIndexOf(".")+1, fileURL.length());
            		} else {
            			extension = fileURL.substring(fileURL.lastIndexOf(".")+1, checkURL);
            		}
            	} 
            	
            	
            	FileUtils.fileUrlDownload(fileURL,  uploadDir + filename + "." + extension);
            	
            	file.setBoardNo(boardNo);
            	file.setFilename(filename + "." + extension);
            	file.setUrl(fileURL);
            	file.setRegDate(date);
            	file.setDelFlag("N");
            	
            	// ÷������ ���� ���� Ȯ��
				if(checkFileCount(boardNo)) {
					
					fileRepository.save(file);
					UPLOAD_STATUS =  "Y";
					
				} else {
				
					return "OVER";
				}
            }
            
            if(key.indexOf("uploaded") != -1) {
            	
            	File file = new File();
            	
            	// ���ϸ�
            	String filename = uploadData.get(key); 
            	
            	// Ȯ����
            	String extension = "";
            	
            	// ������ Ȯ���ڰ� ������ �ش� Ȯ���ڷ� ����
            	if(filename.substring(filename.lastIndexOf("/")+1, filename.length()).contains(".")) {
            		extension = filename.substring(filename.lastIndexOf(".")+1, filename.length());
            	} 
            	
            	file.setBoardNo(boardNo);
            	file.setFilename(filename);
            	file.setRegDate(date);
            	file.setDelFlag("N");
            	
            	// ÷������ ���� ���� Ȯ��
				if(checkFileCount(boardNo)) {
					
					fileRepository.save(file);
					UPLOAD_STATUS =  "Y";
					
				} else {
				
					return "OVER";
				}
            }
        }
		
        return UPLOAD_STATUS;
	}
	
	
	/**
	 * ÷������ ����
	 * @param fNo
	 * @return �����̷�Ʈ �� �� ������
	 */
	@DeleteMapping("/deleteFile/{fNo}")
	@ResponseBody
	public String delete(@PathVariable int fNo) {
		
		// ���� �����ʹ� �����ϵ�, delFlag = 'Y' ������Ʈ
		File file = fileRepository.findOne(fNo);
		
		file.setDelFlag("Y");
		fileRepository.save(file);
		
		return "/board/detail/" + file.getBoardNo();
	}	
	
	
	/**
	 * ÷������ ���ε� ���� ����
	 * @param boardNo
	 * @return true / false
	 */
	private boolean checkFileCount(int boardNo) {
		
		// ÷������ ���� ����
		int fileCnt = fileRepository.countByBoardNo(boardNo);
		
		log.info("File Count : " + fileCnt);
		
		if(fileCnt < UPLOAD_LIMIT) {
			return true;
		} else {
			return false;
		}
		
	}
	
	


}
