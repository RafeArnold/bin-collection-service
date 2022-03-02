package uk.co.rafearnold.bincollection

import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.Jedis
import javax.inject.Inject
import javax.inject.Provider

internal class RedisClientProvider @Inject constructor(
    private val appProps: Map<String, String>
) : Provider<Jedis> {

    override fun get(): Jedis {
        val host: String = appProps.getValue("redis.connection.host")
        val port: Int = appProps.getValue("redis.connection.port").toInt()
        val password: String? = appProps["redis.connection.password"]
        val configBuilder: DefaultJedisClientConfig.Builder = DefaultJedisClientConfig.builder()
        if (password != null) configBuilder.password(password)
        return Jedis(host, port, configBuilder.build())
    }
}
