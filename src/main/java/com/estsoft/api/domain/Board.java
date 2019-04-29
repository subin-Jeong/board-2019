package com.estsoft.api.domain;


import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "BOARD")
public class Board {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NO")
    private int no;

    @Column(name = "TITLE")
    private String title;

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
	
	@Column(name = "HIT", columnDefinition = "int default 0")
    private int hit;

	@Column(name = "REPLY_COUNT", columnDefinition = "int default 0")
	private int replyCount;
	
	// 답글
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
	
	public String getFormattedRegDate() {
		return getFormattedDate(this.regDate, "yyyy.MM.dd HH:mm:ss");
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}
	
	public String getFormattedModifyDate() {
		return getFormattedDate(this.modifyDate, "yyyy.MM.dd HH:mm:ss");
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

	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
	}
	
	public int getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
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
	
	// Date 포맷 변경
	private String getFormattedDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(format).format(date);
    }

	@Override
	public String toString() {
		return "Board [no=" + no + ", title=" + title + ", content=" + content + ", writer=" + writer + ", regDate="
				+ regDate + ", modifyDate=" + modifyDate + ", delFlag=" + delFlag + ", hit=" + hit + ", groupNo="
				+ groupNo + ", groupSeq=" + groupSeq + ", parentNo=" + parentNo + ", depth=" + depth + ", replyCount="
				+ replyCount + "]";
	}

	
}
