/*
 * Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openehealth.ipf.commons.audit.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.openehealth.ipf.commons.audit.AuditException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Simple Netty client implementation of RFC 5425 TLS syslog transport
 * for sending audit messages to an Audit Record Repository that implements TLS syslog.
 * Multiple messages may be sent over the same socket.
 *
 * @author Christian Ohr
 * @since 3.7
 */
public class NettyTLSSyslogSenderImpl extends NioTLSSyslogSenderImpl<ChannelFuture> {

    private static final Logger LOG = LoggerFactory.getLogger(NettyTLSSyslogSenderImpl.class);

    private int workerThreads = 1;
    private long connectTimeoutMillis = 5000;
    private long sendTimeoutMillis = 5000;

    public String getTransportName() {
        return AuditTransmissionChannel.NETTY_TLS.getProtocolName();
    }

    @Override
    protected NioTLSSyslogSenderImpl.Destination<ChannelFuture> makeDestination(String host, int port, boolean logging) {
        return new NettyDestination(host, port, workerThreads, connectTimeoutMillis, sendTimeoutMillis, logging);
    }

    public void setConnectTimeout(int value, TimeUnit timeUnit) {
        this.connectTimeoutMillis = timeUnit.toMillis(value);
    }

    public void setSendTimeout(int value, TimeUnit timeUnit) {
        this.sendTimeoutMillis = timeUnit.toMillis(value);
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    private static final class NettyDestination implements Destination<ChannelFuture> {
        private long connectTimeout;
        private long sendTimeout;
        private Bootstrap bootstrap;
        private EventLoopGroup workerGroup;
        private ChannelFuture channelFuture;
        private final String host;
        private final int port;

        NettyDestination(String host, int port, int workerThreads, long connectTimeout, long sendTimeout, boolean withLogging) {

            this.workerGroup = new NioEventLoopGroup(workerThreads);
            this.connectTimeout = connectTimeout;
            this.sendTimeout = sendTimeout;
            this.host = host;
            this.port = port;

            // Configure the client.
            this.bootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .remoteAddress(host, port)
                    .handler(new InitializerHandler(host, port, withLogging));
        }

        @Override
        public void shutdown() {
            if (workerGroup != null) {
                LOG.info("TLS Channel to Audit Repository at {}:{} is closed", host, port);
                workerGroup.shutdownGracefully();
            }
        }

        @Override
        public ChannelFuture getHandle() {
            if (channelFuture == null || !channelFuture.channel().isActive()) {
                try {
                    channelFuture = bootstrap.connect();
                    if (channelFuture == null || !channelFuture.await(connectTimeout)) {
                        throw new AuditException("Could not establish TLS connection to " + host + ":" + port);
                    }
                } catch (InterruptedException e) {
                    throw new AuditException("Interrupted while establishing TLS connection to " + host + ":" + port, e);
                }
            }
            return channelFuture;
        }

        @Override
        public void write(byte[]... bytes) {
            // The write operation is asynchronous.
            Channel channel = getHandle().channel();
            LOG.trace("Writing {} bytes using session: {}", bytes.length, channel);
            try {
                if (!channel.writeAndFlush(Unpooled.wrappedBuffer(bytes)).await(sendTimeout)) {
                    throw new AuditException("Could not send audit message to " + host + ":" + port);
                }
            } catch (InterruptedException e) {
                throw new AuditException("Interrupted during sending audit message to " + host + ":" + port, e);
            }
        }

        /**
         * Handler called upon channel events
         */
        private static class InboundHandler extends ChannelInboundHandlerAdapter {

            private final String host;
            private final int port;

            public InboundHandler(String host, int port) {
                this.host = host;
                this.port = port;
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                LOG.info("TLS Channel to Audit Repository at {}:{} is now active", host, port);
                super.channelActive(ctx);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                LOG.info("Exception on receiving message for context {}", ctx, cause);
                if (ctx != null) {
                    ctx.close();
                }
            }
        }

        /**
         * Handler called upon setup
         */
        private static class InitializerHandler extends ChannelInitializer<SocketChannel> {
            private final String host;
            private final int port;
            private final boolean withLogging;

            public InitializerHandler(String host, int port, boolean withLogging) {
                this.host = host;
                this.port = port;
                this.withLogging = withLogging;
            }

            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                SslContext sslContext = initSslContext();
                pipeline.addLast(sslContext.newHandler(channel.alloc(), host, port));
                pipeline.addLast(new InboundHandler(host, port));
                if (withLogging) {
                    channel.pipeline().addLast(new LoggingHandler(getClass(), LogLevel.DEBUG));
                }
            }

            private SslContext initSslContext() throws NoSuchAlgorithmException {
                String allowedProtocols = System.getProperty(JDK_TLS_CLIENT_PROTOCOLS, "TLSv1.2");
                String[] protocols = Stream.of(allowedProtocols.split("\\s*,\\s*"))
                        .toArray(String[]::new);
                return new JdkSslContext(
                        SSLContext.getDefault(),
                        true,
                        null, // use default
                        SupportedCipherSuiteFilter.INSTANCE,
                        ApplicationProtocolConfig.DISABLED,
                        ClientAuth.REQUIRE,
                        protocols,
                        false
                );
            }
        }
    }


}
