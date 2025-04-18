package com.bittokazi.oauth2.auth.frontend.frontend.base.common

object ObservableManager {
    var holder: MutableMap<String, () -> Unit> = mutableMapOf()

    fun setSubscriber(key: String, block: () -> () -> Unit) {
        if(holder.containsKey(key)) {
            holder[key]?.invoke()
        }
        holder[key] = block.invoke()
    }
}
