# HANDOFF TÉCNICO — BELEZURA (AGENDA & MARKETING VISUAL)

> **Documento de continuidade para desenvolvedores e agentes de IA** (Copilot, Claude, Gemini, Cursor, OpenCode).  
> Leia atentamente este documento antes de realizar qualquer alteração no código.

---

## 1. Visão Geral Técnica

Aplicativo Android nativo desenvolvido em **Kotlin** com interface declarativa em **Jetpack Compose (Material 3)**.  
O objetivo principal é gerar, editar interativamente e exportar peças visuais no formato vertical **9:16 (`1080×1920` px)** para divulgação em redes sociais (WhatsApp Status, Instagram Stories e envio direto a clientes), atendendo profissionais autônomas do mercado de beleza (manicures, nail designers, lash designers, esteticistas, maquiadoras).

- **Identidade e Nome Oficial:** **Belezura** (nascido da base tecnológica *Agenda Salú*).
- **Filosofia de Arquitetura:** 100% offline na base atual, sem dependências externas desnecessárias, sem permissão de `INTERNET`, com tempo de inicialização instantâneo e persistência leve via `SharedPreferences` + `org.json`.
- **Renderização WYSIWYG Estrita:** O que é visualizado na tela interativa de edição é matematicamente idêntico ao PNG exportado em 1080×1920.

---

## 2. Ambiente de Desenvolvimento e Build

### 2.1 Especificações do Projeto
| Item | Versão / Configuração |
|---|---|
| **JDK** | Java 17 (`sourceCompatibility = JavaVersion.VERSION_17`, `jvmTarget = "17"`) |
| **Gradle** | 8.13 (Gradle Wrapper) |
| **Android Gradle Plugin (AGP)** | 8.7.3 |
| **Kotlin** | 2.0.21 (`org.jetbrains.kotlin.android`, `org.jetbrains.kotlin.plugin.compose`) |
| **Compose BOM** | `2024.12.01` |
| **compileSdk** | 35 |
| **minSdk** | 26 (Android 8.0 Oreo) |
| **targetSdk** | 35 (Android 15) |
| **applicationId** | `com.salu.calendario.v2` |
| **namespace** | `com.salu.calendario` |
| **versionCode / versionName** | `2` / `"2.0.0"` |
| **Otimizador / Ofuscador** | R8 (`isMinifyEnabled = true`, `isShrinkResources = true`) |

### 2.2 Keystore e Assinatura Release
Por segurança, a chave criptográfica permanente (`salu-release.jks`) e o arquivo de credenciais (`keystore.properties`) residem **fora do repositório Git** (`<pasta-externa-keystore>/salu-release.jks`).  
O `app/build.gradle.kts` lê dinamicamente `rootProject.file("keystore.properties")`, se presente, para assinar builds Release. Caso o arquivo não exista localmente, o build Debug compila normalmente com a chave padrão do Android SDK.

### 2.3 Comandos Prontos de Terminal
```powershell
# 1. Executar bateria de testes unitários JVM
.\gradlew testDebugUnitTest

# 2. Compilar APK Debug
.\gradlew assembleDebug

# 3. Compilar APK Release otimizado com R8
.\gradlew assembleRelease

# 4. Compilar APK God Mode (PRO 100% desbloqueado para testes manuais)
.\gradlew assembleGodmode

# 5. Instalar APK Release no emulador ou dispositivo via ADB
adb -e install -r app\build\outputs\apk\release\app-release.apk

# 6. Instalar APK God Mode no emulador via ADB (instala lado a lado)
adb -e install -r app\build\outputs\apk\godmode\app-godmode.apk

# 7. Iniciar app via ADB (Release)
adb -e shell am start -n com.salu.calendario.v2/com.salu.calendario.MainActivity

# 8. Iniciar app via ADB (God Mode)
adb -e shell am start -n com.salu.calendario.v2.godmode/com.salu.calendario.MainActivity
```

---

## 3. Arquitetura e Mapa de Arquivos

