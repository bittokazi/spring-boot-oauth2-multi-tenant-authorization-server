package com.bittokazi.oauth2.auth.frontend.frontend.base.components.pagination

import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.ul

fun Container.paginationComponent(
    prevPageLink: Int? = null,
    nextPageLink: Int? = null,
    currentPageNumber: Int,
    totalPages: Int,
    totalCount: Int,
    count: Int,
    click: (page: Int) -> Unit
): Container {
    return div {
        div(className = "col-md-6") {
            ul(className = "pagination") {
                li(className = "page-item") {
                    link(className = "page-link ${prevPageLink?.let { "" } ?: "disabled"}", label = "Previous") {
                        onClick {
                            it.preventDefault()
                            click(prevPageLink!!)
                        }
                    }
                }

                getFiveValues(
                    totalRange = totalPages,
                    currentValue = currentPageNumber
                ).forEach { page ->

                    li(className = "page-item") {

                        val currentClass = when (currentPageNumber)  {
                            page -> "active"
                            else -> ""
                        }

                        link(className = "page-link $currentClass", label = page.toString(), dataNavigo = true) {
                            onClick {
                                it.preventDefault()
                                click(page)
                            }
                        }
                    }
                }


                li(className = "page-item") {
                    link(className = "page-link ${nextPageLink?.let { "" } ?: "disabled"}", label = "Next") {
                        onClick {
                            it.preventDefault()
                            click(nextPageLink!!)
                        }
                    }
                }
            }
        }
        div(className = "col-md-6 text-end") {
            content = "Total $totalCount records"
        }
    }
}

fun getFiveValues(totalRange: Int, currentValue: Int): IntRange {
    require(totalRange > 0) { "Total range must be greater than 0" }
    require(currentValue in 1..totalRange) { "Current value must be within the total range (1 to $totalRange)" }

    val start = when {
        currentValue <= 3 -> 1
        else -> currentValue - 2
    }

    val end = when {
        start + 4 <= totalRange -> start + 4
        else -> totalRange
    }

    return start..end
}
