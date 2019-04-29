package com.estsoft.web;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.estsoft.api.domain.Board;
import com.estsoft.api.repository.BoardRepository;
import com.estsoft.api.repository.MemberRepository;
import com.estsoft.api.specification.BoardSpecification;
import com.estsoft.security.SecurityConfig;
import com.estsoft.util.ApiUtils;

@Controller
@RequestMapping("/board")
@Transactional
public class BoardController {

	// Log
	private Logger log = LoggerFactory.getLogger(BoardController.class);
		
	@Autowired
	private BoardRepository boardRepository;
	
	@Autowired
	private MemberRepository memberRepository;
	
	
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
	@GetMapping("/list/{pageNum}")
	@ResponseBody 
	public Page<Board> list(@PageableDefault(size = 100) Pageable pageable, @PathVariable int pageNum, HttpServletRequest request) {
		
		// 검색조건
		String reqPageSize = request.getParameter("reqPageSize");
		String orderType = request.getParameter("orderType");
		String orderField = request.getParameter("orderField");
		
		// 기본 정렬
		LinkedList<Order> order = new LinkedList<Order>();
		order.addLast(new Order(Direction.DESC, "groupNo"));
		order.addLast(new Order(Direction.ASC, "groupSeq"));
		order.addLast(new Order(Direction.ASC, "depth"));
		
		// 정렬 추가
		if(ApiUtils.isNotNullString(orderType) && ApiUtils.isNotNullString(orderField)) {
			order.addFirst(new Order(Direction.valueOf(orderType), orderField));
		}
		
		// 정렬
		Sort sort = new Sort(order);
		
		// 페이지 크기 변경
		int pageSize = pageable.getPageSize();
		if(ApiUtils.isNotNullString(reqPageSize)) {
			pageSize = Integer.parseInt(reqPageSize);
		}
		
		// 페이징 설정
		PageRequest pageRequest = new PageRequest(pageNum - 1, pageSize, sort);
	
		// 글목록 가져오기
		return boardRepository.findAll(BoardSpecification.list(request.getParameterMap()), pageRequest);
		
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
	public Board save(@RequestBody Board board, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {
			
			// 현재 로그인한 사용자
			board.setWriter(writer);
			
			// 등록일자를 오늘로 설정
			board.setRegDate(new Date());
			
			// 게시글 저장
			Board saveBoard = boardRepository.save(board);
			
			// 생성된 bNo로 groupNo 설정
			saveBoard.setGroupNo(saveBoard.getNo());
			
			return boardRepository.save(saveBoard);
		
		} else {
			return new Board();	
		}

	}
	
	/**
	 * 게시글 상세 페이지
	 * @param bNo
	 * @param model
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/detail/{bNo}")
	public String detail(@PathVariable int bNo, Model model, Principal principal) {
		
		// 기존 게시글
		Board board = boardRepository.findOne(bNo);
		
		// 존재하지 않는 게시글 (삭제 게시글 포함) 접근하는 경우
		if(board == null) {
			
			return "/board/404";
			
		}
		
		// 작성자 이름으로 변경
		String writerName = memberRepository.findNameByEmail(board.getWriter());
		if(writerName == null) { 
			writerName = board.getWriter();
		}
		
		// 조회수 증가
		board.setHit(board.getHit() + 1);
		Board newBoard = boardRepository.save(board);
		
		model.addAttribute("board", newBoard);
		model.addAttribute("formattedRegDate", newBoard.getFormattedRegDate());
		model.addAttribute("formattedModifyDate", newBoard.getFormattedModifyDate());
		model.addAttribute("writerName", writerName);
		
		// 작성자 확인
		String userInfo = principal.getName();		
		if(ApiUtils.isNotNullString(userInfo) && board.getWriter().equals(userInfo)) {

			model.addAttribute("userCheck", "Y");
			
		}
		
		return "/board/detail";
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
	public Board update(@PathVariable int bNo, @RequestBody Board board, Principal principal) {
		
		String userInfo = principal.getName();
		Board updateBoard = boardRepository.findOne(bNo);

		// 작성자만 수정 가능
		if(ApiUtils.isNotNullString(userInfo) && updateBoard.getWriter().equals(userInfo)) {
			
			// 수정 시 제목, 내용 이외 기존 사항 반영
			updateBoard.setNo(bNo);
			updateBoard.setTitle(board.getTitle());
			updateBoard.setContent(board.getContent());
			
			// 수정일자를 오늘로 지정
			updateBoard.setModifyDate(new Date());
			
			return boardRepository.save(updateBoard);
			
		} else {
			
			return new Board();
			
		}
		

	}
	
	/**
	 * 게시글 삭제
	 * @param bNo
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@DeleteMapping("/delete/{bNo}")
	@ResponseBody
	public String delete(@PathVariable int bNo, Principal principal) {
		
		String userInfo = principal.getName();
		
		// 기존 데이터는 유지하되, delFlag = 'Y' 업데이트
		Board board = boardRepository.findOne(bNo);
		
		// 작성자만 삭제 가능
		if(ApiUtils.isNotNullString(userInfo) && board.getWriter().equals(userInfo)) {

			board.setDelFlag("Y");
			Board delBoard = boardRepository.save(board);
			
			// 원글 댓글개수(ReplyCount) 업데이트
			boardRepository.updateReplyCount(delBoard.getGroupNo());

			return "/board/list";
			
		} else {
			
			return "";
			
		}
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
	public Board saveReply(@RequestBody Board board, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {

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
				
				log.info("parentNo : " + parentNo);
				log.info("preGroupSeq / maxGroupSeq : " + preGroupSeq + " / " + maxGroupSeq);
				log.info("double : " + groupSeqNew);
				
				// 소수점 자리수 확인
				int lenCheck = ApiUtils.getDecimalLength(groupSeqNew);
				log.info("doubleLenCheck : " + lenCheck);
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
			
			board.setWriter(writer);
			board.setDepth(preDepth + 1);
			
			// 등록일자를 오늘로 설정
			board.setRegDate(new Date());
			
			// 답글 저장
			Board saveBoard = boardRepository.save(board);
			
			// 원글 댓글개수(ReplyCount) 업데이트
			boardRepository.updateReplyCount(saveBoard.getGroupNo());
			
			return saveBoard;
						
			
		} else {
			
			return new Board();
			
		}
		
	}
	
	
}
