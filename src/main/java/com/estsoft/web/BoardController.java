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
	 * ��ü �Խñ� ���
	 * @return �����̷�Ʈ �� �� ������
	 */
	@GetMapping("/list")
	public String list() {
		return "/board/list";
	}
	
	/**
	 * ��ü �Խñ� ��� �ҷ�����
	 * @return ��ü �Խñ� ��� List
	 */
	@PostMapping("/getList/{pageNum}")
	@ResponseBody 
	public Page<Board> getList(@PageableDefault(size = 100) Pageable pageable, @PathVariable int pageNum) {
		PageRequest pageRequest = new PageRequest(pageNum - 1, pageable.getPageSize());	
		return boardRepository.findAllOrdering(pageRequest);
	}
	
	/**
	 * �Խñ� ��� ������
	 * @return �����̷�Ʈ �� �� ������
	 */
	@GetMapping("/write")
	public String write() {
		return "/board/write";
	}
	
	/**
	 * �Խñ� ���
	 * @param board
	 * @return ��ϵ� �Խñ� Entity
	 */
	@PostMapping("/save")
	@ResponseBody 
	public Board save(@RequestBody Board board) {
		
		// ������ڸ� ���÷� ����
		board.setRegDate(new Date());
		
		// �Խñ� ����
		Board saveBoard = boardRepository.save(board);
		
		// ������ bNo�� groupNo ����
		saveBoard.setGroupNo(saveBoard.getNo());
		
		return boardRepository.save(saveBoard);
	}
	
	/**
	 * �Խñ� �� ������
	 * @param bNo
	 * @param model
	 * @return �����̷�Ʈ �� �� ������
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
	 * �Խñ� �� �ҷ�����
	 * @param bNo
	 * @return �Խñ� �� Entity
	 */
	@PostMapping("/getBoard/{bNo}")
	@ResponseBody
	public Board getBoard(@PathVariable int bNo) {
		return boardRepository.findOne(bNo);
	}
	
	/**
	 * �Խñ� ���� ������  
	 * @param bNo
	 * @param model
	 * @return �����̷�Ʈ �� �� ������
	 */
	@GetMapping("/modify/{bNo}")
	public String modify(@PathVariable int bNo, Model model) {
		
		model.addAttribute("board", boardRepository.findOne(bNo));
		
		return "/board/modify";
	}
	
	/**
	 * �Խñ� ����
	 * @param bNo
	 * @param board
	 * @return ������ �Խñ� Entity
	 */
	@PutMapping("/update/{bNo}")
	@ResponseBody
	public Board update(@PathVariable int bNo, @RequestBody Board board) {
		
		Board updateBoard = boardRepository.findOne(bNo);
		
		// ���� �� ����, ���� �̿� ���� ���� �ݿ�
		updateBoard.setNo(bNo);
		updateBoard.setTitle(board.getTitle());
		updateBoard.setContent(board.getContent());
		
		// �������ڸ� ���÷� ����
		updateBoard.setModifyDate(new Date());
		
		return boardRepository.save(updateBoard);
	}
	
	/**
	 * �Խñ� ����
	 * @param bNo
	 * @return �����̷�Ʈ �� �� ������
	 */
	@PutMapping("/delete/{bNo}")
	@ResponseBody
	public String delete(@PathVariable int bNo) {
		
		// ���� �����ʹ� �����ϵ�, delFlag = 'Y' ������Ʈ
		Board board = boardRepository.findOne(bNo);
		
		board.setDelFlag("Y");
		boardRepository.save(board);
		
		return "/board/list";
	}	
	
	/**
	 * ��� ��� ������
	 * @param bNo
	 * @param model
	 * @return �����̷�Ʈ �� �� ������
	 */
	@GetMapping("/write/{bNo}")
	public String writeReply(@PathVariable int bNo, Model model) {
		
		// ����� ����� ���
		// groupNo�� �ִ��� Ȯ��
		int groupNo = boardRepository.findGroupNoBybNo(bNo);
		
		model.addAttribute("groupNo", groupNo);
		model.addAttribute("parentNo", bNo);
		
		return "/board/reply";
	}
	
	/**
	 * ��� ���
	 * @param board
	 * @return ��ϵ� ��� Entity
	 */
	@PostMapping("/saveReply")
	@ResponseBody 
	public Board saveReply(@RequestBody Board board) {
		
		// ���� ��ȣ�� groupSeq, parentNo, depth ����
		int groupNo = board.getGroupNo();
		int parentNo = board.getParentNo();
		
		// ������ ����� ���
		if(parentNo == 0) {
			parentNo = groupNo;
		}
		
		// �ʿ� �Ķ����
		// groupSeq : ���� ���� ��ü ���� ����
		// parentNo : �θ� ��
		// depth : ���۷κ��� ���° ��������
		
		// �θ� ���� depth
		int preDepth = boardRepository.findDepthByParentNo(parentNo);
		
		// ���� �� �׷� ���� ������ groupSeq
		double maxGroupSeq = boardRepository.findMinGroupSeqByParentNoAndGroupNo(parentNo, groupNo);
		
		// ���� ���� groupSeq
		double preGroupSeq = boardRepository.findGroupSeqByGroupNoAndGroupSeq(groupNo, maxGroupSeq);
		
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
				
				board.setGroupSeq(groupSeqNew);
				
			} else {
				
				// ���� groupSeq �� �ڷ� �б�
				boardRepository.updateGroupSeq(groupNo, maxGroupSeq);
							
				board.setGroupSeq(maxGroupSeq);
				
			}
			
		} else {
			
			maxGroupSeq = boardRepository.findMaxGroupSeqByGroupNo(groupNo);			
			board.setGroupSeq(maxGroupSeq + 1);
		
		}
		
		
		board.setDepth(preDepth + 1);
		
		// ������ڸ� ���÷� ����
		board.setRegDate(new Date());
		
		return boardRepository.save(board);
	}
	
	
}
