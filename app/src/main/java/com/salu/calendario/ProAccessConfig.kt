package com.salu.calendario

object ProAccessConfig {
    /**
     * Flag de teste local para simular acesso PRO.
     * Inicia como false para validar o bloqueio transparente das molduras PRO.
     */
    var isProUnlocked: Boolean = false

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
