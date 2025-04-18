package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.Container
import io.kvision.core.Cursor
import io.kvision.core.onClick
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.table
import io.kvision.html.tbody
import io.kvision.html.td
import io.kvision.html.th
import io.kvision.html.thead
import io.kvision.html.tr

fun Container.tenantListComponent(): Container {
    return table(className = "table table-hover my-0") {
        thead {
            tr {
                th {
                    content = "#"
                }
                th {
                    content = "Name"
                }
                th {
                    content = "key"
                }
                th {
                    content = "Domain"
                }
                th {
                    content = "Enabled"
                }
                th {
                    content = "Actions"
                }
            }
        }
        tbody {
            TenantService.getAll().then {
                it.data.forEachIndexed {  index, company ->
                    tr {
                        td {
                            content = "${index + 1}"
                        }
                        td {
                            content = company.name
                        }
                        td {
                            content = company.companyKey
                        }
                        td {
                            content = company.domain
                        }
                        td {
                            content = company.enabled.toString()
                        }
                        td {
                            link(
                                "",
                                AppEngine.APP_DASHBOARD_TENANT_UPDATE_ROUTE(
                                    company.id!!
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
                                    Col.BLUE
                                )
                                cursor = Cursor.POINTER
                                onClick {
                                    AppEngine.authService.switchTenant(company.companyKey)
                                }

                                span(className = "feather-sm me-1") {
                                    setAttribute("data-feather", "log-in")
                                }
                                + " Switch Tenant"
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
