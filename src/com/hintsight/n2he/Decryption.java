package com.hintsight.n2he;

import static com.hintsight.n2he.Parameters.*;
import static com.hintsight.n2he.Utils.*;

public class Decryption {
    public static Boolean decrypt(long[] encryptedResult, int[] secretKey) {
        return true;
    }

    public static long lwe64Dec(long[] encryptedResult, int[] secretKey, int polySize) {
        //computing (b + <a, s>) mod q
        long alpha = getCiphertextModulus() / getPlaintextModulus();
        long result = encryptedResult[polySize];

        for (int i = 0; i < polySize; i++) {
            result += (encryptedResult[i] * (long)secretKey[i]);
            result = modq(result, getCiphertextModulus());
        }

        while (result < 0) {
            result += getCiphertextModulus();
        }

        result = (result + alpha/2) % getCiphertextModulus();
        result /= alpha;
        if (result > getPlaintextModulus()/2) {
            result -= getPlaintextModulus();
        }

        return result;
    }
}