```
app/src/main/
├── AndroidManifest.xml
├── java/com/salu/calendario/
│   ├── MainActivity.kt          # Activity raiz, viewModels, launchers SAF, navegação entre telas e hosts
│   ├── HomeScreen.kt            # Tela inicial / Hub, card do mês ativo, busca de clientes e atalhos
│   ├── EditorScreen.kt          # Tela principal do editor (TopAppBar, preview Canvas 9:16 com gestos multi-touch)
│   ├── PromoEditorScreen.kt     # Editor interativo de Stories 9:16 (chips de templates, preview e edição de textos)
│   ├── LayoutAdjustPanel.kt     # Painel "Ajustar Layout e Cores" (abas, sub-abas, sliders, cor e logo)
│   ├── ThemeGallerySheet.kt     # Galeria de molduras/temas com radar sazonal, miniaturas reais e filtros
│   ├── LogoSettingsDialog.kt    # Diálogo de configuração, visualização e modos da logomarca
│   ├── ProPreviewDialog.kt      # Modal de transparência e preview de modelos exclusivos PRO
│   ├── DayEditSheet.kt          # ModalBottomSheet de edição do dia (nomes + toggle "X" vermelho)
│   ├── EditorDialogs.kt         # Diálogos auxiliares (MonthYearPicker, confirmação de restauração)
│   ├── CustomBackgroundDialog.kt# Modal de orientação prévia e seleção de imagem para moldura
│   ├── CalendarViewModel.kt     # State holder reativo (StateFlow), undo/redo, temas, logo, promoções e ações
│   ├── CalendarRenderer.kt      # Motor gráfico WYSIWYG Canvas 1080×1920 (preview, temas, logo e exportação)
│   ├── PromoRenderer.kt         # Motor gráfico WYSIWYG Canvas 1080×1920 para Stories de Promoção/Avisos
│   ├── CalendarEngine.kt        # Cálculos de calendário, deslocamentos, nomes pt-BR e grid
│   ├── CalendarModels.kt        # Modelos imutáveis (CellData, EditableElement, AppScreen, etc.)
│   ├── PromoModels.kt           # Modelos de Stories/Promoções (PromoTemplateType, PromoDraft, presets e JSON)
│   ├── LayoutConfig.kt          # Configuração geométrica e visual com serialização/deserialização JSON
│   ├── SeasonalRadar.kt         # Calendário comercial sazonal offline brasileiro (dias restantes e dicas)
│   ├── ThemeModels.kt           # Modelos de tema, categorias, paletas de harmonização e contrato
│   ├── ThemeCatalog.kt          # Catálogo procedural e embutido de temas (Make Glam, Floral, Minimal, Luxo + PRO)
│   ├── ProAccessConfig.kt       # Controle de acesso e transparência das funcionalidades PRO
│   ├── LogoManager.kt           # Gestor de armazenamento, escala 600x600 e decodificação da logomarca
│   ├── BackupManager.kt         # Exportação/importação de backup (.json) SAF
│   ├── BackgroundManager.kt     # Decodificador ImageDecoder/BitmapFactory e zoom central 1080×1920
│   ├── FontManager.kt           # Gerenciador e cache de Typefaces embutidas
│   └── ExportHelper.kt          # Renderização offscreen 1080×1920, salvamento MediaStore e FileProvider
└── res/
    ├── drawable/ic_launcher_background.xml # Fundo vinho sólido (#5B0224) para Adaptive Icon
    ├── drawable-nodpi/
    │   ├── bg_base.webp         # Fundo floral clássico limpo sem logo em 1080×1920 (~47 KB)
    │   ├── bg_make_glam.webp    # Fundo Make Glam Luxo em 1080×1920 (~150 KB)
    │   └── bg_make_glam_2.webp  # Fundo Make Glam Floral em 1080×1920 (~140 KB)
    ├── font/                    # Fontes TrueType estáticas embutidas
    │   ├── cinzel_bold.ttf
    │   ├── fredoka_bold.ttf
    │   └── playfair_display_bold.ttf
    ├── mipmap-anydpi-v26/       # Definição do Adaptive Icon (ic_launcher.xml e ic_launcher_round.xml)
    ├── mipmap-{m,h,xh,xxh,xxxh}dpi/ # Assets WebP de ic_launcher, ic_launcher_round e ic_launcher_foreground
    ├── raw/keep.xml             # Blindagem contra remoção indevida pelo R8 shrinker
    └── xml/file_paths.xml       # Configuração de caminhos do FileProvider para compartilhamento
```

