package com.estsoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.estsoft.domain.File;

public interface FileRepository extends JpaRepository<File, Integer> {

	// �������� ���� �����͸� ���
	String delCheck = "f.delFlag='N'";
	
	// findAllByBoardNoOrdering
	@Query("SELECT f FROM FILE f WHERE " + delCheck + " AND f.boardNo = :boardNo ORDER BY f.regDate DESC")
	List<File> findAllByBoardNoOrdering(@Param("boardNo") int boardNo);
	
	// countByBoardNo
	@Query("SELECT COUNT(no) FROM FILE f WHERE " + delCheck + " AND f.boardNo = :boardNo")
	int countByBoardNo(@Param("boardNo") int boardNo);

} 
