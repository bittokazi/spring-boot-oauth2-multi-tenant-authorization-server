package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.ClientService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.Cursor
import io.kvision.core.onClick
import io.kvision.html.Tbody
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.table
import io.kvision.html.tbody
import io.kvision.html.td
import io.kvision.html.th
import io.kvision.html.thead
import io.kvision.html.tr
import io.kvision.panel.SimplePanel
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
class ClientListComponent: SimplePanel() {

    val deleteObserver = ObservableValue<Boolean?>(null)

    fun delete(id: String) {
        ClientService.delete(id).then {
            sweetAlert.fire(
                Json.encodeToDynamic(
                    mapOf(
                        "title" to "Success",
                        "text" to "Deleted Client with ID [$id]",
                        "icon" to "success"
                    )
                )
            )
            deleteObserver.setState(true)
        }.catch {
            sweetAlert.fire(
                Json.encodeToDynamic(
                    mapOf(
                        "title" to "Error",
                        "text" to "Unable to delete",
                        "icon" to "error"
                    )
                )
            )
            deleteObserver.setState(true)
        }
    }

    init {
        div(className = "table-responsive") {
            table(className = "table table-hover my-0") {
                thead {
                    tr {
                        th {
                            content = "#"
                        }
                        th {
                            content = "Client ID"
                        }
                        th {
                            content = "Resource IDs"
                        }
                        th {
                            content = "Client Authentication Method"
                        }
                        th {
                            content = "Authentication Grant Types"
                        }
                        th {
                            content = "Actions"
                        }
                    }
                }

                fun tableBody(): Tbody {
                    return tbody {
                        ClientService.getAll().then {
                            it.data.forEachIndexed {  index, client ->
                                tr {
                                    td {
                                        content = client.id
                                    }
                                    td {
                                        content = client.clientId
                                    }
                                    td {
                                        content = client.resourceIds
                                    }
                                    td {
                                        content = client.clientAuthenticationMethod
                                    }
                                    td {
                                        content = client.authorizedGrantTypes?.joinToString(",")
                                    }
                                    td {
                                        link(
                                            "",
                                            AppEngine.APP_DASHBOARD_CLIENT_UPDATE_ROUTE(
                                                client.id!!
                                            ),
                                            dataNavigo = true
                                        ) {
                                            span(className = "feather-sm me-1") {
                                                setAttribute("data-feather", "edit")
                                            }
                                            + " Edit"
                                        }
                                        span {
                                            content = " | "
                                        }
                                        span {
                                            color = Color.name(
                                                Col.RED
                                            )
                                            cursor = Cursor.POINTER
                                            onClick {
                                                delete(client.id)
                                                hide()
                                            }
                                            span(className = "feather-sm me-1") {
                                                setAttribute("data-feather", "trash")
                                            }
                                            + "Delete"
                                        }
                                    }
                                }
                            }
                        }.then {
                            window.setTimeout({
                                window["feather"].replace()
                            }, 100)

                            SpaAppEngine.routing.updatePageLinks()
                        }
                    }
                }
                add(tableBody())

                deleteObserver.subscribe {
                    if(it !=null && it) {
                        removeAt(1)
                        add(tableBody())
                        window.setTimeout({
                            window["feather"].replace()
                        }, 100)
                    }
                }
            }
        }
    }
}
