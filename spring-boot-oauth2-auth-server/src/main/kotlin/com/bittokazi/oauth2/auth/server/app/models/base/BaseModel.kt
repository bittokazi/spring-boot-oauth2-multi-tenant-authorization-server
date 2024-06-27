package com.bittokazi.oauth2.auth.server.app.models.base

import jakarta.persistence.*
import java.util.*

/**
 * @author bitto kazi
 */

@MappedSuperclass
open class BaseModel {
    @Column(name = "created_date", insertable = true, updatable = false)
    var createdDate: Date? = null

    @Column(name = "updated_date", insertable = true, updatable = true)
    var updatedDate: Date? = null

    @Column(name = "created_by")
    var createdBy: String? = null

    @Column(name = "updated_by")
    var updatedBy: String? = null

    @Column(name = "deleted_at")
    var deletedAt: Date? = null

    @PrePersist
    fun createdAt() {
        this.updatedDate = Date()
        this.createdDate = this.updatedDate
    }

    @PreUpdate
    fun updatedAt() {
        this.updatedDate = Date()
    }
}

