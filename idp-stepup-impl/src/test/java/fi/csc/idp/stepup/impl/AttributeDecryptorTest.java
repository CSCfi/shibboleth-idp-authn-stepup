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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.shibboleth.shared.component.ComponentInitializationException;

public class AttributeDecryptorTest {

    private AttributeDecryptor decryptor;

    @BeforeMethod
    public void setup() {
        decryptor = new AttributeDecryptor();
        decryptor.setKey("secretpassword1234567890");
    }

    public String encrypt(String data, String key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(key.getBytes("UTF-8"));
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sha.digest(), "AES"));
        return new String(Base64.getEncoder().encode(cipher.doFinal((key + ":" + data).getBytes("UTF-8"))));
    }

    @Test
    public void testSuccess() throws ComponentInitializationException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        // Use preset test vector to test decryption
        Assert.assertEquals(decryptor.decrypt("9ju34ArATuHxvs0qXw2Y/Z2/6MNQQQxhKykDS3bQDwI="), "123456");
        // Apply local helper to produce same result as with test vector
        Assert.assertEquals(decryptor.decrypt(encrypt("123456", "secretpassword1234567890")), "123456");
        // Apply a real world value for TOTP seed
        Assert.assertEquals(decryptor.decrypt(encrypt("JBSWY3DPEHPK3PXP", "secretpassword1234567890")),
                "JBSWY3DPEHPK3PXP");
    }

}
