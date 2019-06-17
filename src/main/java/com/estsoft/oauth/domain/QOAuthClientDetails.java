package com.estsoft.oauth.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOAuthClientDetails is a Querydsl query type for OAuthClientDetails
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOAuthClientDetails extends EntityPathBase<OAuthClientDetails> {

    private static final long serialVersionUID = 1701498056L;

    public static final QOAuthClientDetails oAuthClientDetails = new QOAuthClientDetails("oAuthClientDetails");

    public final NumberPath<Integer> accessTokenValidity = createNumber("accessTokenValidity", Integer.class);

    public final StringPath additionalInformation = createString("additionalInformation");

    public final StringPath authorities = createString("authorities");

    public final StringPath authorizedGrantTypes = createString("authorizedGrantTypes");

    public final NumberPath<Integer> autoapprove = createNumber("autoapprove", Integer.class);

    public final StringPath clientId = createString("clientId");

    public final StringPath clientName = createString("clientName");

    public final StringPath clientSecret = createString("clientSecret");

    public final DateTimePath<java.util.Date> created = createDateTime("created", java.util.Date.class);

    public final BooleanPath enabled = createBoolean("enabled");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> refreshTokenValidity = createNumber("refreshTokenValidity", Integer.class);

    public final StringPath resourceIds = createString("resourceIds");

    public final StringPath scope = createString("scope");

    public final StringPath uuid = createString("uuid");

    public final StringPath webServerRedirectUri = createString("webServerRedirectUri");

    public QOAuthClientDetails(String variable) {
        super(OAuthClientDetails.class, forVariable(variable));
    }

    public QOAuthClientDetails(Path<? extends OAuthClientDetails> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOAuthClientDetails(PathMetadata metadata) {
        super(OAuthClientDetails.class, metadata);
    }

}

