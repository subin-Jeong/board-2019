package com.estsoft.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.estsoft.api.domain.Board;

public interface BoardRepository extends JpaRepository<Board, Integer> {
	
	// 삭제되지 않은 데이터만 사용
	String delCheck = "b.delFlag='N'";
	
	// findAll
	Page<Board> findAll(Specification<Board> specification, Pageable pageable);	
	
	// findOne
	@Query("SELECT b FROM BOARD b WHERE " + delCheck +" AND b.no = :bNo")
	Board findOne(@Param("bNo") int bNo);
	
	// findGroupNoBybNo
	@Query("SELECT groupNo FROM BOARD b WHERE " + delCheck +" AND b.no = :bNo")
	int findGroupNoBybNo(@Param("bNo") int bNo);
	
	// findMaxGroupSeqByGroupNo
	@Query("SELECT COALESCE(MAX(groupSeq), 0) FROM BOARD b WHERE " + delCheck + " AND b.groupNo = :groupNo")
	double findMaxGroupSeqByGroupNo(@Param("groupNo") int groupNo);
	
	// findDepthByParentNo
	@Query("SELECT COALESCE(depth, 0) FROM BOARD b WHERE " + delCheck + " AND b.no = :parentNo")
	int findDepthByParentNo(@Param("parentNo") int parentNo);
	
	// findGroupSeqByGroupNoAndGroupSeq
	@Query("SELECT COALESCE(MAX(groupSeq), 0) FROM BOARD b WHERE " + delCheck + " AND b.groupNo = :groupNo AND b.groupSeq < :groupSeq")
	double findGroupSeqByGroupNoAndGroupSeq(@Param("groupNo") int groupNo, @Param("groupSeq") double groupSeq);
	
	// findMinGroupSeqByParentNoAndGroupNo
	@Query("SELECT	COALESCE(MIN(groupSeq), 0) "
		   + "FROM 	BOARD b "
		  + "WHERE	" + delCheck + " "
		  	+ "AND	b.groupNo = :groupNo "
		  	+ "AND	b.groupSeq > (SELECT groupSeq FROM BOARD WHERE " + delCheck + " AND no = :parentNo) "
		  	+ "AND	b.depth <= (SELECT depth FROM BOARD WHERE " + delCheck + " AND no = :parentNo) ")
	double findMinGroupSeqByParentNoAndGroupNo(@Param("parentNo") int parentNo, @Param("groupNo") int groupNo);
	
	// updateGroupSeq
	@Modifying
	@Transactional
	@Query("UPDATE BOARD b SET b.groupSeq = b.groupSeq + 1 WHERE " + delCheck + " AND b.groupNo = :groupNo AND b.groupSeq >= :groupSeq")
	int updateGroupSeq(@Param("groupNo") int groupNo, @Param("groupSeq") double groupSeq);

	// findAllOrdering
	//@Query("SELECT b FROM BOARD b WHERE " + delCheck + " ORDER BY b.groupNo DESC, b.groupSeq ASC, b.depth ASC")
	//Page<Board> findAllOrdering(Pageable page);
	
} 
