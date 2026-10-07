package rsa;

import java.math.BigInteger;

// The public part of an RSA key: modulus n = p * q and public exponent e.
public record RsaPublicKey(BigInteger n, BigInteger e){}
