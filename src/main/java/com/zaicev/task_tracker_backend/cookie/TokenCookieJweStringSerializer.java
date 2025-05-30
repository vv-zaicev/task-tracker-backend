package com.zaicev.task_tracker_backend.cookie;

import java.util.Date;
import java.util.function.Function;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.zaicev.task_tracker_backend.models.Token;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenCookieJweStringSerializer implements Function<Token, String> {

	private final JWEEncrypter jweEncrypter;

	@Setter
	private JWEAlgorithm jweAlgorithm = JWEAlgorithm.DIR;

	@Setter
	private EncryptionMethod encryptionMethod = EncryptionMethod.A128GCM;

	public TokenCookieJweStringSerializer(JWEEncrypter jweEncrypter) {
		this.jweEncrypter = jweEncrypter;
	}

	public TokenCookieJweStringSerializer(JWEEncrypter jweEncrypter, JWEAlgorithm jweAlgorithm, EncryptionMethod encryptionMethod) {
		super();
		this.jweEncrypter = jweEncrypter;
		this.jweAlgorithm = jweAlgorithm;
		this.encryptionMethod = encryptionMethod;
	}

	@Override
	public String apply(Token token) {
		var jweHeader = new JWEHeader.Builder(this.jweAlgorithm, this.encryptionMethod)
				.keyID(token.id().toString())
				.build();
		var claimSet = new JWTClaimsSet.Builder()
				.jwtID(token.id().toString())
				.subject(token.subject())
				.issueTime(Date.from(token.createdAt()))
				.expirationTime(Date.from(token.expiresAt()))
				.claim("authorities", token.authorites())
				.build();
		var encryptedJWT = new EncryptedJWT(jweHeader, claimSet);
		
		try {
			encryptedJWT.encrypt(this.jweEncrypter);
			
			return encryptedJWT.serialize();
		} catch (JOSEException e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}

}
