package com.iverpa.mpi.security;

import com.iverpa.mpi.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

@Service
public class JwtUtils {

    @Value("${private.key.filename}")
    private String privateKeyFileName;
    @Value("${jwt.expiration-time:1800000}")
    private Long expirationTime;

    private final PrivateKey privateKey;

    public JwtUtils() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = Files.readAllBytes(Paths.get(privateKeyFileName));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        privateKey = keyFactory.generatePrivate(keySpec);
    }

    public String generateToken(String username, Set<Role> roles) {
        return generateToken(username, roles, privateKey);
    }

    private String generateToken(String username, Set<Role> roles, PrivateKey privateKey) {
        var claims = new HashMap<String, Object>();
        claims.put("username", username);
        claims.put("roles", roles);
        var expirationTime = new Date(new Date().getTime() + this.expirationTime);

        return Jwts.builder().setClaims(claims).setExpiration(expirationTime).signWith(privateKey, SignatureAlgorithm.RS512).compact();
    }
}
