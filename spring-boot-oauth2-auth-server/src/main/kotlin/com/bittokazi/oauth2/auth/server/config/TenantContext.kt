package com.bittokazi.oauth2.auth.server.config

object TenantContext {
    private val currentTenant = ThreadLocal<String?>()

    private val currentClient = ThreadLocal<String?>()

    private val currentDataTenant = ThreadLocal<String?>()

    private val currentIssuer = ThreadLocal<String?>()

    @JvmStatic
    fun setCurrentTenant(tenant: String?) {
        currentTenant.set(tenant)
    }

    @JvmStatic
    fun getCurrentTenant(): String? {
        return currentTenant.get()
    }

    fun clear() {
        currentTenant.set(null)
    }

    fun setCurrentClient(client: String?) {
        currentClient.set(client)
    }

    fun getCurrentClient(): String? {
        return currentClient.get()
    }

    fun clearClient() {
        currentClient.set(null)
    }

    @JvmStatic
    fun setCurrentIssuer(client: String?) {
        currentIssuer.set(client)
    }

    fun getCurrentIssuer(): String? {
        return currentIssuer.get()
    }

    fun clearCurrentIssuer() {
        currentIssuer.set(null)
    }

    @JvmStatic
    fun setCurrentDataTenant(client: String?) {
        currentDataTenant.set(client)
    }

    @JvmStatic
    fun getCurrentDataTenant(): String? {
        return currentDataTenant.get()
    }

    fun clearCurrentDataTenant() {
        currentDataTenant.set(null)
    }
}
