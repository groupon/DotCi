/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
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

package com.groupon.jenkins.util;

import com.groupon.jenkins.github.DeployKeyPair;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class KeyPairGenerator {
    public DeployKeyPair generateKeyPair() {
        final JSch jsch = new JSch();
        try {
            final KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            final ByteArrayOutputStream privateKeyStream = new ByteArrayOutputStream();
            kpair.writePrivateKey(privateKeyStream);

            final ByteArrayOutputStream publicKeyStream = new ByteArrayOutputStream();
            kpair.writePublicKey(publicKeyStream, "");
            return new DeployKeyPair(new String(publicKeyStream.toByteArray(), "UTF-8"), new String(privateKeyStream.toByteArray(), "UTF-8"));
        } catch (final JSchException e) {
            throw new RuntimeException(e);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }


    }
}
