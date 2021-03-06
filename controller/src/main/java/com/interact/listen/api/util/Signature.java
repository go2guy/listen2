package com.interact.listen.api.util;

import com.interact.listen.exception.ListenServletException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

public final class Signature
{
    private static final byte[] K = Base64.decodeBase64(new byte[] {
                                     77, 73, 73, 69, 111, 119, 73, 66, 65, 65, 75, 67, 65, 81, 69, 65, 53, 78, 109,
                                     73, 56, 71, 73, 115, 117, 112, 117, 118, 79, 77, 88, 82, 52, 121, 102, 115, 56,
                                     104, 75, 50, 82, 85, 109, 88, 47, 67, 75, 108, 72, 109, 76, 69, 114, 47, 98, 49,
                                     109, 80, 114, 47, 103, 120, 43, 102, 101, 110, 97, 102, 79, 83, 75, 111, 88, 115,
                                     55, 79, 85, 118, 85, 120, 70, 47, 67, 82, 48, 69, 87, 54, 105, 85, 74, 118, 74,
                                     57, 108, 109, 84, 65, 67, 84, 90, 70, 115, 84, 102, 70, 43, 109, 83, 74, 90, 55,
                                     54, 100, 112, 117, 104, 54, 74, 56, 66, 81, 106, 57, 108, 112, 110, 72, 43, 65,
                                     69, 112, 48, 53, 76, 113, 76, 114, 50, 122, 118, 108, 122, 107, 107, 115, 114,
                                     106, 89, 83, 87, 52, 104, 102, 97, 90, 113, 102, 85, 90, 68, 107, 56, 89, 118, 71,
                                     115, 100, 66, 113, 68, 112, 87, 114, 69, 121, 107, 68, 49, 54, 82, 53, 74, 118,
                                     53, 73, 119, 47, 89, 49, 51, 74, 100, 57, 57, 70, 53, 122, 89, 85, 43, 90, 51, 77,
                                     43, 88, 100, 66, 83, 114, 70, 83, 97, 68, 119, 85, 53, 71, 101, 105, 79, 81, 86,
                                     121, 108, 56, 113, 47, 66, 116, 57, 103, 89, 55, 79, 49, 72, 102, 113, 89, 89, 57,
                                     117, 100, 88, 109, 65, 122, 80, 102, 69, 97, 90, 100, 67, 113, 67, 106, 55, 66,
                                     56, 86, 56, 83, 106, 55, 87, 99, 57, 50, 84, 90, 47, 102, 72, 97, 98, 90, 70, 75,
                                     122, 102, 104, 86, 119, 102, 72, 67, 65, 122, 75, 52, 109, 81, 90, 49, 98, 101,
                                     56, 115, 110, 74, 100, 49, 102, 57, 82, 50, 112, 101, 113, 106, 122, 66, 69, 73,
                                     78, 71, 100, 79, 110, 65, 110, 109, 48, 114, 71, 73, 116, 75, 90, 74, 89, 57, 49,
                                     76, 77, 89, 105, 53, 72, 54, 87, 104, 47, 81, 104, 50, 49, 67, 89, 89, 52, 78,
                                     101, 50, 119, 119, 73, 68, 65, 81, 65, 66, 65, 111, 73, 66, 65, 69, 75, 88, 81,
                                     103, 43, 103, 111, 90, 57, 84, 79, 102, 78, 116, 76, 74, 118, 75, 118, 70, 110,
                                     99, 78, 65, 109, 74, 86, 112, 53, 90, 102, 109, 54, 80, 69, 117, 105, 90, 70, 102,
                                     73, 68, 53, 50, 72, 67, 83, 43, 101, 89, 113, 78, 65, 53, 85, 52, 68, 121, 56, 72,
                                     113, 88, 79, 107, 102, 98, 67, 114, 76, 116, 57, 48, 43, 70, 99, 48, 55, 72, 74,
                                     99, 115, 114, 120, 55, 102, 71, 65, 75, 43, 75, 76, 90, 113, 108, 110, 122, 122,
                                     51, 65, 72, 54, 98, 79, 122, 100, 68, 51, 72, 90, 56, 72, 81, 72, 47, 90, 75, 112,
                                     90, 55, 54, 98, 87, 77, 72, 49, 79, 68, 110, 122, 103, 97, 76, 87, 87, 65, 108,
                                     71, 73, 53, 107, 72, 99, 80, 103, 81, 53, 52, 57, 113, 47, 50, 70, 120, 98, 97,
                                     107, 117, 110, 107, 67, 48, 69, 108, 112, 90, 73, 43, 67, 73, 113, 87, 69, 102,
                                     48, 122, 78, 102, 84, 88, 79, 112, 69, 120, 77, 87, 120, 43, 70, 69, 78, 110, 116,
                                     107, 47, 113, 112, 72, 105, 106, 69, 43, 122, 99, 98, 104, 49, 47, 99, 121, 56,
                                     66, 115, 109, 106, 48, 87, 99, 88, 90, 51, 97, 84, 68, 69, 108, 71, 51, 88, 67,
                                     67, 50, 49, 114, 80, 100, 55, 122, 103, 114, 70, 101, 76, 54, 83, 121, 50, 54, 66,
                                     114, 48, 101, 113, 120, 113, 100, 100, 97, 116, 103, 104, 90, 72, 66, 53, 80, 104,
                                     90, 89, 69, 52, 66, 89, 77, 51, 65, 108, 90, 84, 90, 86, 102, 117, 68, 72, 111,
                                     103, 88, 110, 52, 66, 88, 73, 76, 84, 121, 86, 43, 111, 81, 72, 78, 85, 122, 72,
                                     75, 106, 83, 116, 102, 86, 114, 121, 74, 110, 115, 108, 110, 87, 86, 65, 70, 53,
                                     119, 104, 97, 88, 71, 109, 49, 102, 121, 82, 89, 51, 87, 66, 65, 69, 109, 117, 69,
                                     67, 103, 89, 69, 65, 43, 65, 105, 118, 66, 99, 99, 113, 110, 75, 102, 103, 66, 55,
                                     66, 84, 68, 84, 85, 77, 67, 100, 116, 55, 118, 43, 118, 86, 98, 82, 55, 67, 69,
                                     90, 99, 53, 47, 106, 67, 82, 121, 43, 78, 48, 82, 54, 78, 78, 47, 76, 47, 43, 76,
                                     100, 65, 65, 106, 115, 111, 100, 106, 122, 86, 109, 107, 76, 79, 85, 122, 80, 98,
                                     90, 104, 119, 55, 50, 104, 72, 88, 65, 54, 51, 82, 105, 106, 65, 104, 83, 68, 101,
                                     103, 56, 73, 100, 67, 85, 89, 49, 89, 86, 121, 103, 121, 114, 53, 75, 90, 112, 72,
                                     82, 110, 110, 57, 54, 88, 74, 79, 117, 99, 80, 67, 67, 70, 87, 70, 102, 105, 84,
                                     111, 74, 50, 81, 122, 43, 74, 102, 90, 77, 66, 85, 116, 57, 72, 104, 115, 83, 87,
                                     71, 83, 50, 106, 70, 50, 104, 85, 71, 106, 105, 120, 104, 82, 119, 70, 119, 84,
                                     100, 53, 120, 116, 89, 115, 67, 103, 89, 69, 65, 55, 68, 77, 101, 48, 98, 74, 101,
                                     70, 51, 51, 54, 85, 116, 103, 114, 89, 57, 77, 78, 86, 67, 49, 115, 99, 47, 65,
                                     56, 115, 69, 101, 89, 116, 108, 114, 52, 55, 114, 99, 76, 108, 82, 68, 98, 111,
                                     97, 79, 57, 80, 82, 115, 117, 73, 77, 115, 115, 120, 105, 90, 47, 57, 117, 82,
                                     107, 89, 80, 49, 70, 68, 109, 83, 51, 83, 56, 112, 103, 119, 103, 52, 85, 117,
                                     111, 86, 85, 89, 72, 103, 53, 84, 116, 57, 53, 105, 113, 51, 97, 75, 48, 66, 82,
                                     81, 110, 112, 75, 117, 71, 73, 101, 114, 98, 66, 67, 119, 70, 85, 66, 90, 77, 119,
                                     112, 80, 49, 68, 87, 51, 102, 77, 75, 116, 79, 81, 53, 112, 106, 111, 86, 55, 52,
                                     116, 113, 82, 43, 100, 102, 55, 103, 68, 48, 104, 87, 108, 102, 77, 70, 86, 56,
                                     87, 85, 80, 57, 118, 88, 76, 97, 43, 88, 66, 99, 87, 113, 107, 67, 103, 89, 69,
                                     65, 56, 53, 115, 98, 119, 51, 73, 69, 115, 81, 51, 85, 89, 57, 106, 84, 67, 83,
                                     75, 122, 113, 121, 55, 78, 85, 81, 99, 103, 102, 71, 98, 56, 78, 109, 105, 119,
                                     66, 97, 55, 81, 85, 48, 56, 88, 85, 112, 68, 97, 116, 77, 89, 103, 115, 65, 65,
                                     100, 118, 67, 66, 89, 102, 101, 72, 49, 49, 87, 76, 55, 88, 51, 43, 71, 48, 68,
                                     90, 113, 43, 108, 102, 111, 51, 90, 104, 87, 102, 98, 66, 105, 88, 116, 82, 98,
                                     48, 116, 53, 89, 68, 50, 82, 113, 84, 67, 75, 55, 53, 80, 116, 111, 79, 55, 80,
                                     73, 57, 53, 114, 49, 108, 65, 117, 66, 52, 80, 116, 85, 52, 73, 108, 101, 47, 82,
                                     52, 107, 76, 51, 106, 110, 78, 106, 52, 77, 78, 117, 112, 70, 88, 48, 89, 54, 113,
                                     117, 47, 66, 101, 116, 113, 120, 115, 73, 116, 52, 69, 49, 81, 102, 90, 43, 116,
                                     49, 66, 78, 99, 67, 103, 89, 66, 114, 68, 105, 67, 66, 50, 116, 53, 97, 116, 51,
                                     97, 108, 53, 101, 83, 69, 115, 106, 118, 119, 85, 48, 89, 56, 112, 106, 53, 98,
                                     104, 53, 102, 110, 122, 119, 80, 85, 55, 112, 73, 74, 86, 107, 75, 49, 50, 73,
                                     107, 70, 69, 84, 83, 118, 71, 71, 101, 75, 121, 66, 104, 110, 120, 115, 122, 89,
                                     83, 80, 76, 114, 117, 121, 112, 52, 53, 53, 108, 68, 87, 121, 53, 53, 43, 56, 82,
                                     113, 108, 82, 77, 107, 100, 74, 87, 97, 68, 89, 73, 56, 54, 69, 72, 115, 90, 53,
                                     70, 71, 85, 80, 75, 109, 116, 113, 85, 75, 108, 51, 121, 121, 79, 118, 98, 88, 65,
                                     56, 84, 102, 104, 68, 68, 117, 72, 67, 77, 107, 47, 70, 55, 69, 50, 43, 79, 111,
                                     65, 50, 54, 118, 97, 83, 57, 113, 54, 72, 43, 69, 89, 76, 113, 106, 109, 118, 86,
                                     43, 48, 72, 102, 47, 90, 114, 88, 49, 81, 81, 75, 66, 103, 71, 100, 56, 114, 57,
                                     119, 105, 43, 102, 80, 71, 51, 73, 103, 71, 88, 72, 66, 81, 106, 109, 110, 86,
                                     103, 97, 80, 78, 115, 118, 81, 122, 66, 75, 70, 109, 72, 114, 69, 82, 48, 47, 105,
                                     76, 90, 117, 65, 57, 65, 50, 82, 53, 120, 55, 68, 120, 90, 100, 72, 85, 83, 82,
                                     87, 97, 80, 65, 68, 73, 97, 72, 105, 85, 49, 79, 57, 106, 98, 78, 74, 67, 107, 55,
                                     74, 116, 110, 113, 110, 55, 77, 56, 53, 81, 48, 83, 82, 115, 113, 90, 104, 65, 50,
                                     43, 50, 56, 47, 49, 98, 109, 113, 114, 84, 107, 81, 109, 84, 55, 84, 57, 81, 52,
                                     43, 104, 85, 69, 78, 43, 113, 101, 104, 90, 120, 56, 51, 66, 107, 82, 89, 97, 80,
                                     49, 81, 87, 117, 72, 49, 49, 85, 99, 82, 120, 70, 114, 43, 79, 51, 72, 78, 88,
                                     108, 67, 57, 109, 65, 71, 47, 122, 116, 54, 72, 104, 117, 86 });

    private Signature()
    {
        throw new AssertionError("Cannot instantiate utility class Signature");
    }

    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.err.println("Usage: com.interact.listen.api.security.AuthenticationFilter <date>");
            System.exit(1);
        }

        try
        {
            String signature = create(args[0]);
            System.out.println(signature);
        }
        catch(ServletException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String create(String date) throws ServletException
    {
        try
        {
            SecretKeySpec key = new SecretKeySpec(K, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);

            byte[] raw = mac.doFinal(date.getBytes());
            return new String(Base64.encodeBase64(raw));
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        catch(InvalidKeyException e)
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}
