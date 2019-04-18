package com.estsoft.domain.api;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "REPLY")
public class Reply {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NO")
    private int no;

    @Column(name = "BOARD_NO")
    private int boardNo;

    @Column(name = "CONTENT")
    private String content;
    
    @Column(name = "WRITER", updatable = false)
    private String writer;

    @Column(name = "REG_DATE")
    private Date regDate;

    @Column(name = "MODIFY_DATE")
    private Date modifyDate;

	@Column(name = "DEL_FLAG")
    private String delFlag;
	
	// ´ä±Û
	@Column(name = "GROUP_NO")
    private int groupNo;
	
	@Column(name = "GROUP_SEQ")
    private double groupSeq;
	
	@Column(name = "PARENT_NO", insertable = true, updatable = false)
    private int parentNo;
	
	@Column(name = "DEPTH", insertable = true, updatable = false)
    private int depth;

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public int getBoardNo() {
		return boardNo;
	}

	public void setBoardNo(int boardNo) {
		this.boardNo = boardNo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}

	public int getGroupNo() {
		return groupNo;
	}

	public void setGroupNo(int groupNo) {
		this.groupNo = groupNo;
	}

	public double getGroupSeq() {
		return groupSeq;
	}

	public void setGroupSeq(double groupSeq) {
		this.groupSeq = groupSeq;
	}

	public int getParentNo() {
		return parentNo;
	}

	public void setParentNo(int parentNo) {
		this.parentNo = parentNo;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public String toString() {
		return "Reply [no=" + no + ", boardNo=" + boardNo + ", content=" + content + ", writer=" + writer + ", regDate="
				+ regDate + ", modifyDate=" + modifyDate + ", delFlag=" + delFlag + ", groupNo=" + groupNo
				+ ", groupSeq=" + groupSeq + ", parentNo=" + parentNo + ", depth=" + depth + "]";
	}

	
	
	
	
}
