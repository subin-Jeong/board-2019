package com.estsoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.estsoft.domain.File;
import com.estsoft.domain.Reply;

public interface FileRepository extends JpaRepository<File, Integer> {

	// 삭제되지 않은 데이터만 사용
	String delCheck = "f.delFlag='N'";
	
	// findAllByBoardNoOrdering
	@Query("SELECT f FROM FILE f WHERE " + delCheck + " AND f.boardNo = :boardNo ORDER BY f.regDate DESC")
	List<File> findAllByBoardNoOrdering(@Param("boardNo") int boardNo);

} 
