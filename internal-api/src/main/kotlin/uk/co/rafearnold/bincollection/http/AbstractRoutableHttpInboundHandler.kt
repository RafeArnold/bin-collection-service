package uk.co.rafearnold.bincollection.http

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod

abstract class AbstractRoutableHttpInboundHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    protected abstract val methods: Set<HttpMethod>
    protected abstract val path: String?

    override fun acceptInboundMessage(msg: Any?): Boolean =
        super.acceptInboundMessage(msg) && (msg as FullHttpRequest).isAcceptableRoute()

    private fun FullHttpRequest.isAcceptableRoute(): Boolean = this.isAcceptableMethod() && this.isAcceptablePath()

    private fun FullHttpRequest.isAcceptableMethod(): Boolean = methods.isEmpty() || this.method() in methods

    private fun FullHttpRequest.isAcceptablePath(): Boolean = path == null || this.uri().substringBefore('?') == path
}
