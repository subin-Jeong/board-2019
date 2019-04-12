package com.estsoft.web;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.estsoft.domain.Board;
import com.estsoft.repository.BoardRepository;

@Controller
@RequestMapping("/board")
@Transactional
public class BoardController {

	@Autowired
	private BoardRepository boardRepository;
	
	/**
	 * 전체 게시글 목록
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/list")
	public String list() {
		return "/board/list";
	}
	
	/**
	 * 전체 게시글 목록 불러오기
	 * @return 전체 게시글 목록 List
	 */
	@PostMapping("/getList/{pageNum}")
	@ResponseBody 
	public Page<Board> getList(@PageableDefault(size = 100) Pageable pageable, @PathVariable int pageNum) {
		PageRequest pageRequest = new PageRequest(pageNum - 1, pageable.getPageSize());	
		return boardRepository.findAllOrdering(pageRequest);
	}
	
	/**
	 * 게시글 등록 페이지
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/write")
	public String write() {
		return "/board/write";
	}
	
	/**
	 * 게시글 등록
	 * @param board
	 * @return 등록된 게시글 Entity
	 */
	@PostMapping("/save")
	@ResponseBody 
	public Board save(@RequestBody Board board) {
		
		// 등록일자를 오늘로 설정
		board.setRegDate(new Date());
		
		// 게시글 저장
		Board saveBoard = boardRepository.save(board);
		
		// 생성된 bNo로 groupNo 설정
		saveBoard.setGroupNo(saveBoard.getNo());
		
		return boardRepository.save(saveBoard);
	}
	
	/**
	 * 게시글 상세 페이지
	 * @param bNo
	 * @param model
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/detail/{bNo}")
	public String detail(@PathVariable int bNo, Model model) {
		
		Board board = boardRepository.findOne(bNo);
		
		model.addAttribute("board", board);
		model.addAttribute("formattedRegDate", board.getFormattedRegDate());
		model.addAttribute("formattedModifyDate", board.getFormattedModifyDate());
		
		return "/board/detail";
	}
	
	/**
	 * 게시글 상세 불러오기
	 * @param bNo
	 * @return 게시글 상세 Entity
	 */
	@PostMapping("/getBoard/{bNo}")
	@ResponseBody
	public Board getBoard(@PathVariable int bNo) {
		return boardRepository.findOne(bNo);
	}
	
	/**
	 * 게시글 수정 페이지  
	 * @param bNo
	 * @param model
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/modify/{bNo}")
	public String modify(@PathVariable int bNo, Model model) {
		
		model.addAttribute("board", boardRepository.findOne(bNo));
		
		return "/board/modify";
	}
	
	/**
	 * 게시글 수정
	 * @param bNo
	 * @param board
	 * @return 수정된 게시글 Entity
	 */
	@PutMapping("/update/{bNo}")
	@ResponseBody
	public Board update(@PathVariable int bNo, @RequestBody Board board) {
		
		Board updateBoard = boardRepository.findOne(bNo);
		
		// 수정 시 제목, 내용 이외 기존 사항 반영
		updateBoard.setNo(bNo);
		updateBoard.setTitle(board.getTitle());
		updateBoard.setContent(board.getContent());
		
		// 수정일자를 오늘로 지정
		updateBoard.setModifyDate(new Date());
		
		return boardRepository.save(updateBoard);
	}
	
	/**
	 * 게시글 삭제
	 * @param bNo
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@PutMapping("/delete/{bNo}")
	@ResponseBody
	public String delete(@PathVariable int bNo) {
		
		// 기존 데이터는 유지하되, delFlag = 'Y' 업데이트
		Board board = boardRepository.findOne(bNo);
		
		board.setDelFlag("Y");
		boardRepository.save(board);
		
		return "/board/list";
	}	
	
	/**
	 * 답글 등록 페이지
	 * @param bNo
	 * @param model
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/write/{bNo}")
	public String writeReply(@PathVariable int bNo, Model model) {
		
		// 답글의 답글인 경우
		// groupNo가 있는지 확인
		int groupNo = boardRepository.findGroupNoBybNo(bNo);
		
		model.addAttribute("groupNo", groupNo);
		model.addAttribute("parentNo", bNo);
		
		return "/board/reply";
	}
	
	/**
	 * 답글 등록
	 * @param board
	 * @return 등록된 답글 Entity
	 */
	@PostMapping("/saveReply")
	@ResponseBody 
	public Board saveReply(@RequestBody Board board) {
		
		// 원글 번호로 groupSeq, parentNo, depth 지정
		int groupNo = board.getGroupNo();
		int parentNo = board.getParentNo();
		
		// 원글의 답글인 경우
		if(parentNo == 0) {
			parentNo = groupNo;
		}
		
		// 필요 파라미터
		// groupSeq : 원글 포함 전체 순서 지정
		// parentNo : 부모 글
		// depth : 원글로부터 몇번째 계층인지
		
		// 부모 글의 depth
		int preDepth = boardRepository.findDepthByParentNo(parentNo);
		
		// 현재 글 그룹 내의 마지막 groupSeq
		double maxGroupSeq = boardRepository.findMinGroupSeqByParentNoAndGroupNo(parentNo, groupNo);
		
		// 이전 글의 groupSeq
		double preGroupSeq = boardRepository.findGroupSeqByGroupNoAndGroupSeq(groupNo, maxGroupSeq);
		
		// 현재 답글 이후 글이 있는 경우
		if(maxGroupSeq > 0) {
		
			// groupSeqNew = 이전 글의 groupSeq + 이후 글의 groupSeq / 2
			// groupSeqNew 가 소수점 아래 15자리 이상인 경우 이후 groupSeq + 1 전체 업데이트
			double groupSeqNew = (preGroupSeq + maxGroupSeq) / 2;
			String groupSeqNewStr = groupSeqNew + "";
			
			System.out.println("parentNo : " + parentNo);
			System.out.println("preGroupSeq / maxGroupSeq : " + preGroupSeq + " / " + maxGroupSeq);
			System.out.println("double : " + groupSeqNew);
			System.out.println("String : " + groupSeqNewStr);
			
			// 소수점 자리수 확인
			int lenCheck = groupSeqNewStr.length() - groupSeqNewStr.indexOf(".") - 1;
			System.out.println("소수점 자리수 : " + lenCheck);
			if(lenCheck <= 15) {
				
				board.setGroupSeq(groupSeqNew);
				
			} else {
				
				// 기존 groupSeq 를 뒤로 밀기
				boardRepository.updateGroupSeq(groupNo, maxGroupSeq);
							
				board.setGroupSeq(maxGroupSeq);
				
			}
			
		} else {
			
			maxGroupSeq = boardRepository.findMaxGroupSeqByGroupNo(groupNo);			
			board.setGroupSeq(maxGroupSeq + 1);
		
		}
		
		
		board.setDepth(preDepth + 1);
		
		// 등록일자를 오늘로 설정
		board.setRegDate(new Date());
		
		return boardRepository.save(board);
	}
	
	
}
