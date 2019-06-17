package com.estsoft.api.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QReply is a Querydsl query type for Reply
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QReply extends EntityPathBase<Reply> {

    private static final long serialVersionUID = -2011671617L;

    public static final QReply reply = new QReply("reply");

    public final NumberPath<Integer> boardNo = createNumber("boardNo", Integer.class);

    public final StringPath content = createString("content");

    public final StringPath delFlag = createString("delFlag");

    public final NumberPath<Integer> depth = createNumber("depth", Integer.class);

    public final NumberPath<Integer> groupNo = createNumber("groupNo", Integer.class);

    public final NumberPath<Double> groupSeq = createNumber("groupSeq", Double.class);

    public final DateTimePath<java.util.Date> modifyDate = createDateTime("modifyDate", java.util.Date.class);

    public final NumberPath<Integer> no = createNumber("no", Integer.class);

    public final NumberPath<Integer> parentNo = createNumber("parentNo", Integer.class);

    public final DateTimePath<java.util.Date> regDate = createDateTime("regDate", java.util.Date.class);

    public final StringPath writer = createString("writer");

    public QReply(String variable) {
        super(Reply.class, forVariable(variable));
    }

    public QReply(Path<? extends Reply> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReply(PathMetadata metadata) {
        super(Reply.class, metadata);
    }

}

