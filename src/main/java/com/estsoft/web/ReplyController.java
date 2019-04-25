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
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.estsoft.auth.SecurityConfig;
import com.estsoft.domain.api.Reply;
import com.estsoft.repository.api.ReplyRepository;
import com.estsoft.util.ApiUtils;

@Controller
@RequestMapping("/reply")
@Transactional
public class ReplyController {

	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
	
	@Autowired
	private ReplyRepository replyRepository;
	
	/**
	 * 전체 댓글 불러오기
	 * @param bNo
	 * @return 전체 댓글 List
	 */
	@GetMapping("/list/{bNo}/{pageNum}")
	@ResponseBody 
	public Page<Reply> list(@PathVariable int bNo, @PathVariable int pageNum, @PageableDefault(size = 100) Pageable pageable, HttpServletRequest request) {
		
		// 검색 조건 파라미터
		String reqPageSize = request.getParameter("reqPageSize");
		
		// 페이지 크기 변경
		int pageSize = pageable.getPageSize();
		if(ApiUtils.isNotNullString(reqPageSize)) {
			pageSize = Integer.parseInt(reqPageSize);
		}
		
		// 페이징 설정
		PageRequest pageRequest = new PageRequest(pageNum - 1, pageSize);
		
		return replyRepository.findAllByBoardNoOrdering(bNo, pageRequest); 
	}
	
	/**
	 * 댓글 등록
	 * @param reply
	 * @return 등록된 댓글 Entity
	 */
	@PostMapping("/save")
	@ResponseBody 
	public Reply save(@RequestBody Reply reply, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {
			
			// 작성자 설정
			reply.setWriter(writer);
			
			// 등록일자를 오늘로 설정
			reply.setRegDate(new Date());
			
			// 게시글 저장
			Reply saveReply = replyRepository.save(reply);
			
			// 생성된 rNo로 groupNo 설정
			saveReply.setGroupNo(saveReply.getNo());
			
			return replyRepository.save(saveReply);
				
		} else {
			
			return new Reply();
			
		}

	}
	
	/**
	 * 대댓글 등록
	 * @param reply
	 * @return 등록된 대댓글 Entity
	 */
	@PostMapping("/saveReply")
	@ResponseBody 
	public Reply saveReply(@RequestBody Reply reply, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {
			
			int parentNo = reply.getParentNo();
			
			// 답글의 답글인 경우
			// groupNo가 있는지 확인
			int groupNo = replyRepository.findGroupNoByrNo(parentNo);
			
			
			// 원글의 답글인 경우
			if(parentNo == 0) {
				groupNo = parentNo;
			}
			
			// 필요 파라미터
			// groupSeq : 원글 포함 전체 순서 지정
			// parentNo : 부모 글
			// depth : 원글로부터 몇번째 계층인지
			
			// 부모 글의 depth
			int preDepth = replyRepository.findDepthByParentNo(parentNo);
			
			// 현재 글 그룹 내의 마지막 groupSeq
			double maxGroupSeq = replyRepository.findMinGroupSeqByParentNoAndGroupNo(parentNo, groupNo);
			
			// 이전 글의 groupSeq
			double preGroupSeq = replyRepository.findGroupSeqByGroupNoAndGroupSeq(groupNo, maxGroupSeq);
			
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
				log.info("소수점 자리수 : " + lenCheck);
				if(lenCheck <= 15) {
					
					reply.setGroupSeq(groupSeqNew);
					
				} else {
					
					// 기존 groupSeq 를 뒤로 밀기
					replyRepository.updateGroupSeq(groupNo, maxGroupSeq);
								
					reply.setGroupSeq(maxGroupSeq);
					
				}
				
			} else {
				
				maxGroupSeq = replyRepository.findMaxGroupSeqByGroupNo(groupNo);			
				reply.setGroupSeq(maxGroupSeq + 1);
			
			}
			
			reply.setWriter(writer);
			reply.setGroupNo(groupNo);
			reply.setDepth(preDepth + 1);
			
			// 등록일자를 오늘로 설정
			reply.setRegDate(new Date());
			
			return replyRepository.save(reply);
			
		} else {
			
			return new Reply();
		}
		
	}
	
	/**
	 * 댓글 상세
	 * @param rNo
	 * @return 선택한 댓글 상세 Entity
	 */
	@GetMapping("/detail/{rNo}")
	@ResponseBody
	public Reply reply(@PathVariable int rNo) {
		return replyRepository.findOne(rNo);
	}
	
	/**
	 * 댓글 수정
	 * @param rNo
	 * @param reply
	 * @param principal
	 * @return 수정된 댓글 Entity
	 */
	@PutMapping("/update/{rNo}")
	@ResponseBody
	public Reply update(@PathVariable int rNo, @RequestBody Reply reply, Principal principal) {
		
		String userInfo = principal.getName();
		
		Reply updateReply = replyRepository.findOne(rNo);
		
		// 작성자만 수정 가능
		if(ApiUtils.isNotNullString(userInfo) && updateReply.getWriter().equals(userInfo)) {

			// 수정 시 내용 이외 기존 사항 반영
			updateReply.setNo(rNo);
			updateReply.setContent(reply.getContent());
			
			// 수정일자를 오늘로 지정
			updateReply.setModifyDate(new Date());
			
			return replyRepository.save(updateReply);
			
		} else {
			
			return new Reply();
					
		}
		
	}
	
	/**
	 * 댓글 삭제
	 * @param rNo
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@DeleteMapping("/delete/{rNo}")
	@ResponseBody
	public String delete(@PathVariable int rNo, Principal principal) {
		
		String userInfo = principal.getName();
		
		// 기존 데이터는 유지하되, delFlag = 'Y' 업데이트
		Reply reply = replyRepository.findOne(rNo);

		// 작성자만 삭제 가능
		if(ApiUtils.isNotNullString(userInfo) && reply.getWriter().equals(userInfo)) {

			reply.setDelFlag("Y");
			replyRepository.save(reply);

			return "/board/detail/" + reply.getBoardNo();
			
		} else {
			
			return "";
			
		}
		
	}	
	
	
	
}
