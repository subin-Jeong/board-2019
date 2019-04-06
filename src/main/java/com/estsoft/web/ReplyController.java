package com.estsoft.web;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.estsoft.domain.Reply;
import com.estsoft.repository.ReplyRepository;

@Controller
@RequestMapping("/reply")
@Transactional
public class ReplyController {

	@Autowired
	private ReplyRepository replyRepository;
	
	//───────────────────────────────────────
	// 리스트 조회
	//───────────────────────────────────────
	
	@PostMapping("/getList/{bNo}")
	@ResponseBody 
	public List<Reply> getList(@PathVariable int bNo) {
	
		return replyRepository.findAllByBoardNoOrdering(bNo); 
	}
	
	//───────────────────────────────────────
	// 등록
	//───────────────────────────────────────
	
	@PostMapping("/save")
	@ResponseBody 
	public Reply save(@RequestBody Reply reply) {
		
		// 등록일자를 오늘로 설정
		reply.setRegDate(new Date());
		
		// 게시글 저장
		Reply saveReply = replyRepository.save(reply);
		
		// 생성된 rNo로 groupNo 설정
		saveReply.setGroupNo(saveReply.getNo());
		
		return replyRepository.save(saveReply);
	}
	
	//───────────────────────────────────────
	// 답글 등록
	//───────────────────────────────────────
	@PostMapping("/saveReply")
	@ResponseBody 
	public Reply saveReply(@RequestBody Reply reply) {
		
		System.out.println("==>>>" + reply.toString());
		
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
			String groupSeqNewStr = groupSeqNew + "";
			
			System.out.println("parentNo : " + parentNo);
			System.out.println("preGroupSeq / maxGroupSeq : " + preGroupSeq + " / " + maxGroupSeq);
			System.out.println("double : " + groupSeqNew);
			System.out.println("String : " + groupSeqNewStr);
			
			// 소수점 자리수 확인
			int lenCheck = groupSeqNewStr.length() - groupSeqNewStr.indexOf(".") - 1;
			System.out.println("소수점 자리수 : " + lenCheck);
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
		
		reply.setGroupNo(groupNo);
		reply.setDepth(preDepth + 1);
		
		// 등록일자를 오늘로 설정
		reply.setRegDate(new Date());
		
		return replyRepository.save(reply);
	}
	
	//───────────────────────────────────────
	// 수정
	//───────────────────────────────────────
	@PostMapping("/getReply/{rNo}")
	@ResponseBody
	public Reply getReply(@PathVariable int rNo) {
		return replyRepository.findOne(rNo);
	}
	
	@PutMapping("/update/{rNo}")
	@ResponseBody
	public Reply update(@PathVariable int rNo, @RequestBody Reply reply) {
		
		Reply updateReply = replyRepository.findOne(rNo);
		
		// 수정 시 내용 이외 기존 사항 반영
		updateReply.setContent(reply.getContent());
		
		// 수정일자를 오늘로 지정
		updateReply.setModifyDate(new Date());
		
		return replyRepository.save(reply);
	}
	
	//───────────────────────────────────────
	// 삭제
	//───────────────────────────────────────
	@PutMapping("/delete/{rNo}")
	@ResponseBody
	public String delete(@PathVariable int rNo) {
		
		// 기존 데이터는 유지하되, delFlag = 'Y' 업데이트
		Reply reply = replyRepository.findOne(rNo);
		
		reply.setDelFlag("Y");
		replyRepository.save(reply);
		
		return "/board/detail/" + reply.getBoardNo();
	}	
	
	
	
}
