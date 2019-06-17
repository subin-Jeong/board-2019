package com.estsoft.oauth.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOAuthRefreshToken is a Querydsl query type for OAuthRefreshToken
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOAuthRefreshToken extends EntityPathBase<OAuthRefreshToken> {

    private static final long serialVersionUID = -1727993619L;

    public static final QOAuthRefreshToken oAuthRefreshToken = new QOAuthRefreshToken("oAuthRefreshToken");

    public final ArrayPath<byte[], Byte> authentication = createArray("authentication", byte[].class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final ArrayPath<byte[], Byte> token = createArray("token", byte[].class);

    public final StringPath tokenId = createString("tokenId");

    public QOAuthRefreshToken(String variable) {
        super(OAuthRefreshToken.class, forVariable(variable));
    }

    public QOAuthRefreshToken(Path<? extends OAuthRefreshToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOAuthRefreshToken(PathMetadata metadata) {
        super(OAuthRefreshToken.class, metadata);
    }

}