### Detalhamento dos Componentes

| Arquivo | Responsabilidade Exata |
|---|---|
| **`MainActivity.kt`** | Ponto de entrada (`ComponentActivity`). Enxuto (~325 linhas), gerencia launchers de arquivos SAF (`CreateDocument`, `OpenDocument`, `PickVisualMedia`, permissões), navegação entre `HomeScreen` e `EditorScreen`, `BackHandler` e host de Snackbar. |
| **`HomeScreen.kt`** | Painel inicial rápido / Hub. Hero card com contadores dinâmicos de agendamentos e dias com "X", botão rápido para compartilhar PNG sem abrir editor, busca global de clientes, carrossel "Meus Meses" (`LazyRow`) e atalhos de "Aparência e Backup" (com acionamento da `ThemeGallerySheet`). |
| **`EditorScreen.kt`** | Tela principal do editor. Contém o `Scaffold`, `TopAppBar` (Undo/Redo, navegação de mês, botão de ajuste, atalho de moldura e menu de backup), `CalendarCanvasView` com gestos multi-touch (arraste e pinça sincronizados para logo e recado) e toque direto na logomarca ou no recado para abrir a edição. |
| **`LayoutAdjustPanel.kt`** | Painel modular de ajuste visual (zero rolagem). Cinco abas (`Mês`, `Ano`, `Calendário`, `Logo`, `Recado`), sub-abas contextuais (`Posição`, `Fonte`, `Cor`, `Estilo`, `Recado`), atalhos de posição da logo (`Topo Esq`, `Topo Dir`, `Rodapé`), 4 frases prontas de salão, sliders de tamanho, botões de ajuste fino (`← ↑ ↓ →`) e `Centralizar X`. |
| **`ThemeGallerySheet.kt`** | Galeria modal de temas com banner dinâmico do Radar Sazonal (`SeasonalRadar`), miniaturas reais geradas para temas embutidos (`bg_make_glam`, `bg_make_glam_2`, `bg_base`), filtros de categoria e cards em proporção 9:16 com badge PRO e borda ativa. |
| **`LogoSettingsDialog.kt`** | Modal dedicado de configuração da logomarca: prévia em tempo real, envio de arquivo, seleção de modos (`Original`, `Sem fundo branco`, `Selo redondo`), ativação no calendário e remoção. |
| **`ProPreviewDialog.kt`** | Diálogo de transparência radical para modelos exclusivos PRO: exibe badge de diamante e explica de forma honesta a proposta de assinatura futura sem travas inesperadas na exportação. |
| **`DayEditSheet.kt`** | `ModalBottomSheet` de edição do dia. Permite alternar o "X" vermelho de dia indisponível, adicionar/remover clientes, reordenar prioridades e autocompletar. |
| **`EditorDialogs.kt`** | Diálogos modais do editor: `MonthYearPickerDialog` (seletor rápido de mês/ano) e `ConfirmRestoreDialog` (confirmação atômica de restauração). |
| **`CustomBackgroundDialog.kt`** | Modal de orientação prévia antes do upload da moldura (recomenda formato 9:16 vertical e explica ajuste central automático). |
| **`CalendarViewModel.kt`** | Gerenciamento de estado (`CalendarUiState`), pilhas de Undo/Redo com snapshots de células, motor de busca sem acento (`ClientSearchHelper`), navegação de meses, ajuste fino de layout com arraste 2D e pinça (incluindo `LOGO` e `FOOTER_MESSAGE`), atalhos de posicionamento, catálogo de temas e logo. |
| **`CalendarRenderer.kt`** | Motor gráfico Canvas. Desenha fundo (temas procedurais do `ThemeCatalog` ou fundos customizados/embutidos), logomarca livre em qualquer posição com escala/offset e bounding box para toque direto, mensagem / recado de rodapé (CTA) com tipografia Fredoka Bold e contraste automático por luminância, texto do mês, ano 3D com 4 camadas de relevo/sombra e swoosh, cartão e grade do calendário, letras dos dias da semana, numerais, "X" vermelho centralizado, algoritmo de auto-fit de nomes em 2 passos e caixas tracejadas em modo de edição. |
| **`CalendarEngine.kt`** | Lógica de datas baseada em `java.time.YearMonth`. Mapeamento de domingo como início de semana (padrão brasileiro `value % 7`), contagem dinâmica de semanas (5 ou 6 linhas) e geração das células do grid. |
| **`CalendarModels.kt`** | Definições de `CellData` (slot, dia, hasRedX, lista de nomes), `MonthData`, `EditableElement` (`NONE`, `MONTH`, `YEAR`, `GRID_CARD`, `LOGO`, `FOOTER_MESSAGE`), `AppScreen` (`HOME`, `CALENDAR`), `MonthSummary` e `ClientSearchResult`. |
| **`LayoutConfig.kt`** | Armazena escalas, cores hexadecimais, fontes, coordenadas e configurações de tema/logomarca/recado (`selectedThemeId`, `logoEnabled`, `logoMode`, `logoScale`, `logoOffsetX`, `logoOffsetY`, `footerMessage`, `showFooterMessage`, `footerScale`, `footerOffsetX`, `footerOffsetY`, `footerColor`). Garante total compatibilidade retroativa com backups legados. |
| **`SeasonalRadar.kt`** | Calendário comercial sazonal offline brasileiro (Verão, Carnaval, Dia da Mulher, Dia das Mães, Dia dos Namorados, São João, Dia da Cliente, Outubro Rosa, Black Friday, Festas de Fim de Ano). Calcula dias restantes e dicas comerciais. |
| **`ThemeModels.kt`** | Modelos imutáveis do catálogo de temas (`ThemeCategory`, `ThemeSourceType`, `ThemeItem`, `ThemePalette`) e interface `ThemeRepository`. |
| **`ThemeCatalog.kt`** | Catálogo offline com temas embutidos (`make_glam_luxo`, `make_glam_floral`, `floral_default`, `minimal_cream`, `rose_editorial`, `luxury_gold` + temas de demonstração PRO), paletas harmônicas automáticas e escalas recomendadas (`defaultCalendarScale`). |
| **`ProAccessConfig.kt`** | Configuração de acesso a funcionalidades PRO com transparência total. |
| **`LogoManager.kt`** | Gerenciamento de arquivo e cache de bitmap da logomarca em `filesDir/user_logo.png`. Decodificação segura (ImageDecoder/BitmapFactory), limite proporcional de até 600×600 px e suporte a transparência PNG. |
| **`BackupManager.kt`** | Gera e consome backups universais em JSON estruturado (`SCHEMA_VERSION = 1`). Limpa e restaura atomicamente o `SharedPreferences` preservando integridade referencial. Formata data do último backup (`last_backup_at`). |
| **`BackgroundManager.kt`** | Decodifica `res/drawable-nodpi/` com `inScaled = false` para manter a escala 1:1. Processa fundos customizados com `ImageDecoder` (software allocator) e fallback `BitmapFactory` com corte central (*center-crop* 1080×1920). |
| **`FontManager.kt`** | Cache de fontes embutidas (`CINZEL_BOLD`, `FREDOKA_BOLD`, `PLAYFAIR_DISPLAY_BOLD`, `DEFAULT`). Usa referências diretas `R.font.*` para proteção total contra o R8. |
| **`ExportHelper.kt`** | Renderiza a imagem em um `Bitmap` ARGB_8888 de `1080×1920`, salva na galeria através do `MediaStore` (com verificação de permissões para API ≤ 28) e compartilha via `Intent.ACTION_SEND` e `FileProvider`. |

