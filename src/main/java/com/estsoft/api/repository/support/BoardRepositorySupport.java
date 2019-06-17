package com.estsoft.api.repository.support;

import static com.estsoft.api.domain.QBoard.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.estsoft.api.domain.Board;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class BoardRepositorySupport extends QueryDslRepositorySupport {

	private final JPAQueryFactory queryFactory;

    public BoardRepositorySupport(JPAQueryFactory queryFactory) {
        super(Board.class);
        this.queryFactory = queryFactory;
    }

    public Page<Board> findAll(Pageable pageable) {
        return queryFactory
        		.selectFrom(board).fetch();
    }
    
}
