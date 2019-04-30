package com.estsoft.api.web;

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
import com.estsoft.api.security.SecurityConfig;
import com.estsoft.api.specification.BoardSpecification;
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
	 * ?���? 게시�? 목록
	 * @return 리다?��?��?�� ?�� �? ?��?���?
	 */
	@GetMapping("/list")
	public String list() {
		return "/board/list";
	}
	
	/**
	 * ?���? 게시�? 목록 불러?���?
	 * @return ?���? 게시�? 목록 List
	 */
	@GetMapping("/list/{pageNum}")
	@ResponseBody 
	public Page<Board> list(@PageableDefault(size = 100) Pageable pageable, @PathVariable int pageNum, HttpServletRequest request) {
		
		// �??��조건
		String reqPageSize = request.getParameter("reqPageSize");
		String orderType = request.getParameter("orderType");
		String orderField = request.getParameter("orderField");
		
		// 기본 ?��?��
		LinkedList<Order> order = new LinkedList<Order>();
		order.addLast(new Order(Direction.DESC, "groupNo"));
		order.addLast(new Order(Direction.ASC, "groupSeq"));
		order.addLast(new Order(Direction.ASC, "depth"));
		
		// ?��?�� 추�?
		if(ApiUtils.isNotNullString(orderType) && ApiUtils.isNotNullString(orderField)) {
			order.addFirst(new Order(Direction.valueOf(orderType), orderField));
		}
		
		// ?��?��
		Sort sort = new Sort(order);
		
		// ?��?���? ?���? �?�?
		int pageSize = pageable.getPageSize();
		if(ApiUtils.isNotNullString(reqPageSize)) {
			pageSize = Integer.parseInt(reqPageSize);
		}
		
		// ?��?���? ?��?��
		PageRequest pageRequest = new PageRequest(pageNum - 1, pageSize, sort);
	
		// �?목록 �??��?���?
		return boardRepository.findAll(BoardSpecification.list(request.getParameterMap()), pageRequest);
		
	}
	
	/**
	 * 게시�? ?���? ?��?���?
	 * @return 리다?��?��?�� ?�� �? ?��?���?
	 */
	@GetMapping("/write")
	public String write() {
		return "/board/write";
	}
	
	/**
	 * 게시�? ?���?
	 * @param board
	 * @return ?��록된 게시�? Entity
	 */
	@PostMapping("/save")
	@ResponseBody 
	public Board save(@RequestBody Board board, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {
			
			// ?��?�� 로그?��?�� ?��?��?��
			board.setWriter(writer);
			
			// ?��록일?���? ?��?���? ?��?��
			board.setRegDate(new Date());
			
			// 게시�? ???��
			Board saveBoard = boardRepository.save(board);
			
			// ?��?��?�� bNo�? groupNo ?��?��
			saveBoard.setGroupNo(saveBoard.getNo());
			
			return boardRepository.save(saveBoard);
		
		} else {
			return new Board();	
		}

	}
	
	/**
	 * 게시�? ?��?�� ?��?���?
	 * @param bNo
	 * @param model
	 * @return 리다?��?��?�� ?�� �? ?��?���?
	 */
	@GetMapping("/detail/{bNo}")
	public String detail(@PathVariable int bNo, Model model, Principal principal) {
		
		// 기존 게시�?
		Board board = boardRepository.findOne(bNo);
		
		// 존재?���? ?��?�� 게시�? (?��?�� 게시�? ?��?��) ?��근하?�� 경우
		if(board == null) {
			
			return "/board/404";
			
		}
		
		// ?��?��?�� ?��름으�? �?�?
		String writerName = memberRepository.findNameByEmail(board.getWriter());
		if(writerName == null) { 
			writerName = board.getWriter();
		}
		
		// 조회?�� 증�?
		board.setHit(board.getHit() + 1);
		Board newBoard = boardRepository.save(board);
		
		model.addAttribute("board", newBoard);
		model.addAttribute("formattedRegDate", newBoard.getFormattedRegDate());
		model.addAttribute("formattedModifyDate", newBoard.getFormattedModifyDate());
		model.addAttribute("writerName", writerName);
		
		// ?��?��?�� ?��?��
		String userInfo = principal.getName();		
		if(ApiUtils.isNotNullString(userInfo) && board.getWriter().equals(userInfo)) {

			model.addAttribute("userCheck", "Y");
			
		}
		
		return "/board/detail";
	}
	
	/**
	 * 게시�? ?��?�� ?��?���?  
	 * @param bNo
	 * @param model
	 * @return 리다?��?��?�� ?�� �? ?��?���?
	 */
	@GetMapping("/modify/{bNo}")
	public String modify(@PathVariable int bNo, Model model) {
		
		model.addAttribute("board", boardRepository.findOne(bNo));
		
		return "/board/modify";
	}
	
	/**
	 * 게시�? ?��?��
	 * @param bNo
	 * @param board
	 * @return ?��?��?�� 게시�? Entity
	 */
	@PutMapping("/update/{bNo}")
	@ResponseBody
	public Board update(@PathVariable int bNo, @RequestBody Board board, Principal principal) {
		
		String userInfo = principal.getName();
		Board updateBoard = boardRepository.findOne(bNo);

		// ?��?��?���? ?��?�� �??��
		if(ApiUtils.isNotNullString(userInfo) && updateBoard.getWriter().equals(userInfo)) {
			
			// ?��?�� ?�� ?���?, ?��?�� ?��?�� 기존 ?��?�� 반영
			updateBoard.setNo(bNo);
			updateBoard.setTitle(board.getTitle());
			updateBoard.setContent(board.getContent());
			
			// ?��?��?��?���? ?��?���? �??��
			updateBoard.setModifyDate(new Date());
			
			return boardRepository.save(updateBoard);
			
		} else {
			
			return new Board();
			
		}
		

	}
	
	/**
	 * 게시�? ?��?��
	 * @param bNo
	 * @return 리다?��?��?�� ?�� �? ?��?���?
	 */
	@DeleteMapping("/delete/{bNo}")
	@ResponseBody
	public String delete(@PathVariable int bNo, Principal principal) {
		
		String userInfo = principal.getName();
		
		// 기존 ?��?��?��?�� ?���??��?��, delFlag = 'Y' ?��?��?��?��
		Board board = boardRepository.findOne(bNo);
		
		// ?��?��?���? ?��?�� �??��
		if(ApiUtils.isNotNullString(userInfo) && board.getWriter().equals(userInfo)) {

			board.setDelFlag("Y");
			boardRepository.save(board);

			return "/board/list";
			
		} else {
			
			return "";
			
		}
	}	
	
	/**
	 * ?���? ?���? ?��?���?
	 * @param bNo
	 * @param model
	 * @return 리다?��?��?�� ?�� �? ?��?���?
	 */
	@GetMapping("/write/{bNo}")
	public String writeReply(@PathVariable int bNo, Model model) {
		
		// ?���??�� ?���??�� 경우
		// groupNo�? ?��?���? ?��?��
		int groupNo = boardRepository.findGroupNoBybNo(bNo);
		
		model.addAttribute("groupNo", groupNo);
		model.addAttribute("parentNo", bNo);
		
		return "/board/reply";
	}
	
	/**
	 * ?���? ?���?
	 * @param board
	 * @return ?��록된 ?���? Entity
	 */
	@PostMapping("/saveReply")
	@ResponseBody 
	public Board saveReply(@RequestBody Board board, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {

			// ?���? 번호�? groupSeq, parentNo, depth �??��
			int groupNo = board.getGroupNo();
			int parentNo = board.getParentNo();
			
			// ?���??�� ?���??�� 경우
			if(parentNo == 0) {
				parentNo = groupNo;
			}
			
			// ?��?�� ?��?��미터
			// groupSeq : ?���? ?��?�� ?���? ?��?�� �??��
			// parentNo : �?�? �?
			// depth : ?���?로�??�� 몇번�? 계층?���?
			
			// �?�? �??�� depth
			int preDepth = boardRepository.findDepthByParentNo(parentNo);
			
			// ?��?�� �? 그룹 ?��?�� 마�?�? groupSeq
			double maxGroupSeq = boardRepository.findMinGroupSeqByParentNoAndGroupNo(parentNo, groupNo);
			
			// ?��?�� �??�� groupSeq
			double preGroupSeq = boardRepository.findGroupSeqByGroupNoAndGroupSeq(groupNo, maxGroupSeq);
			
			// ?��?�� ?���? ?��?�� �??�� ?��?�� 경우
			if(maxGroupSeq > 0) {
			
				// groupSeqNew = ?��?�� �??�� groupSeq + ?��?�� �??�� groupSeq / 2
				// groupSeqNew �? ?��?��?�� ?��?�� 15?���? ?��?��?�� 경우 ?��?�� groupSeq + 1 ?���? ?��?��?��?��
				double groupSeqNew = (preGroupSeq + maxGroupSeq) / 2;
				
				log.info("parentNo : " + parentNo);
				log.info("preGroupSeq / maxGroupSeq : " + preGroupSeq + " / " + maxGroupSeq);
				log.info("double : " + groupSeqNew);
				
				// ?��?��?�� ?��리수 ?��?��
				int lenCheck = ApiUtils.getDecimalLength(groupSeqNew);
				log.info("doubleLenCheck : " + lenCheck);
				if(lenCheck <= 15) {
					
					board.setGroupSeq(groupSeqNew);
					
				} else {
					
					// 기존 groupSeq �? ?���? �?�?
					boardRepository.updateGroupSeq(groupNo, maxGroupSeq);
								
					board.setGroupSeq(maxGroupSeq);
					
				}
				
			} else {
				
				maxGroupSeq = boardRepository.findMaxGroupSeqByGroupNo(groupNo);			
				board.setGroupSeq(maxGroupSeq + 1);
			
			}
			
			board.setWriter(writer);
			board.setDepth(preDepth + 1);
			
			// ?��록일?���? ?��?���? ?��?��
			board.setRegDate(new Date());
			
			return boardRepository.save(board);
			
		} else {
			
			return new Board();
			
		}
		
	}
	
}