---

## 4. Invariantes Críticos ("NÃO QUEBRAR")

Ao realizar qualquer alteração futura, respeite obrigatoriamente as seguintes diretrizes:

1. **Coordenadas Virtuais Fixas (`1080f × 1920f`):**
   - Toda a geometria do calendário é desenhada dentro do espaço virtual `VIRTUAL_WIDTH = 1080f` e `VIRTUAL_HEIGHT = 1920f`.
   - O preview Compose escala a tela virtual via `nativeCanvas.scale(scaleX, scaleY)` mantendo proporção 9:16 travada. **Nunca** use valores absolutos de densidade de tela (`dp` do Compose) dentro do `CalendarRenderer`.
2. **Auto-fit de Nomes em Dois Passos:**
   - **Passo 1 (Vertical):** O tamanho-base da fonte reduz proporcionalmente à quantidade de nomes na célula para que todos caibam na altura disponível sem invadir bordas.
   - **Passo 2 (Horizontal):** Cada linha é medida individualmente (`measureText`). Nomes longos sofrem redução individual para nunca ultrapassarem a largura útil da célula.
3. **Marcação com "X" Vermelho:**
   - Desenhado com proporção quadrada (`min(w, h) * 0.70f`), centralizado na célula e posicionado **atrás dos nomes** (`drawRedX` antes de `drawNames`) para manter a legibilidade dos textos caso ambos existam.
