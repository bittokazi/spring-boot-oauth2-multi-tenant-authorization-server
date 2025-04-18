package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.UserService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.pagination.paginationComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.UserList
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.table
import io.kvision.html.tbody
import io.kvision.html.td
import io.kvision.html.th
import io.kvision.html.thead
import io.kvision.html.tr
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
fun Container.userListComponent(): Container {

    val userListObserver = ObservableValue<UserList?>(null)

    var page = 1
    val count = 10

    fun getUsers() {
        UserService.getAll(
            page = page - 1,
            count = count
        ).then {
            userListObserver.setState(it.data)
        }
    }

    fun tableContent(userList: UserList): Div {
        return Div(className = "row mb-3") {
            table(className = "table table-hover my-0") {
                thead {
                    tr {
                        th {
                            content = "#"
                        }
                        th {
                            content = "Firstname"
                        }
                        th {
                            content = "Lastname"
                        }
                        th {
                            content = "Username"
                        }
                        th {
                            content = "Email"
                        }
                        th {
                            content = "Role"
                        }
                        th {
                            content = "Actions"
                        }
                    }
                }

                tbody {
                    userList.users.forEachIndexed { index, user ->
                        tr {
                            td {
                                content = user.id
                            }
                            td {
                                content = user.firstName
                            }
                            td {
                                content = user.lastName
                            }
                            td {
                                content = user.username
                            }
                            td {
                                content = user.email
                            }
                            td {
                                content = user.roles?.firstOrNull()?.title ?: ""
                            }
                            td {
                                link(
                                    "",
                                    AppEngine.APP_DASHBOARD_USER_UPDATE_ROUTE(
                                        user.id!!
                                    ),
                                    dataNavigo = true
                                ) {
                                    span(className = "feather-sm me-1") {
                                        setAttribute("data-feather", "edit")
                                    }
                                    +" Edit"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun pagination(userList: UserList): Div {
        return div(className = "row") {
            add(
                paginationComponent(
                    when (page > 1) {
                        true -> page - 1
                        false -> null
                    },
                    when (page < userList.pages) {
                        true -> page + 1
                        false -> null
                    },
                    page,
                    userList.pages,
                    userList.records,
                    count
                ) { pageNumber ->
                    page = pageNumber
                    getUsers()
                }
            )
        }
    }

    return div {
        userListObserver.subscribe {
            if(it != null) {
                removeAll()
                add(tableContent(it))
                add(pagination(it))

                AppEngine.routing.updatePageLinks()

                window.setTimeout({
                    window["feather"].replace()
                }, 100)
            }
        }
        getUsers()
    }
}
