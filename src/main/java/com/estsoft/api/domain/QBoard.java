package com.estsoft.api.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = -2026164293L;

    public static final QBoard board = new QBoard("board");

    public final StringPath content = createString("content");

    public final StringPath delFlag = createString("delFlag");

    public final NumberPath<Integer> depth = createNumber("depth", Integer.class);

    public final NumberPath<Integer> groupNo = createNumber("groupNo", Integer.class);

    public final NumberPath<Double> groupSeq = createNumber("groupSeq", Double.class);

    public final NumberPath<Integer> hit = createNumber("hit", Integer.class);

    public final DateTimePath<java.util.Date> modifyDate = createDateTime("modifyDate", java.util.Date.class);

    public final NumberPath<Integer> no = createNumber("no", Integer.class);

    public final NumberPath<Integer> parentNo = createNumber("parentNo", Integer.class);

    public final DateTimePath<java.util.Date> regDate = createDateTime("regDate", java.util.Date.class);

    public final NumberPath<Integer> replyCount = createNumber("replyCount", Integer.class);

    public final StringPath title = createString("title");

    public final StringPath writer = createString("writer");

    public QBoard(String variable) {
        super(Board.class, forVariable(variable));
    }

    public QBoard(Path<? extends Board> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBoard(PathMetadata metadata) {
        super(Board.class, metadata);
    }

}

