/*
 * Copyright 2015 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.server;

import java.io.File;
import java.util.function.Function;

import javax.net.ssl.SSLException;

import com.linecorp.armeria.common.Request;
import com.linecorp.armeria.common.Response;
import com.linecorp.armeria.common.SessionProtocol;

import io.netty.handler.ssl.SslContext;

/**
 * Builds a new {@link VirtualHost}.
 * <h2>Example</h2>
 * <pre>{@code
 * VirtualHostBuilder vhb = new VirtualHostBuilder("*.example.com");
 * vhb.serviceAt("/foo", new FooService())
 *    .serviceUnder("/bar/", new BarService())
 *    .service(PathMapping.ofRegex("^/baz/.*", new BazService());
 *
 * VirtualHost vh = vhb.build();
 * }</pre>
 *
 * @see PathMapping
 */
public class VirtualHostBuilder {

    private InternalVirtualHostBuilder internalVirtualHostBuilder;
    
    /**
     * Creates a new {@link VirtualHostBuilder} whose hostname pattern is {@code "*"} (match-all).
     */
    public VirtualHostBuilder() {
        internalVirtualHostBuilder =
                new InternalVirtualHostBuilder(InternalVirtualHostBuilder.LOCAL_HOSTNAME, "*");
    }

    /**
     * Creates a new {@link VirtualHostBuilder} with the specified hostname pattern.
     */
    public VirtualHostBuilder(String hostnamePattern) {
        internalVirtualHostBuilder = new InternalVirtualHostBuilder(hostnamePattern);
    }

    /**
     * Creates a new {@link VirtualHostBuilder} with the specified hostname pattern.
     */
    public VirtualHostBuilder(String defaultHostname, String hostnamePattern) {
        internalVirtualHostBuilder = new InternalVirtualHostBuilder(defaultHostname, hostnamePattern);
    }

    /**
     * Sets the {@link SslContext} of this {@link VirtualHost}.
     */
    public VirtualHostBuilder sslContext(SslContext sslContext) {
        internalVirtualHostBuilder.sslContext(sslContext);
        return this;
    }

    /**
     * Sets the {@link SslContext} of this {@link VirtualHost} from the specified {@link SessionProtocol},
     * {@code keyCertChainFile} and cleartext {@code keyFile}.
     */
    public VirtualHostBuilder sslContext(
            SessionProtocol protocol, File keyCertChainFile, File keyFile) throws SSLException {
        return sslContext(protocol, keyCertChainFile, keyFile, null);
    }

    /**
     * Sets the {@link SslContext} of this {@link VirtualHost} from the specified {@link SessionProtocol},
     * {@code keyCertChainFile}, {@code keyFile} and {@code keyPassword}.
     */
    public VirtualHostBuilder sslContext(
            SessionProtocol protocol,
            File keyCertChainFile, File keyFile, String keyPassword) throws SSLException {

        internalVirtualHostBuilder.sslContext(protocol, keyCertChainFile, keyFile, keyPassword);
        return this;
    }

    /**
     * Binds the specified {@link Service} at the specified exact path.
     */
    public VirtualHostBuilder serviceAt(String exactPath, Service<?, ?> service) {
        return service(PathMapping.ofExact(exactPath), service);
    }

    /**
     * Binds the specified {@link Service} under the specified directory..
     */
    public VirtualHostBuilder serviceUnder(String pathPrefix, Service<?, ?> service) {
        return service(PathMapping.ofPrefix(pathPrefix), service);
    }

    /**
     * Binds the specified {@link Service} at the specified {@link PathMapping}.
     */
    public VirtualHostBuilder service(PathMapping pathMapping, Service<?, ?> service) {
        internalVirtualHostBuilder.service(pathMapping, service);
        return this;
    }

    /**
     * Binds the specified {@link Service} at the specified {@link PathMapping}.
     *
     * @param loggerName the name of the {@linkplain ServiceRequestContext#logger() service logger};
     *                   must be a string of valid Java identifier names concatenated by period ({@code '.'}),
     *                   such as a package name or a fully-qualified class name
     */
    public VirtualHostBuilder service(PathMapping pathMapping, Service<?, ?> service, String loggerName) {
        internalVirtualHostBuilder.service(pathMapping, service, loggerName);
        return this;
    }

    /**
     * Decorates all {@link Service}s with the specified {@code decorator}.
     *
     * @param decorator the {@link Function} that decorates a {@link Service}
     * @param <T> the type of the {@link Service} being decorated
     * @param <R> the type of the {@link Service} {@code decorator} will produce
     */
    public <T extends Service<T_I, T_O>, T_I extends Request, T_O extends Response,
            R extends Service<R_I, R_O>, R_I extends Request, R_O extends Response>
    VirtualHostBuilder decorator(Function<T, R> decorator) {

        internalVirtualHostBuilder.decorator(decorator);
        return this;
    }

    /**
     * Creates a new {@link VirtualHost}.
     */
    public VirtualHost build() {
        return internalVirtualHostBuilder.build();
    }

    @Override
    public String toString() {
        return internalVirtualHostBuilder.toString();
    }
}
