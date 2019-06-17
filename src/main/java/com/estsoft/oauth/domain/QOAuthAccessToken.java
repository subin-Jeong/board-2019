package com.estsoft.oauth.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOAuthAccessToken is a Querydsl query type for OAuthAccessToken
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOAuthAccessToken extends EntityPathBase<OAuthAccessToken> {

    private static final long serialVersionUID = -1650797562L;

    public static final QOAuthAccessToken oAuthAccessToken = new QOAuthAccessToken("oAuthAccessToken");

    public final ArrayPath<byte[], Byte> authentication = createArray("authentication", byte[].class);

    public final StringPath authenticationId = createString("authenticationId");

    public final StringPath clientId = createString("clientId");

    public final StringPath refreshToken = createString("refreshToken");

    public final ArrayPath<byte[], Byte> token = createArray("token", byte[].class);

    public final StringPath tokenId = createString("tokenId");

    public final StringPath userName = createString("userName");

    public QOAuthAccessToken(String variable) {
        super(OAuthAccessToken.class, forVariable(variable));
    }

    public QOAuthAccessToken(Path<? extends OAuthAccessToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOAuthAccessToken(PathMetadata metadata) {
        super(OAuthAccessToken.class, metadata);
    }

}

