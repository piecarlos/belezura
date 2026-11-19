# Diretrizes para Agentes de IA e Desenvolvedores

Este repositório contém o código de produção do **Agenda Salú v2** (Android nativo em Jetpack Compose).

## 1. Leitura Prévia Obrigatória
- Antes de inspecionar ou alterar qualquer código, **leia integralmente o arquivo [HANDOFF.md](HANDOFF.md)**.
- O `HANDOFF.md` descreve a arquitetura, pilha de tecnologias, mapeamento de arquivos e o schema de dados.

## 2. Invariantes Críticos (Inegociáveis)
- **Proibido alterar a resolução do canvas:** O bitmap de exportação deve ser exatamente `1080 x 1920` px (`CalendarRenderer.kt`).
- **Proibido dependências pesadas:** Não adicione Room, SQLite, Hilt/Koin, ou bibliotecas de navegação externa sem aprovação expressa.
- **100% Offline e Privado:** Não adicione `INTERNET`, Analytics, crash reporters ou SDKs de terceiros.
- **Preservação de SharedPreferences e Schema:** O schema JSON de backup e as chaves de preferências (`salu_calendar_prefs`, `month_data_YYYY_MM`, `layout_config`) devem manter compatibilidade retroativa estrita.
- **Proteção do R8/Shrinker:** Os recursos de fontes (`@font/*`) e fundos (`@drawable/bg_base`) devem permanecer listados em `res/raw/keep.xml` e referenciados via `R.*`.

## 3. Regra de Cirurgia de Código
- **Nunca reescreva módulos funcionais do zero.** Faça apenas edições pontuais e cirúrgicas no escopo solicitado.
- Preserve os componentes estáveis: `CalendarEngine.kt`, `ExportHelper.kt`, `BackgroundManager.kt`, `FontManager.kt`.
- Em Compose, preserve a separação limpa entre a tela Home (`HomeScreen.kt`), Editor (`MainActivity.kt`) e painéis de ajuste.

## 4. Validação Pós-Alteração
- Após qualquer modificação em código Kotlin ou recursos, execute obrigatoriamente:
  `./gradlew testDebugUnitTest assembleRelease`
- Confirme que o build termina com `BUILD SUCCESSFUL` sem quebras de compilação ou conflitos de shrinker.

## 5. Segurança e Sigilo
- **Nunca** comite arquivos com credenciais (`keystore.properties`, senhas, tokens).
- **Nunca** inclua arquivos de keystore (`*.jks`, `*.keystore`) nem backups no repositório Git.
- **Nunca** escreva caminhos absolutos locais contendo nomes de usuários da máquina host em documentações públicas.
