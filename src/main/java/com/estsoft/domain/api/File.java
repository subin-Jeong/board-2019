package com.estsoft.domain.api;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "FILE")
public class File {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NO")
    private int no;

    @Column(name = "FILENAME")
    private String filename;

    @Column(name = "URL", length = 500)
    private String url;
    
    @Column(name = "BOARD_NO")
    private int boardNo;
    
    @Column(name = "REG_DATE")
    private Date regDate;
    
    @Column(name = "DEL_FLAG")
    private String delFlag;

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getBoardNo() {
		return boardNo;
	}

	public void setBoardNo(int boardNo) {
		this.boardNo = boardNo;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}

	@Override
	public String toString() {
		return "File [no=" + no + ", filename=" + filename + ", url=" + url + ", boardNo=" + boardNo + ", regDate="
				+ regDate + ", delFlag=" + delFlag + "]";
	}

    

    
}
