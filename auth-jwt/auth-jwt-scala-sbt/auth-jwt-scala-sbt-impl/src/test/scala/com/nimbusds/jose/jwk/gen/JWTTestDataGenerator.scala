package com.nimbusds.jose.jwk.gen

import com.google.common.collect.ImmutableList
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.DirectEncrypter
import com.nimbusds.jose.crypto.ECDHEncrypter
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import java.util.UUID
import com.nimbusds.jose.EncryptionMethod.A128GCM
import com.nimbusds.jose.EncryptionMethod.A256GCM
import com.nimbusds.jose.JWEAlgorithm.DIR
import com.nimbusds.jose.JWEAlgorithm.ECDH_ES_A256KW
import com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256
import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.JWSAlgorithm.HS256
import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.jwk.Curve.P_256
import java.lang.String.format

/**
  * Generate JWKs and JWTs.
  */
object JWTTestDataGenerator {

  @throws[JOSEException]
  private def generateEC = new ECKeyGenerator(P_256).generate

  @throws[JOSEException]
  private def generateSecret = new OctetSequenceKeyGenerator(256).algorithm(HS256).generate

  @throws[JOSEException]
  private def generateRSA = new RSAKeyGenerator(2048).generate

  private def printJwkConfiguration(octetSequenceJwk: OctetSequenceKey, rsaJwk: RSAKey, ecJwk: ECKey): Unit = {
    println(
      s"""pac4j.lagom.jwt.authenticator {
         |  signatures = [
         |    {
         |      algorithm = "HS256"
         |      jwk = ${octetSequenceJwk.toJSONString}
         |    },
         |    {
         |      algorithm = "RS256"
         |      jwk = ${rsaJwk.toPublicJWK.toJSONString}
         |    },
         |    {
         |      algorithm = "ES256"
         |      jwk = ${ecJwk.toPublicJWK.toJSONString}
         |    }
         |  ]
         |  encryptions = [
         |    {
         |      method = "A256GCM"
         |      algorithm = "dir"
         |      jwk = ${octetSequenceJwk.toJSONString}
         |    },
         |    {
         |      method = "A128GCM"
         |      algorithm = "RSA-OAEP-256"
         |      jwk = ${rsaJwk.toJSONString}
         |    },
         |    {
         |      method = "A256GCM"
         |      algorithm = "ECDH-ES+A256KW"
         |      jwk = ${ecJwk.toJSONString}
         |    }
         |  ]
         |}
       """.stripMargin
    )
  }

  @throws[JOSEException]
  private def printJwt(octetSequenceJwk: OctetSequenceKey, rsaJwk: RSAKey, ecJwk: ECKey): Unit = {
    val Alice = new JWTClaimsSet.Builder()
      .issuer("https://pac4j.org")
      .subject("Alice")
      .claim("roles", ImmutableList.of("manager"))
      .issueTime(new Date)
      .jwtID(UUID.randomUUID.toString)
      .build

    val Bob = new JWTClaimsSet.Builder()
      .issuer("https://pac4j.org")
      .subject("Bob")
      .claim("roles", ImmutableList.of("developer"))
      .issueTime(new Date)
      .jwtID(UUID.randomUUID.toString)
      .build


    // Alice SECRET
    var jwsHeader = new JWSHeader(HS256)
    var signedJWT = new SignedJWT(jwsHeader, Alice)
    signedJWT.sign(new MACSigner(octetSequenceJwk))
    println(s"""private val ALICE_SECRET_JWT = "${signedJWT.serialize}"""")
    var jweHeader = new JWEHeader(DIR, A256GCM)
    var jwe = new EncryptedJWT(jweHeader, Alice)
    jwe.encrypt(new DirectEncrypter(octetSequenceJwk))
    println(s"""private val ALICE_SECRET_JWE = "${jwe.serialize}"""")


    // Alice RSA
    jwsHeader = new JWSHeader(RS256)
    signedJWT = new SignedJWT(jwsHeader, Alice)
    signedJWT.sign(new RSASSASigner(rsaJwk))
    println(s"""private val ALICE_RSA_JWT = "${signedJWT.serialize}"""")
    jweHeader = new JWEHeader(RSA_OAEP_256, A128GCM)
    jwe = new EncryptedJWT(jweHeader, Alice)
    jwe.encrypt(new RSAEncrypter(rsaJwk))
    println(s"""private val ALICE_RSA_JWE = "${jwe.serialize}"""")


    // Alice EC
    jwsHeader = new JWSHeader(ES256)
    signedJWT = new SignedJWT(jwsHeader, Alice)
    signedJWT.sign(new ECDSASigner(ecJwk))
    println(s"""private val ALICE_EC_JWT = "${signedJWT.serialize}"""")
    jweHeader = new JWEHeader(ECDH_ES_A256KW, A256GCM)
    jwe = new EncryptedJWT(jweHeader, Alice)
    jwe.encrypt(new ECDHEncrypter(ecJwk))
    println(s"""private val ALICE_EC_JWE = "${jwe.serialize}"""")


    // Bob RSA
    jwsHeader = new JWSHeader(RS256)
    signedJWT = new SignedJWT(jwsHeader, Bob)
    signedJWT.sign(new RSASSASigner(rsaJwk))
    println(s"""private val BOB_RSA_JWT = "${signedJWT.serialize}"""")
  }

  @throws[JOSEException]
  def main(args: Array[String]): Unit = {
    val octetSequenceKey = generateSecret
    val rsaKey = generateRSA
    val ecKey = generateEC

    printJwkConfiguration(octetSequenceKey, rsaKey, ecKey)
    printJwt(octetSequenceKey, rsaKey, ecKey)
  }
}
