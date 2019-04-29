package com.estsoft.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.estsoft.api.domain.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
	
	// 삭제되지 않은 데이터만 사용
	String delCheck = "r.delFlag='N'";
		
	// findAllOrdering
	@Query("SELECT r FROM REPLY r WHERE " + delCheck + " AND r.boardNo = :boardNo ORDER BY r.groupNo DESC, r.groupSeq ASC, r.depth ASC")
	Page<Reply> findAllByBoardNoOrdering(@Param("boardNo") int boardNo, Pageable page);	
	
	// findGroupNoByrNo
	@Query("SELECT groupNo FROM REPLY r WHERE " + delCheck +" AND r.no = :rNo")
	int findGroupNoByrNo(@Param("rNo") int rNo);
	
	// findMaxGroupSeqByGroupNo
	@Query("SELECT COALESCE(MAX(groupSeq), 0) FROM REPLY r WHERE " + delCheck + " AND r.groupNo = :groupNo")
	double findMaxGroupSeqByGroupNo(@Param("groupNo") int groupNo);
	
	// findDepthByParentNo
	@Query("SELECT COALESCE(depth, 0) FROM REPLY r WHERE " + delCheck + " AND r.no = :parentNo")
	int findDepthByParentNo(@Param("parentNo") int parentNo);
	
	// findGroupSeqByGroupNoAndGroupSeq
	@Query("SELECT COALESCE(MAX(groupSeq), 0) FROM REPLY r WHERE " + delCheck + " AND r.groupNo = :groupNo AND r.groupSeq < :groupSeq")
	double findGroupSeqByGroupNoAndGroupSeq(@Param("groupNo") int groupNo, @Param("groupSeq") double groupSeq);
	
	// findAllOrdering
	@Query("SELECT r FROM REPLY r WHERE " + delCheck + " ORDER BY r.groupNo DESC, r.groupSeq ASC, r.depth ASC")
	List<Reply> findAllOrdering();
	
	// findMinGroupSeqByParentNoAndGroupNo
	@Query("SELECT	COALESCE(MIN(groupSeq), 0) "
		   + "FROM 	REPLY r "
		  + "WHERE	" + delCheck + " "
		  	+ "AND	r.groupNo = :groupNo "
		  	+ "AND	r.groupSeq > (SELECT groupSeq FROM REPLY WHERE " + delCheck + " AND no = :parentNo) "
		  	+ "AND	r.depth <= (SELECT depth FROM REPLY WHERE " + delCheck + " AND no = :parentNo) ")
	double findMinGroupSeqByParentNoAndGroupNo(@Param("parentNo") int parentNo, @Param("groupNo") int groupNo);
	
	// updateGroupSeq
	@Modifying
	@Transactional
	@Query("UPDATE REPLY r SET r.groupSeq = r.groupSeq + 1 WHERE " + delCheck + " AND r.groupNo = :groupNo AND r.groupSeq >= :groupSeq")
	int updateGroupSeq(@Param("groupNo") int groupNo, @Param("groupSeq") double groupSeq);
	 
} 
