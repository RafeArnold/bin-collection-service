package uk.co.rafearnold.bincollection.http

import io.netty.handler.codec.http.HttpMethod

data class HttpRouteMatchKey(val method: HttpMethod, val path: String)
