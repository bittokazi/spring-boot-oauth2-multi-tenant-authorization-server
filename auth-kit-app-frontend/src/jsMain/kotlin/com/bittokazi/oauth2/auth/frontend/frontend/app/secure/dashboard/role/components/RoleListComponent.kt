package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.RoleService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import io.kvision.core.Container
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.table
import io.kvision.html.tbody
import io.kvision.html.td
import io.kvision.html.th
import io.kvision.html.thead
import io.kvision.html.tr

fun Container.roleListComponent(): Container {
    return table(className = "table table-hover my-0") {
        thead {
            tr {
                th {
                    content = "#"
                }
                th {
                    content = "Title"
                }
                th {
                    content = "Name"
                }
                th {
                    content = "Actions"
                }
            }
        }
        tbody {
            RoleService.getAll().then {
                it.data.roles.forEachIndexed {  index, role ->
                    tr {
                        td {
                            content = "${index + 1}"
                        }
                        td {
                            content = role.title
                        }
                        td {
                            content = role.name
                        }
                        td {
                            link(
                                "",
                                AppEngine.APP_DASHBOARD_ROLE_UPDATE_ROUTE(
                                    role.id!!
                                ),
                                dataNavigo = true
                            ) {
                                span(className = "feather-sm me-1") {
                                    setAttribute("data-feather", "edit")
                                }
                                + " Edit"
                            }
                        }
                    }
                }
            }.then {
                AppEngine.routing.updatePageLinks()
            }
        }
    }
}
