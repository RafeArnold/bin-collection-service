package uk.co.rafearnold.bincollection.discordbot.repository

import com.fasterxml.jackson.databind.ObjectMapper
import redis.clients.jedis.Jedis
import redis.clients.jedis.Response
import redis.clients.jedis.Transaction
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

internal class RedisUserInfoRepository @Inject constructor(
    private val redisClient: Jedis,
    private val objectMapper: ObjectMapper
) : UserInfoRepository {

    private val lock: Lock = ReentrantLock()

    override fun createUserInfo(userId: String, userInfo: StoredUserInfo): StoredUserInfo =
        lock.withLock {
            val alreadyExists: Long =
                redisClient.setnx(userIdKey(userId = userId), userInfo.serialize())
            if (alreadyExists == 0L) throw UserInfoAlreadyExistsException(userId = userId)
            return userInfo
        }

    override fun loadUserInfo(userId: String): StoredUserInfo? =
        lock.withLock {
            redisClient.get(userIdKey(userId = userId)).deserializeStoredUserInfo()
        }

    override fun updateUserInfo(
        userId: String,
        updateOperations: Iterable<UpdateStoredUserInfoOperation>
    ): StoredUserInfo =
        lock.withLock {
            val userInfo: StoredUserInfo =
                loadUserInfo(userId = userId) ?: throw NoSuchUserInfoFoundException(userId = userId)
            for (updateOperation: UpdateStoredUserInfoOperation in updateOperations) {
                updateOperation.update(userInfo = userInfo)
            }
            redisClient.set(userIdKey(userId = userId), userInfo.serialize())
            userInfo
        }

    override fun deleteUserInfo(userId: String): StoredUserInfo? =
        lock.withLock {
            redisClient.multi().use { transaction: Transaction ->
                val userIdKey: String = userIdKey(userId)
                val getResponse: Response<String?> = transaction.get(userIdKey)
                transaction.del(userIdKey)
                transaction.exec()
                getResponse.get()?.deserializeStoredUserInfo()
            }
        }

    override fun userInfoExists(userId: String): Boolean =
        lock.withLock { redisClient.exists(userIdKey(userId = userId)) }

    override fun loadAllUserInfo(): Map<String, StoredUserInfo> =
        lock.withLock {
            val keys: Array<String> = redisClient.keys(userIdKey(userId = "*")).toTypedArray()
            if (keys.isEmpty()) emptyMap()
            else {
                val userInfo: List<StoredUserInfo?> = redisClient.mget(*keys).map { it?.deserializeStoredUserInfo() }
                val map: MutableMap<String, StoredUserInfo> = mutableMapOf()
                for (i: Int in keys.indices) {
                    map[keys[i].removePrefix(userIdKeyPrefix)] = userInfo[i] ?: continue
                }
                map
            }
        }

    private fun StoredUserInfo.serialize(): String = objectMapper.writeValueAsString(this)

    private fun String?.deserializeStoredUserInfo() =
        this?.let { objectMapper.readValue(it, StoredUserInfo::class.java) }

    companion object {
        private const val userIdKeyPrefix = "uk.co.rafearnold.bin-collection-service.discord-bot.user-info."
        private fun userIdKey(userId: String): String = "$userIdKeyPrefix$userId"
    }
}