4. **Sincronização do Toque com Transformações do Grid e da Logo:**
   - Ao tocar no preview, o cálculo de conversão `Offset` → célula considera `cardOffsetX`, `cardOffsetY` e `cardScale`. Modificações na escala ou translação do cartão refletem imediatamente na função `getCellAt`.
   - Toques sobre a logomarca no rodapé disparam a seleção e abertura do painel focado em `EditableElement.LOGO`.
5. **Insets e Barras do Sistema:**
   - Todas as barras superiores, inferiores e o painel de ajuste devem conter `statusBarsPadding()` e `navigationBarsPadding()`. Nunca remova esses insets para não causar sobreposição com os botões virtuais do Android (gestos/3 botões).
6. **Proteção R8 e Shrinkage de Recursos:**
   - As fontes e os fundos base são protegidos pelo arquivo `res/raw/keep.xml` (`tools:keep="@font/*,@drawable/bg_base,@drawable/bg_make_glam,@drawable/bg_make_glam_2"`).
   - `FontManager` e `BackgroundManager` devem continuar referenciando recursos diretamente via identificadores estáticos gerados (`R.font.*` e `R.drawable.*`), evitando busca reflexiva por strings (`getIdentifier`).
7. **Autoridade do FileProvider Dinâmica:**
   - O `AndroidManifest.xml` utiliza `android:authorities="${applicationId}.fileprovider"`.
   - O `ExportHelper.kt` deve invocar `FileProvider.getUriForFile` com `"${context.packageName}.fileprovider"`.
8. **Decodificação de Bitmaps em 1:1:**
   - Bitmaps embutidos (em `drawable-nodpi/`) devem ser decodificados com `BitmapFactory.Options().apply { inScaled = false }` ou `ImageDecoder` para evitar upscaling automático por densidade de tela (`xxxhdpi`), preservando a resolução nativa de `1080×1920`.
9. **Zero Dependências Pesadas e Zero Permissão de Internet:**
   - Não adicione Room, SQLite, Ktor ou Retrofit. Mantenha persistência leve via `SharedPreferences`. Não solicite `android.permission.INTERNET` enquanto o aplicativo operar em modo local.

---

## 5. Esquema de Dados e Persistência

### 5.1 SharedPreferences (`salu_calendar_prefs`)
Os dados são armazenados de forma atômica no arquivo privado `salu_calendar_prefs.xml`:

| Chave | Tipo | Descrição |
|---|---|---|
| **`layout_config`** | `String (JSON)` | Serialização do objeto `LayoutConfig` (escalas, fontes, cores, coordenadas X/Y e parâmetros da logomarca). |
| **`month_data_YYYY_MM`** | `String (JSON)` | Dados de agendamento de um mês específico (ex: `month_data_2026_12`). |
| **`last_backup_at`** | `Long` | Timestamp em milissegundos do último backup exportado. |

### 5.2 Estrutura do JSON do Mês (`month_data_YYYY_MM`)
```json
{
  "slots": [
    {
      "slot": 2,
      "day": 1,
      "hasRedX": false,
      "names": [
        "Jessica",
        "Fabiana"
      ]
    },
    {
      "slot": 3,
      "day": 2,
      "hasRedX": true,
      "names": []
    }
  ]
}
```

