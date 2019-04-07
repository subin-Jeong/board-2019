package com.estsoft.web;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.estsoft.domain.File;
import com.estsoft.repository.FileRepository;
import com.estsoft.util.FileUtils;

@Controller
@RequestMapping("/board")
@Transactional
public class FileController {

	@Autowired
	private FileRepository fileRepository;
	
	static final String uploadDir = "./src/main/resources/static/upload/";
	
	/**
	 * 전체 첨부파일 리스트 불러오기
	 * @param bNo
	 * @return 전체 첨부파일 List
	 */
	@PostMapping("/getFile/{bNo}")
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
		
		// 연관 글번호
		int boardNo = Integer.parseInt(uploadData.get("boardNo"));
		
		// 등록시간
		Date date = new Date();
		
		// 첨부파일 리스트
		Iterator<String> iterator = uploadData.keySet().iterator();
		
		// 결과값 반환을 위한 JSON Object
		JSONObject resultObj = new JSONObject();

        while(iterator.hasNext()) {
        	
        	String key = iterator.next();
        	
        	// 붙여넣기 한 첨부파일
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
            		// URL에 포함된 파라미터 삭제
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
            	
            	try {
            		fileRepository.save(file);
					resultObj.put("result", HttpStatus.OK);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
            
            if(key.indexOf("uploaded") != -1) {
            	
            	File file = new File();
            	
            	// 파일명
            	String filename = uploadData.get(key); 
            	
            	// 확장자
            	String extension = "";
            	
            	// 지정된 확장자가 있으면 해당 확장자로 변경
            	if(filename.substring(filename.lastIndexOf("/")+1, filename.length()).contains(".")) {
            		extension = filename.substring(filename.lastIndexOf(".")+1, filename.length());
            	} 
            	
            	file.setBoardNo(boardNo);
            	file.setFilename(filename);
            	file.setRegDate(date);
            	file.setDelFlag("N");
            	
            	try {
            		fileRepository.save(file);
            		resultObj.put("result", HttpStatus.OK);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
        }
		
		return resultObj.toString();
	}
	
	
	/**
	 * 첨부파일 삭제
	 * @param fNo
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@PutMapping("/deleteFile/{fNo}")
	@ResponseBody
	public String delete(@PathVariable int fNo) {
		
		// 기존 데이터는 유지하되, delFlag = 'Y' 업데이트
		File file = fileRepository.findOne(fNo);
		
		file.setDelFlag("Y");
		fileRepository.save(file);
		
		return "/board/detail/" + file.getBoardNo();
	}	
	
		
	
	


}
