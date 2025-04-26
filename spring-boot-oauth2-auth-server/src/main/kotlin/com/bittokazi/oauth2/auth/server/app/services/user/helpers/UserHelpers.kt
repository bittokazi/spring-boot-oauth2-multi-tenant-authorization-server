package com.bittokazi.oauth2.auth.server.app.services.user.helpers

import com.bittokazi.oauth2.auth.server.app.models.tenant.User
import com.bittokazi.oauth2.auth.server.app.models.tenant.UserList
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.utils.Utils.getMD5
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.security.NoSuchAlgorithmException
import java.util.stream.Collectors

/**
 * @author Bitto Kazi
 */
object UserHelpers {
    fun getUsers(page: Int, count: Int, userRepository: UserRepository): Any {
        val reqCount: Pageable = PageRequest.of(page, count, Sort.by(Sort.Direction.DESC, "createdDate"))
        val pages = userRepository.findAll(reqCount)
        return UserList(
            pages = pages.totalPages,
            records = pages.totalElements,
            users = setUsersImage(pages.content)
        )
    }

    fun setUserImage(user: User?): User? {
        try {
            if (user != null) {
                user.avatarImage = "https://www.gravatar.com/avatar/" + getMD5(
                    user!!.email!!
                ) + "?d=identicon"
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return user
    }

    fun setUsersImage(users: MutableList<User?>): List<User?> {
        return users.stream().map { user: User? -> setUserImage(user) }
            .collect(Collectors.toList())
    }
}
