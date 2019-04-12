package com.estsoft.web;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.estsoft.domain.Reply;
import com.estsoft.repository.ReplyRepository;

@Controller
@RequestMapping("/reply")
@Transactional
public class ReplyController {

	@Autowired
	private ReplyRepository replyRepository;
	
	/**
	 * ��ü ��� �ҷ�����
	 * @param bNo
	 * @return ��ü ��� List
	 */
	@PostMapping("/getList/{bNo}/{pageNum}")
	@ResponseBody 
	public Page<Reply> getList(@PathVariable int bNo, @PathVariable int pageNum, @PageableDefault(size = 100) Pageable pageable) {
		PageRequest pageRequest = new PageRequest(pageNum - 1, pageable.getPageSize());	
		return replyRepository.findAllByBoardNoOrdering(bNo, pageRequest); 
	}
	
	/**
	 * ��� ���
	 * @param reply
	 * @return ��ϵ� ��� Entity
	 */
	@PostMapping("/save")
	@ResponseBody 
	public Reply save(@RequestBody Reply reply) {
		
		// ������ڸ� ���÷� ����
		reply.setRegDate(new Date());
		
		// �Խñ� ����
		Reply saveReply = replyRepository.save(reply);
		
		// ������ rNo�� groupNo ����
		saveReply.setGroupNo(saveReply.getNo());
		
		return replyRepository.save(saveReply);
	}
	
	/**
	 * ���� ���
	 * @param reply
	 * @return ��ϵ� ���� Entity
	 */
	@PostMapping("/saveReply")
	@ResponseBody 
	public Reply saveReply(@RequestBody Reply reply) {
		
		System.out.println("==>>>" + reply.toString());
		
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
			String groupSeqNewStr = groupSeqNew + "";
			
			System.out.println("parentNo : " + parentNo);
			System.out.println("preGroupSeq / maxGroupSeq : " + preGroupSeq + " / " + maxGroupSeq);
			System.out.println("double : " + groupSeqNew);
			System.out.println("String : " + groupSeqNewStr);
			
			// �Ҽ��� �ڸ��� Ȯ��
			int lenCheck = groupSeqNewStr.length() - groupSeqNewStr.indexOf(".") - 1;
			System.out.println("�Ҽ��� �ڸ��� : " + lenCheck);
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
		
		reply.setGroupNo(groupNo);
		reply.setDepth(preDepth + 1);
		
		// ������ڸ� ���÷� ����
		reply.setRegDate(new Date());
		
		return replyRepository.save(reply);
	}
	
	/**
	 * ��� ����
	 * @param rNo
	 * @return ������ ��� Entity
	 */
	@PostMapping("/getReply/{rNo}")
	@ResponseBody
	public Reply getReply(@PathVariable int rNo) {
		return replyRepository.findOne(rNo);
	}
	
	@PutMapping("/update/{rNo}")
	@ResponseBody
	public Reply update(@PathVariable int rNo, @RequestBody Reply reply) {
		
		Reply updateReply = replyRepository.findOne(rNo);
		
		// ���� �� ���� �̿� ���� ���� �ݿ�
		updateReply.setNo(rNo);
		updateReply.setContent(reply.getContent());
		
		// �������ڸ� ���÷� ����
		updateReply.setModifyDate(new Date());
		
		return replyRepository.save(updateReply);
	}
	
	/**
	 * ��� ����
	 * @param rNo
	 * @return �����̷�Ʈ �� �� ������
	 */
	@PutMapping("/delete/{rNo}")
	@ResponseBody
	public String delete(@PathVariable int rNo) {
		
		// ���� �����ʹ� �����ϵ�, delFlag = 'Y' ������Ʈ
		Reply reply = replyRepository.findOne(rNo);
		
		reply.setDelFlag("Y");
		replyRepository.save(reply);
		
		return "/board/detail/" + reply.getBoardNo();
	}	
	
	
	
}
