/*
 * The MIT License
 * Copyright (c) 2015-2021 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.csc.idp.stepup.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AttributeDecryptor {

    private String key = "";

    private String cipherName = "AES/ECB/PKCS5Padding";

    private String keySpecName = "AES";

    private String digestName = "SHA-256";

    public void setKeySpecName(String keySpecName) {
        this.keySpecName = keySpecName;
    }

    public void setCipherName(String cipherName) {
        this.cipherName = cipherName;
    }

    public void setDigestName(String digestName) {
        this.digestName = digestName;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String decrypt(String data) throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(cipherName);
        MessageDigest sha = MessageDigest.getInstance(digestName);
        sha.update(key.getBytes("UTF-8"));
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sha.digest(), keySpecName));
        return new String(cipher.doFinal(Base64.getDecoder().decode(data))).split(":")[1];
    }
}
