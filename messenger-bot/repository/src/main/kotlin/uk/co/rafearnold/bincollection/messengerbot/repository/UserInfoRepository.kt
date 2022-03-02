package uk.co.rafearnold.bincollection.messengerbot.repository

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo

interface UserInfoRepository {

    fun createUserInfo(userId: String, userInfo: StoredUserInfo): StoredUserInfo

    fun loadUserInfo(userId: String): StoredUserInfo?

    fun updateUserInfo(userId: String, updateOperations: Iterable<UpdateStoredUserInfoOperation>): StoredUserInfo

    fun deleteUserInfo(userId: String): StoredUserInfo?

    fun userInfoExists(userId: String): Boolean

    fun loadAllUserInfo(): Map<String, StoredUserInfo>
}
