package com.yigit.chat;

/**
 * Created by Mahmut on 1.07.2018.
 */
import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class RSA {
    public BigInteger p;
    public BigInteger q;
    public BigInteger N;
    public BigInteger phi;
    public BigInteger e;
    public BigInteger d;
    public int bitlength = 2048;
    public Random r;



    public RSA(){
        r = new Random();
        p = BigInteger.probablePrime(bitlength, r);
        q = BigInteger.probablePrime(bitlength, r);
        N = p.multiply(q);
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = BigInteger.probablePrime(bitlength / 2, r);

        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0)
        {
            e.add(BigInteger.ONE);
        }
        d = e.modInverse(phi);
        Log.d("privateRSA",d.toString());


    }
    public RSA(BigInteger e, BigInteger d, BigInteger N)
    {
        this.e = e;
        this.d = d;
        this.N = N;
    }
    private static String bytesToString(byte[] encrypted)
    {
        String test = "";
        for (byte b : encrypted)
        {
            test += Byte.toString(b);
        }
        return test;
    }
    // Encrypt message
    public byte[] encrypt(byte[] message)
    {
        return (new BigInteger(message)).modPow(e, N).toByteArray();
    }

    // Decrypt message
    public byte[] decrypt(byte[] message)
    {
        return (new BigInteger(message)).modPow(d, N).toByteArray();
    }
}
