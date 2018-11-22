/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
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

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/** Abstract Step Up Account storage. */
public class AbstractStepUpAccountStorage {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractStepUpAccountStorage.class);

    /** encryptor for the fields. */
    private TextEncryptor encryptor;

    /** if name parameter should be encrypted. */
    private boolean encryptName;

    /** if target parameter should be encrypted. */
    private boolean encryptTarget;

    /** if key parameter should be encrypted. */
    private boolean encryptKey;

    /**
     * Setter for account field cryptor.
     * 
     * @param cryptor TextEncryptor
     */
    public void setEncryptor(TextEncryptor cryptor) {
        this.encryptor = cryptor;
    }

    /**
     * Setter for name encryption option.
     * 
     * @param encrypt parameter or not
     */
    public void setEncryptName(boolean encrypt) {
        this.encryptName = encrypt;
    }

    /**
     * Setter for target encryption option.
     * 
     * @param encrypt parameter or not
     */
    public void setEncryptTarget(boolean encrypt) {
        this.encryptTarget = encrypt;
    }

    /**
     * Setter for key encryption option.
     * 
     * @param encrypt parameter or not
     */
    public void setEncryptKey(boolean encrypt) {
        this.encryptKey = encrypt;
    }

    /**
     * Encrypts parameter with encryptor.
     * 
     * @param parameter to be encrypted
     * @return encrypted parameter
     * @throws Exception if something unexpected occurs
     */
    protected String encrypt(String parameter) throws Exception {
        if (encryptor == null) {
            throw new Exception("Encryptor not set");
        }
        if (parameter == null) {
            return null;
        }
        String result = encryptor.encrypt(parameter);
        log.debug("Encrypt({})={}", parameter, result);
        return result;
    }

    /**
     * Decrypts parameter with encryptor.
     * 
     * @param parameter to be decrypted
     * @return decrypted parameter
     * @throws Exception if something unexpected occurs
     */
    protected String decrypt(String parameter) throws Exception {
        if (encryptor == null) {
            throw new Exception("Encryptor not set");
        }
        if (parameter == null) {
            return null;
        }
        String result = encryptor.decrypt(parameter);
        log.debug("Decrypt({})={}", parameter, result);
        return result;
    }

    /**
     * Encrypts key if needed.
     * 
     * @param key to be encrypted.
     * @return encrypted key
     * @throws Exception if something unexpected occurs
     */
    protected String encryptKey(String key) throws Exception {
        if (!encryptKey) {
            return key;
        }
        return encrypt(key);
    }

    /**
     * Encrypts name if needed.
     * 
     * @param name to be encrypted.
     * @return encrypted name
     * @throws Exception if something unexpected occurs
     */
    protected String encryptName(String name) throws Exception {
        if (!encryptName) {
            return name;
        }
        return encrypt(name);
    }

    /**
     * Decrypts name if needed.
     * 
     * @param name to be decrypted.
     * @return decrypted name
     * @throws Exception if something unexpected occurs
     */
    protected String decryptName(String name) throws Exception {
        if (!encryptName) {
            return name;
        }
        return decrypt(name);
    }

    /**
     * Encrypts target if needed.
     * 
     * @param target to be encrypted.
     * @return encrypted target
     * @throws Exception if something unexpected occurs
     */
    protected String encryptTarget(String target) throws Exception {
        if (!encryptTarget) {
            return target;
        }
        return encrypt(target);
    }

    /**
     * Decrypts target if needed.
     * 
     * @param target to be decrypted.
     * @return decrypted target
     * @throws Exception if something unexpected occurs
     */
    protected String decryptTarget(String target) throws Exception {
        if (!encryptTarget) {
            return target;
        }
        return decrypt(target);
    }
}
