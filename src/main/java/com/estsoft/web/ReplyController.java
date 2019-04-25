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
	 * ��ü ��� �ҷ�����
	 * @param bNo
	 * @return ��ü ��� List
	 */
	@GetMapping("/list/{bNo}/{pageNum}")
	@ResponseBody 
	public Page<Reply> list(@PathVariable int bNo, @PathVariable int pageNum, @PageableDefault(size = 100) Pageable pageable, HttpServletRequest request) {
		
		// �˻� ���� �Ķ����
		String reqPageSize = request.getParameter("reqPageSize");
		
		// ������ ũ�� ����
		int pageSize = pageable.getPageSize();
		if(ApiUtils.isNotNullString(reqPageSize)) {
			pageSize = Integer.parseInt(reqPageSize);
		}
		
		// ����¡ ����
		PageRequest pageRequest = new PageRequest(pageNum - 1, pageSize);
		
		return replyRepository.findAllByBoardNoOrdering(bNo, pageRequest); 
	}
	
	/**
	 * ��� ���
	 * @param reply
	 * @return ��ϵ� ��� Entity
	 */
	@PostMapping("/save")
	@ResponseBody 
	public Reply save(@RequestBody Reply reply, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {
			
			// �ۼ��� ����
			reply.setWriter(writer);
			
			// ������ڸ� ���÷� ����
			reply.setRegDate(new Date());
			
			// �Խñ� ����
			Reply saveReply = replyRepository.save(reply);
			
			// ������ rNo�� groupNo ����
			saveReply.setGroupNo(saveReply.getNo());
			
			return replyRepository.save(saveReply);
				
		} else {
			
			return new Reply();
			
		}

	}
	
	/**
	 * ���� ���
	 * @param reply
	 * @return ��ϵ� ���� Entity
	 */
	@PostMapping("/saveReply")
	@ResponseBody 
	public Reply saveReply(@RequestBody Reply reply, Principal principal) {
		
		String writer = principal.getName();
		
		if(ApiUtils.isNotNullString(writer)) {
			
			int parentNo = reply.getParentNo();
			
			// ����� ����� ���
			// groupNo�� �ִ��� Ȯ��
			int groupNo = replyRepository.findGroupNoByrNo(parentNo);
			
			
			// ������ ����� ���
			if(parentNo == 0) {
				groupNo = parentNo;
			}
			
			// �ʿ� �Ķ����
			// groupSeq : ���� ���� ��ü ���� ����
			// parentNo : �θ� ��
			// depth : ���۷κ��� ���° ��������
			
			// �θ� ���� depth
			int preDepth = replyRepository.findDepthByParentNo(parentNo);
			
			// ���� �� �׷� ���� ������ groupSeq
			double maxGroupSeq = replyRepository.findMinGroupSeqByParentNoAndGroupNo(parentNo, groupNo);
			
			// ���� ���� groupSeq
			double preGroupSeq = replyRepository.findGroupSeqByGroupNoAndGroupSeq(groupNo, maxGroupSeq);
			
			// ���� ��� ���� ���� �ִ� ���
			if(maxGroupSeq > 0) {
			
				// groupSeqNew = ���� ���� groupSeq + ���� ���� groupSeq / 2
				// groupSeqNew �� �Ҽ��� �Ʒ� 15�ڸ� �̻��� ��� ���� groupSeq + 1 ��ü ������Ʈ
				double groupSeqNew = (preGroupSeq + maxGroupSeq) / 2;
				
				log.info("parentNo : " + parentNo);
				log.info("preGroupSeq / maxGroupSeq : " + preGroupSeq + " / " + maxGroupSeq);
				log.info("double : " + groupSeqNew);
				
				// �Ҽ��� �ڸ��� Ȯ��
				int lenCheck = ApiUtils.getDecimalLength(groupSeqNew);
				log.info("�Ҽ��� �ڸ��� : " + lenCheck);
				if(lenCheck <= 15) {
					
					reply.setGroupSeq(groupSeqNew);
					
				} else {
					
					// ���� groupSeq �� �ڷ� �б�
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
			
			// ������ڸ� ���÷� ����
			reply.setRegDate(new Date());
			
			return replyRepository.save(reply);
			
		} else {
			
			return new Reply();
		}
		
	}
	
	/**
	 * ��� ��
	 * @param rNo
	 * @return ������ ��� �� Entity
	 */
	@GetMapping("/detail/{rNo}")
	@ResponseBody
	public Reply reply(@PathVariable int rNo) {
		return replyRepository.findOne(rNo);
	}
	
	/**
	 * ��� ����
	 * @param rNo
	 * @param reply
	 * @param principal
	 * @return ������ ��� Entity
	 */
	@PutMapping("/update/{rNo}")
	@ResponseBody
	public Reply update(@PathVariable int rNo, @RequestBody Reply reply, Principal principal) {
		
		String userInfo = principal.getName();
		
		Reply updateReply = replyRepository.findOne(rNo);
		
		// �ۼ��ڸ� ���� ����
		if(ApiUtils.isNotNullString(userInfo) && updateReply.getWriter().equals(userInfo)) {

			// ���� �� ���� �̿� ���� ���� �ݿ�
			updateReply.setNo(rNo);
			updateReply.setContent(reply.getContent());
			
			// �������ڸ� ���÷� ����
			updateReply.setModifyDate(new Date());
			
			return replyRepository.save(updateReply);
			
		} else {
			
			return new Reply();
					
		}
		
	}
	
	/**
	 * ��� ����
	 * @param rNo
	 * @return �����̷�Ʈ �� �� ������
	 */
	@DeleteMapping("/delete/{rNo}")
	@ResponseBody
	public String delete(@PathVariable int rNo, Principal principal) {
		
		String userInfo = principal.getName();
		
		// ���� �����ʹ� �����ϵ�, delFlag = 'Y' ������Ʈ
		Reply reply = replyRepository.findOne(rNo);

		// �ۼ��ڸ� ���� ����
		if(ApiUtils.isNotNullString(userInfo) && reply.getWriter().equals(userInfo)) {

			reply.setDelFlag("Y");
			replyRepository.save(reply);

			return "/board/detail/" + reply.getBoardNo();
			
		} else {
			
			return "";
			
		}
		
	}	
	
	
	
}