### 5.3 Estrutura do Backup Universal (`BackupManager`)
- **Versão do Schema:** `schemaVersion = 1`
```json
{
  "schemaVersion": 1,
  "timestamp": 1732047000000,
  "layoutConfig": {
    "monthX": 540.0,
    "monthY": 115.0,
    "monthOffsetX": 0.0,
    "monthOffsetY": 0.0,
    "monthScale": 1.0,
    "monthFont": "CINZEL_BOLD",
    "monthColor": "#FF5E2326",
    "yearX": 540.0,
    "yearY": 225.0,
    "yearOffsetX": 0.0,
    "yearOffsetY": 0.0,
    "yearScale": 1.0,
    "yearFont": "FREDOKA_BOLD",
    "yearColor": "#FFE64886",
    "cardLeft": 20.0,
    "cardRight": 1060.0,
    "cardTop": 278.0,
    "cardBottom": 1612.0,
    "cardScale": 1.0,
    "cardOffsetX": 0.0,
    "cardOffsetY": 0.0,
    "cardAccentColor": "#FFE89BAE",
    "selectedThemeId": "floral_default",
    "logoEnabled": false,
    "logoMode": "ORIGINAL",
    "logoScale": 1.0,
    "logoOffsetX": 0.0,
    "logoOffsetY": 0.0,
    "footerMessage": "",
    "showFooterMessage": false,
    "footerScale": 1.0,
    "footerOffsetX": 0.0,
    "footerOffsetY": 0.0,
    "footerColor": "#00000000"
  },
  "months": {
    "month_data_2026_12": {
      "slots": [ ... ]
    }
  },
  "promoDraft": {
    "templateType": "AGENDA_ABERTA",
    "badgeText": "ÚLTIMOS HORÁRIOS",
    "title": "Agenda Aberta",
    "subtitle": "Garanta seu momento de autocuidado esta semana ✨",
    "items": [ ... ],
    "footerCta": "Agende seu horário pelo WhatsApp!",
    "showLogo": true
  }
}
```

### 5.4 Schema JSON de Rascunho de Promoções (`promo_draft_json`)
Armazenado em `SharedPreferences` (`salu_calendar_prefs`) sob a chave `"promo_draft_json"`:
```json
{
  "templateType": "AGENDA_ABERTA",
  "badgeText": "ÚLTIMOS HORÁRIOS",
  "title": "Agenda Aberta",
  "subtitle": "Garanta seu momento de autocuidado esta semana ✨",
  "items": [
    { "label": "Quinta-feira", "value": "14:00 • 16:30" },
    { "label": "Sexta-feira", "value": "10:00 • 15:00" },
    { "label": "Sábado", "value": "09:30 • 11:00 • 14:00" }
  ],
  "footerCta": "Agende seu horário pelo WhatsApp!",
  "showLogo": true
}
```

> **Regra Obrigatória:** Todas as leituras de JSON devem usar `optDouble`, `optInt`, `optBoolean` e `optString` com valores padrão de fallback, garantindo que o app nunca quebre ao carregar dados salvos em versões anteriores.

---

## 6. Status Atual das Funcionalidades e Validação

- **Fase A (100% Concluída e Validada):**
  - **Identidade & Ícone:** Transição visual para **Belezura** com Adaptive Icon 3D em alta resolução em todas as densidades `mipmap-*` com fundo sólido `#5B0224`.
  - **Build God Mode:** 3º buildType `godmode` (`Belezura GOD`, `applicationIdSuffix = ".godmode"`, `PRO_GOD_MODE = true`) instalando lado a lado para testes manuais da galeria PRO sem afetar `debug`/`release`.
  - **Galeria de Molduras Integrada (`ThemeGallerySheet`):** Temas procedurais e fotográficos embutidos (`Make Glam Luxo` e `Make Glam Floral`), miniaturas reais geradas, paleta automática (`ThemePalette`) e escala adaptada da grade (`defaultCalendarScale = 0.88f`).
  - **Logomarca Livre em Todo o Canvas:** Diálogo de configuração (`LogoSettingsDialog`), 3 modos visuais (Original, Sem fundo branco, Selo redondo), toque direto na logo no canvas para abrir o painel de ajuste, gestos multi-touch (pinça e arraste livre com escala de 50% a 220%), limites seguros para não perder a logo e atalhos rápidos de posição (`Topo Esq`, `Topo Dir`, `Rodapé`).
  - **Recado / CTA Personalizado no Rodapé:** Mensagem de até 2 linhas com quebra automática, tipografia Fredoka Bold, contraste inteligente por luminância (dourado `#FFE082` em temas escuros e vinho nos claros), toque direto na frase no canvas para editar, e chip `Recado` no painel com 4 frases prontas de salão, slider de escala, ajuste fino e seletor de cor.
