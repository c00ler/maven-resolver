/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.eclipse.aether.transport.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.AuthenticationContext;
import org.eclipse.aether.util.ConfigUtils;

/**
 * Connection manager config: among other SSL-related configuration and cache key for connection pools (whose scheme
 * registries are derived from this config).
 */
final class ConnMgrConfig {

    private static final String CIPHER_SUITES = "https.cipherSuites";

    private static final String PROTOCOLS = "https.protocols";

    final SSLContext context;

    final HostnameVerifier verifier;

    final String[] cipherSuites;

    final String[] protocols;

    final String httpsSecurityMode;

    final int connectionMaxTtlSeconds;

    final int maxConnectionsPerRoute;

    ConnMgrConfig(
            RepositorySystemSession session,
            AuthenticationContext authContext,
            String httpsSecurityMode,
            int connectionMaxTtlSeconds,
            int maxConnectionsPerRoute) {
        context = (authContext != null) ? authContext.get(AuthenticationContext.SSL_CONTEXT, SSLContext.class) : null;
        verifier = (authContext != null)
                ? authContext.get(AuthenticationContext.SSL_HOSTNAME_VERIFIER, HostnameVerifier.class)
                : null;

        cipherSuites = split(get(session, CIPHER_SUITES));
        protocols = split(get(session, PROTOCOLS));
        this.httpsSecurityMode = httpsSecurityMode;
        this.connectionMaxTtlSeconds = connectionMaxTtlSeconds;
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    private static String get(RepositorySystemSession session, String key) {
        String value = ConfigUtils.getString(session, null, "aether.connector." + key, key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    private static String[] split(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.split(",+");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        ConnMgrConfig that = (ConnMgrConfig) obj;
        return Objects.equals(context, that.context)
                && Objects.equals(verifier, that.verifier)
                && Arrays.equals(cipherSuites, that.cipherSuites)
                && Arrays.equals(protocols, that.protocols)
                && Objects.equals(httpsSecurityMode, that.httpsSecurityMode)
                && connectionMaxTtlSeconds == that.connectionMaxTtlSeconds
                && maxConnectionsPerRoute == that.maxConnectionsPerRoute;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + hash(context);
        hash = hash * 31 + hash(verifier);
        hash = hash * 31 + Arrays.hashCode(cipherSuites);
        hash = hash * 31 + Arrays.hashCode(protocols);
        hash = hash * 31 + hash(httpsSecurityMode);
        hash = hash * 31 + hash(connectionMaxTtlSeconds);
        hash = hash * 31 + hash(maxConnectionsPerRoute);
        return hash;
    }

    private static int hash(Object obj) {
        return obj != null ? obj.hashCode() : 0;
    }
}
