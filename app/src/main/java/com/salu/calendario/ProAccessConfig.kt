package com.salu.calendario

object ProAccessConfig {
    /**
     * Flag de teste local para simular acesso PRO manualmente.
     */
    var isProUnlockedManual: Boolean = false

    val isProUnlocked: Boolean
        get() = isProUnlockedManual || BuildConfig.PRO_GOD_MODE

    fun canUseTheme(theme: ThemeItem): Boolean {
        return !theme.isPro || isProUnlocked
    }

    /**
     * O upload e personalização de logomarca fica liberado para testes e uso no desenvolvimento.
     */
    fun canUploadLogo(): Boolean {
        return true
    }
}
