package com.zaicev.task_tracker_backend.authentication.cookie;

import java.text.ParseException;
import java.util.UUID;
import java.util.function.Function;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.zaicev.task_tracker_backend.models.Token;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenCookieJweStringDeserializer implements Function<String, Token> {

	private final JWEDecrypter jweDecrypter;

	public TokenCookieJweStringDeserializer(JWEDecrypter jweDecrypter) {
		this.jweDecrypter = jweDecrypter;
	}

	@Override
	public Token apply(String string) {
		try {
			var encryptedJWT = EncryptedJWT.parse(string);
			encryptedJWT.decrypt(this.jweDecrypter);
			var claimSet = encryptedJWT.getJWTClaimsSet();
			return new Token(UUID.fromString(claimSet.getJWTID()), claimSet.getSubject(), claimSet.getStringListClaim("authorities"),
					claimSet.getIssueTime().toInstant(), claimSet.getExpirationTime().toInstant());
		} catch (ParseException | JOSEException exception) {
			log.error(exception.getMessage(), exception);
		}
		
		return null;
	}

}
