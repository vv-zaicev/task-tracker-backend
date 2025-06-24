package com.zaicev.task_tracker_backend.authentication.cookie;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.zaicev.task_tracker_backend.models.Token;

@ExtendWith(MockitoExtension.class)
public class TokenCookieJweStringDeserializerTest {
	@Mock
	private JWEDecrypter jweDecrypter;

	@InjectMocks
	private TokenCookieJweStringDeserializer deserializer;

	@Test
	void apply_validEncryptedJWTProvided_shouldReturnToken() throws Exception {
		String jweString = "validJweString";
		EncryptedJWT encryptedJWT = mock(EncryptedJWT.class);
		JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
				.jwtID(UUID.randomUUID().toString())
				.subject("user")
				.claim("authorities", List.of("ROLE_USER"))
				.issueTime(Date.from(Instant.now()))
				.expirationTime(Date.from(Instant.now().plusSeconds(3600)))
				.build();

		try (MockedStatic<EncryptedJWT> mocked = mockStatic(EncryptedJWT.class)) {
			mocked.when(() -> EncryptedJWT.parse(jweString)).thenReturn(encryptedJWT);

			doNothing().when(encryptedJWT).decrypt(jweDecrypter);
			when(encryptedJWT.getJWTClaimsSet()).thenReturn(claimSet);

			Token token = deserializer.apply(jweString);

			assertNotNull(token);
			assertEquals(claimSet.getJWTID(), token.id().toString());
			assertEquals(claimSet.getSubject(), token.subject());
			assertEquals(claimSet.getStringListClaim("authorities"), token.authorites());
			assertEquals(claimSet.getIssueTime().toInstant(), token.createdAt());
			assertEquals(claimSet.getExpirationTime().toInstant(), token.expiresAt());
		}
	}

	@Test
	void apply_parseExceptionOccurs_shouldReturnNull() throws Exception {
		String invalidString = "invalid";

		try (MockedStatic<EncryptedJWT> mocked = mockStatic(EncryptedJWT.class)) {
			mocked.when(() -> EncryptedJWT.parse(invalidString)).thenThrow(new ParseException("Invalid JWE", 0));

			Token token = deserializer.apply(invalidString);

			assertNull(token);
		}
	}

	@Test
	void apply_JOSEExceptionOccurs_shouldReturnNull() throws Exception {
		String jweString = "validButFailsDecryption";
		EncryptedJWT encryptedJWT = mock(EncryptedJWT.class);

		try (MockedStatic<EncryptedJWT> mocked = mockStatic(EncryptedJWT.class)) {
			mocked.when(() -> EncryptedJWT.parse(jweString)).thenReturn(encryptedJWT);

			doThrow(new JOSEException("Decryption error")).when(encryptedJWT).decrypt(jweDecrypter);

			Token token = deserializer.apply(jweString);

			assertNull(token);
		}
	}
}
