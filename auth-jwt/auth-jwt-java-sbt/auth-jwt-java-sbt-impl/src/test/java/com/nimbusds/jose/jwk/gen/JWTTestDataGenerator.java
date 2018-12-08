package com.nimbusds.jose.jwk.gen;

import com.google.common.collect.ImmutableList;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.UUID;

import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.EncryptionMethod.A256GCM;
import static com.nimbusds.jose.JWEAlgorithm.DIR;
import static com.nimbusds.jose.JWEAlgorithm.ECDH_ES_A256KW;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static com.nimbusds.jose.JWSAlgorithm.HS256;
import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static com.nimbusds.jose.jwk.Curve.P_256;

import static java.lang.String.format;

/**
 * Generate JWKs and JWTs.
 */
public class JWTTestDataGenerator {

    private static ECKey generateEC() throws JOSEException {
        return new ECKeyGenerator(P_256).generate();
    }

    private static OctetSequenceKey generateSecret() throws JOSEException {
        return new OctetSequenceKeyGenerator(256).algorithm(HS256).generate();
    }

    private static RSAKey generateRSA() throws JOSEException {
        return new RSAKeyGenerator(2048).generate();
    }

    private static void printJwkConfiguration(OctetSequenceKey octetSequenceJwk, RSAKey rsaJwk, ECKey ecJwk) {
        System.out.println(format(
            "pac4j.lagom.jwt.authenticator {\n" +
            "  signatures = [\n" +
            "    {\n" +
            "      algorithm = \"HS256\"\n" +
            "      jwk = %s\n" +
            "    },\n" +
            "    {\n" +
            "      algorithm = \"RS256\"\n" +
            "      jwk =  %s\n" +
            "    },\n" +
            "    {\n" +
            "      algorithm = \"ES256\"\n" +
            "      jwk =  %s\n" +
            "    }\n" +
            "  ]\n" +
            "  encryptions = [\n" +
            "    {\n" +
            "      method = \"A256GCM\"\n" +
            "      algorithm = \"dir\"\n" +
            "      jwk =  %s\n" +
            "    },\n" +
            "    {\n" +
            "      method = \"A128GCM\"\n" +
            "      algorithm = \"RSA-OAEP-256\"\n" +
            "      jwk =  %s\n" +
            "    },\n" +
            "    {\n" +
            "      method = \"A256GCM\"\n" +
            "      algorithm = \"ECDH-ES+A256KW\"\n" +
            "      jwk =  %s\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            octetSequenceJwk.toJSONString(),
            // For signed JWT need only public key
            rsaJwk.toPublicJWK().toJSONString(),
            // For signed JWT need only public key
            ecJwk.toPublicJWK().toJSONString(),
            octetSequenceJwk.toJSONString(),
            rsaJwk.toJSONString(),
            ecJwk.toJSONString()
        ));
    }

    private static void printJwt(OctetSequenceKey octetSequenceJwk, RSAKey rsaJwk, ECKey ecJwk) throws JOSEException {
        final JWTClaimsSet Alice = new JWTClaimsSet.Builder()
                .issuer("https://pac4j.org")
                .subject("Alice")
                .claim("roles", ImmutableList.of("manager"))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();

        final JWTClaimsSet Bob = new JWTClaimsSet.Builder()
                .issuer("https://pac4j.org")
                .subject("Bob")
                .claim("roles", ImmutableList.of("developer"))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();

        // Alice SECRET
        JWSHeader jwsHeader = new JWSHeader(HS256);
        SignedJWT signedJWT = new SignedJWT(jwsHeader, Alice);
        signedJWT.sign(new MACSigner(octetSequenceJwk));
        System.out.println(format("private static final String ALICE_SECRET_JWT = \"%s\";", signedJWT.serialize()));
        JWEHeader jweHeader = new JWEHeader(DIR, A256GCM);
        EncryptedJWT jwe = new EncryptedJWT(jweHeader, Alice);
        jwe.encrypt(new DirectEncrypter(octetSequenceJwk));
        System.out.println(format("private static final String ALICE_SECRET_JWE = \"%s\";", jwe.serialize()));


        // Alice RSA
        jwsHeader = new JWSHeader(RS256);
        signedJWT = new SignedJWT(jwsHeader, Alice);
        signedJWT.sign(new RSASSASigner(rsaJwk));
        System.out.println(format("private static final String ALICE_RSA_JWT = \"%s\";", signedJWT.serialize()));
        jweHeader = new JWEHeader(RSA_OAEP_256, A128GCM);
        jwe = new EncryptedJWT(jweHeader, Alice);
        jwe.encrypt(new RSAEncrypter(rsaJwk));
        System.out.println(format("private static final String ALICE_RSA_JWE = \"%s\";", jwe.serialize()));


        // Alice EC
        jwsHeader = new JWSHeader(ES256);
        signedJWT = new SignedJWT(jwsHeader, Alice);
        signedJWT.sign(new ECDSASigner(ecJwk));
        System.out.println(format("private static final String ALICE_EC_JWT = \"%s\";", signedJWT.serialize()));
        jweHeader = new JWEHeader(ECDH_ES_A256KW, A256GCM);
        jwe = new EncryptedJWT(jweHeader, Alice);
        jwe.encrypt(new ECDHEncrypter(ecJwk));
        System.out.println(format("private static final String ALICE_EC_JWE = \"%s\";", jwe.serialize()));

        // Bob RSA
        jwsHeader = new JWSHeader(RS256);
        signedJWT = new SignedJWT(jwsHeader, Bob);
        signedJWT.sign(new RSASSASigner(rsaJwk));
        System.out.println(format("private static final String BOB_RSA_JWT = \"%s\";", signedJWT.serialize()));
    }

    public static void main(String[] args) throws JOSEException {
        OctetSequenceKey octetSequenceKey = generateSecret();
        RSAKey rsaKey = generateRSA();
        ECKey ecKey = generateEC();

        printJwkConfiguration(octetSequenceKey, rsaKey, ecKey);
        printJwt(octetSequenceKey, rsaKey, ecKey);
    }

}