- **Fase B — Sprint B.1 (Entregue):**
  - **Identidade no Cabeçalho:** Atualizado para "Belezura" com subtítulo "Seu estúdio organizado e bonito".
  - **Hero Card com Métricas Completas:** Contadores de agendamentos, dias com "X" e dias livres no mês com ações de "Editar Agenda" e "Compartilhar PNG".
  - **Card do Radar Sazonal na Home:** Integração direta com `SeasonalRadar`, exibindo contagem regressiva, dica comercial e atalho direto para a galeria pré-filtrada em "Em Alta / Datas".
  - **Grade de Ações Rápidas (2x2):** Atalhos para "Agenda do Mês", "Criar Promoção" (badge "Em breve" + aviso informativo), "Molduras & Temas" e "Minha Logo".
  - **Preservação:** Busca global de clientes, carrossel "Meus Meses" e controles de backup integralmente preservados.
- **Fase B — Sprint B.2 (Entregue):**
  - **Módulo Editor de Stories de Promoções / Avisos 9:16 (`PromoEditorScreen`):** Tela dedicada de edição e exportação com seleção rápida entre 3 templates (`Vagas`, `Preços`, `Avisos`), preview WYSIWYG 1080×1920 com toque direto para abrir edição, e barra de ações com "Salvar PNG" e "Compartilhar".
  - **Motor Gráfico WYSIWYG 1080×1920 (`PromoRenderer`):** Cartão elegante translúcido com margens de respiro seguras (~110px horizontal, ~180px vertical) para molduras fotográficas, logotipo do estúdio integrado no topo, selo pílula superior, título refinado, divisor gradiente, corpo adaptado a cada template (cards de horários, tabela pontilhada de serviços/preços e tópicos com numeração/quebra de linha) e CTA de rodapé com contraste inteligente por luminância.
  - **Modelos e Presets pt-BR (`PromoModels`):** Presets realistas e humanizados de salão de beleza para Vagas, Tabela de Preços e Avisos do Estúdio, serialização segura em JSON com fallback e persistência em `SharedPreferences`.
  - **Exportação Integrada (`ExportHelper`):** Métodos de renderização offscreen 1080×1920, salvamento no MediaStore (`Belezura_Promo_*.png`) e compartilhamento via `FileProvider`.
  - **Micro-polimentos na Home:** Remoção do botão redundante no cabeçalho, substituição do ícone do card de Backup por escudo de segurança (`Icons.Outlined.Shield`) e ativação direta da ação "Criar Promoção" navegando para o editor.
- **Fase B — Sprint B.3 (Entregue):**
  - **Campanhas Sazonais Integradas no Radar (`SeasonalRadar` & `HomeScreen`):** Todas as 10 datas comerciais contam com presets prontos de promoções (`PromoDraft`), cópias persuasivas em pt-BR e botão "Criar Story da Data" direto no card do Radar na Home, navegando para o editor com o rascunho preenchido.
  - **Backup Universal Completo (`BackupManager`):** Exportação e importação preservam e restauram `"promoDraft"` com retrocompatibilidade total de JSON (backups legados sem esse campo continuam importando perfeitamente).
  - **Polimentos de Tipografia e Diagramação (`PromoRenderer`):** Fontes ampliadas para Stories 9:16 (título 62f, subtítulo 29f, itens 34f-38f, CTA 38f), centralização vertical calculada pela altura natural dos blocos de itens (eliminando vácuo em Vagas e espaçamento vertical excessivo em Preços e Avisos) e contraste adaptativo rico para temas escuros (`Color.argb(240, 22, 22, 24)` com bordas e destaques dourados/vibrantes).
  - **Usabilidade do Editor (`PromoEditorScreen`, `ThemeGallerySheet` & `MainActivity`):** Chips `Vagas`, `Preços`, `Avisos` 100% centralizados com `Surface`, campos em formato empilhado para Avisos e Vagas no bottom sheet, switch com thumb branco, proporção áurea 65%/35% para Tabela de Preços, subtítulo contextual na Galeria de Molduras ("Escolha o visual da sua arte") e Snackbar exibido acima da barra inferior.
