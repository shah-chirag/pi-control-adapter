package  in.fortytwo42.adapter.util.handler;


import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.Level;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.fortytwo42.adapter.exception.InvalidTokenException;
import in.fortytwo42.adapter.util.Constant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class JWTokenImpl.
 */
public final class JWTokenImpl {

    /** The jwt token impl. */
    private static String JWT_TOKEN_IMPL = "<<<<< JWTokenImpl";

    private static Logger logger=LogManager.getLogger(JWTokenImpl.class);
    /**
     * Default Constructor.
     */
    private JWTokenImpl() {
        super();
    }

    /**
     * generate token with string key.
     *
     * @param key the key
     * @param id the id
     * @param issuer the issuer
     * @param audiance the audiance
     * @param payload the payload
     * @param tokenValidityInMillis the token validity in millis
     * @return the string
     */
    public static String generateToken(String key, String id, String issuer, String audiance, Map<String, Object> payload, Long tokenValidityInMillis) {
        logger.log(Level.DEBUG, JWT_TOKEN_IMPL + " generateToken : start");
        long currentTimeInMillis = System.currentTimeMillis();
        Date currentDateTime = new Date(currentTimeInMillis + Constant.TOKEN_ACTIVATION_TIME_SPAN);
        Date expirationTime = new Date(currentTimeInMillis + Constant.TOKEN_ACTIVATION_TIME_SPAN + tokenValidityInMillis);
        Key secretKey = new SecretKeySpec(key.getBytes(), SignatureAlgorithm.HS512.getJcaName());
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId(id).setIssuer(issuer)
                .setAudience(audiance).setClaims(payload)
                .setIssuedAt(currentDateTime)
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS512, secretKey);
        logger.log(Level.DEBUG, JWT_TOKEN_IMPL + " generateToken : end");
        return jwtBuilder.compact();
    }

    /**
     * generate token with byte array key.
     *
     * @param key the key
     * @param id the id
     * @param issuer the issuer
     * @param audiance the audiance
     * @param payload the payload
     * @param tokenValidityInMillis the token validity in millis
     * @return the string
     */
    public static String generateToken(byte[] key, String id, String issuer, String audiance, Map<String, Object> payload, Long tokenValidityInMillis) {
        logger.log(Level.DEBUG, JWT_TOKEN_IMPL + " generateToken : start");
        long currentTimeInMillis = System.currentTimeMillis();
        Date currentDateTime = new Date(currentTimeInMillis + Constant.TOKEN_ACTIVATION_TIME_SPAN);
        Date expirationTime = new Date(currentTimeInMillis + Constant.TOKEN_ACTIVATION_TIME_SPAN + tokenValidityInMillis);
        Key secretKey = new SecretKeySpec(key, SignatureAlgorithm.HS512.getJcaName());
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId(id).setIssuer(issuer)
                .setAudience(audiance).setClaims(payload)
                .setIssuedAt(currentDateTime)
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS512, secretKey);
        logger.log(Level.DEBUG, JWT_TOKEN_IMPL + " generateToken : start");
        return jwtBuilder.compact();
    }

    /**
     * Parses the and verify JWT.
     *
     * @param jwt the jwt
     * @param key the key
     * @return true, if successful
     */
    public static boolean parseAndVerifyJWT(String jwt, String key) {
        logger.log(Level.DEBUG, JWT_TOKEN_IMPL + " parseAndVerifyJWT : start");
        Key secretKey = new SecretKeySpec(key.getBytes(), SignatureAlgorithm.HS512.getJcaName());

        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
            return true;
        }
        catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }finally {
            logger.log(Level.DEBUG, JWT_TOKEN_IMPL + " parseAndVerifyJWT : end");
        }
    }

    /**
     * Gets the claim.
     *
     * @param jwt the jwt
     * @param key the key
     * @param claimType the claim type
     * @return the claim
     * @throws InvalidTokenException the invalid token exception
     */
    public static String getClaim(String jwt, String key, String claimType) throws InvalidTokenException {
        Key secretKey = new SecretKeySpec(key.getBytes(), SignatureAlgorithm.HS512.getJcaName());

        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
            Claims claims = jws.getBody();
            return (String) claims.get(claimType);
        }
        catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            throw new InvalidTokenException();
        }
    }

    /**
     * Gets the claim.
     *
     * @param jwt the jwt
     * @param key the key
     * @param claimType the claim type
     * @return the claim
     * @throws InvalidTokenException the invalid token exception
     */
    public static String getClaim(String jwt, byte[] key, String claimType) throws InvalidTokenException {
        Key secretKey = new SecretKeySpec(key, SignatureAlgorithm.HS512.getJcaName());

        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
            Claims claims = jws.getBody();
            return (String) claims.get(claimType);
        }
        catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            throw new InvalidTokenException();
        }
    }

    /**
     * Gets the claim without validation.
     *
     * @param jwt the jwt
     * @param claimKey the claim key
     * @return the claim without validation
     */
    public static Object getClaimWithoutValidation(String jwt, String claimKey) {
        String claims = jwt.split("\\.")[1];
        String jsonClaims = new String(Base64.getDecoder().decode(claims));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> claimsMap = objectMapper.readValue(jsonClaims, new TypeReference<Map<String, Object>>() {
            });
            return claimsMap.get(claimKey);
        }
        catch (IOException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        return null;
    }

    /**
     * get all claims without validation.
     *
     * @param jwt the jwt
     * @return the all claims without validation
     */
    public static Map<String, String> getAllClaimsWithoutValidation(String jwt) {
        String claims = jwt.split("\\.")[1];
        String jsonClaims = new String(Base64.getDecoder().decode(claims));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonClaims, new TypeReference<Map<String, String>>() {
            });
        }
        catch (IOException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        return null;
    }

    public static Map<String,Object> getALlClaimsWithoutValidationCAMAccessToken(String token){
        String claims = token.split("\\.")[1];
        String jsonClaims = new String(Base64.getDecoder().decode(claims));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonClaims, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (IOException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        return null;
    }
}