- **Fase C — Sprint C.1 (Entregue):**
  - **Motor de Mensagens e Intent (`WhatsAppHelper.kt`):** Templates humanizados e acolhedores de salão em pt-BR (`CONFIRMACAO`, `LEMBRETE`, `POS_ATENDIMENTO`) com formatação inteligente de data e dia da semana (ex: `15/12 (terça-feira)`), cópia para área de transferência e disparo via Intent seguro com fallback em cascata (`com.whatsapp` -> `com.whatsapp.w4b` -> `Intent.createChooser`).
  - **Visibilidade de Pacotes (`AndroidManifest.xml`):** Declaração explícita de `<queries>` para `com.whatsapp` e `com.whatsapp.w4b` (compatibilidade Android 11+ sem requerer permissões).
  - **Modal de Envio Rápido (`WhatsAppMessageDialog.kt`):** `ModalBottomSheet` Material 3 com chips de alternância rápida de template, área de texto editável para personalização de horários/serviços, ação secundária "Copiar" com toast de confirmação e botão principal "Enviar no WhatsApp".
  - **Integração na UI (`DayEditSheet.kt` & `HomeScreen.kt`):** Botão compacto de mensagem em verde WhatsApp (`Icons.AutoMirrored.Filled.Chat`) integrado diretamente ao lado de cada cliente listada no dia e nos resultados da busca global na Home.
- **Suíte de Testes Automatizados:**
  - `WhatsAppHelperTest.kt`: Validação dos 3 templates de mensagem, formatação de dias da semana em pt-BR e higienização de nomes.
  - `PromoModelsTest.kt`: Validação dos 3 presets, round-trip completo de serialização JSON, tolerância a payloads corrompidos/vazios e limitação estrita de até 6 itens.
  - `LayoutConfigTest.kt`: Validação de defaults, retrocompatibilidade de JSONs legados e round-trip completo com campos de logo e recado.
  - `ClientSearchTest.kt`: Validação de busca insensível a acentuação e ordenação multi-mês.
  - `BackupManagerTest.kt`: Validação de exportação/importação e integridade do schema JSON.
  - `CalendarEngineTest.kt`: Validação de offset de início de semana e linhas do grid.
  - `SeasonalRadarTest.kt`: Validação de cálculo de datas comerciais brasileiras e dicas.
- **Compilação e R8:** Builds `assembleDebug`, `assembleRelease` e `assembleGodmode` 100% funcionais e validados no emulador `Belezura_Teste`.

---

## 7. Roadmap da Fase B: Hub Central & Módulo de Promoções

A **Fase B** transforma a tela inicial de um simples visualizador de mês em um verdadeiro **Hub de Negócios e Marketing Visual** para a profissional da beleza:

1. **Evolução da HomeScreen para Formato Hub:**
   - **Hero Card Dinâmico:** Visão rápida do mês atual com métricas acionáveis (vagas restantes, ocupação estimada, status da divulgação).
   - **Módulo de Ações Rápidas (Quick Actions):** Acesso em 1 toque para:
     - *Criar Agenda do Mês*
     - *Criar Post de Promoção / Aviso*
     - *Personalizar Moldura & Marca*
     - *Avisar Clientes no WhatsApp*
   - **Feed / Radar de Oportunidades Sazonais:** Destaque para a próxima data comemorativa comercial com contagem regressiva e sugestão de ação de marketing pronta.

2. **Módulo de Peças Rápidas / Promoções (Story 9:16):**
   - Criação de peças complementares ao calendário no mesmo padrão 1080×1920:
     - Avisos de "Agenda Aberta" / "Últimos Horários".
     - Tabela de Preços / Combos Promocionais.
     - Lembretes e Regras de Atendimento (ex: tolerância de atraso, cancelamento).
   - Reutilização dos motores gráficos já estáveis (`CalendarRenderer`, `ThemeCatalog`, `FontManager`, `ExportHelper`).
