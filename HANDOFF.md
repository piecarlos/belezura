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

# 4. Instalar APK Release no emulador ou dispositivo via ADB
adb -e install -r app\build\outputs\apk\release\app-release.apk

# 5. Iniciar app via ADB
adb -e shell am start -n com.salu.calendario.v2/com.salu.calendario.MainActivity
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
│   ├── LayoutAdjustPanel.kt     # Painel "Ajustar Layout e Cores" (abas, sub-abas, sliders, cor e logo)
│   ├── ThemeGallerySheet.kt     # Galeria de molduras/temas com radar sazonal, miniaturas reais e filtros
│   ├── LogoSettingsDialog.kt    # Diálogo de configuração, visualização e modos da logomarca
│   ├── ProPreviewDialog.kt      # Modal de transparência e preview de modelos exclusivos PRO
│   ├── DayEditSheet.kt          # ModalBottomSheet de edição do dia (nomes + toggle "X" vermelho)
│   ├── EditorDialogs.kt         # Diálogos auxiliares (MonthYearPicker, confirmação de restauração)
│   ├── CustomBackgroundDialog.kt# Modal de orientação prévia e seleção de imagem para moldura
│   ├── CalendarViewModel.kt     # State holder reativo (StateFlow), undo/redo, temas, logo e ações
│   ├── CalendarRenderer.kt      # Motor gráfico WYSIWYG Canvas 1080×1920 (preview, temas, logo e exportação)
│   ├── CalendarEngine.kt        # Cálculos de calendário, deslocamentos, nomes pt-BR e grid
│   ├── CalendarModels.kt        # Modelos imutáveis (CellData, EditableElement, AppScreen, etc.)
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
| **`EditorScreen.kt`** | Tela principal do editor. Contém o `Scaffold`, `TopAppBar` (Undo/Redo, navegação de mês, botão de ajuste, atalho de moldura e menu de backup), `CalendarCanvasView` com gestos multi-touch (arraste e pinça sincronizados) e toque direto na logomarca. |
| **`LayoutAdjustPanel.kt`** | Painel modular de ajuste visual (zero rolagem). Quatro abas (`Mês`, `Ano`, `Calendário`, `Logo`), sub-abas (`Posição`, `Fonte`/`Estilo`, `Cor`), sliders (com escala 50%..220% para a logo), botões de ajuste fino (`← ↑ ↓ →`) e `Centralizar X`. |
| **`ThemeGallerySheet.kt`** | Galeria modal de temas com banner dinâmico do Radar Sazonal (`SeasonalRadar`), miniaturas reais geradas para temas embutidos (`bg_make_glam`, `bg_make_glam_2`, `bg_base`), filtros de categoria e cards em proporção 9:16 com badge PRO e borda ativa. |
| **`LogoSettingsDialog.kt`** | Modal dedicado de configuração da logomarca: prévia em tempo real, envio de arquivo, seleção de modos (`Original`, `Sem fundo branco`, `Selo redondo`), ativação no rodapé e remoção. |
| **`ProPreviewDialog.kt`** | Diálogo de transparência radical para modelos exclusivos PRO: exibe badge de diamante e explica de forma honesta a proposta de assinatura futura sem travas inesperadas na exportação. |
| **`DayEditSheet.kt`** | `ModalBottomSheet` de edição do dia. Permite alternar o "X" vermelho de dia indisponível, adicionar/remover clientes, reordenar prioridades e autocompletar. |
| **`EditorDialogs.kt`** | Diálogos modais do editor: `MonthYearPickerDialog` (seletor rápido de mês/ano) e `ConfirmRestoreDialog` (confirmação atômica de restauração). |
| **`CustomBackgroundDialog.kt`** | Modal de orientação prévia antes do upload da moldura (recomenda formato 9:16 vertical e explica ajuste central automático). |
| **`CalendarViewModel.kt`** | Gerenciamento de estado (`CalendarUiState`), pilhas de Undo/Redo com snapshots de células, motor de busca sem acento (`ClientSearchHelper`), navegação de meses, ajuste fino de layout com arraste 2D e pinça (incluindo `LOGO`), catálogo de temas e logo. |
| **`CalendarRenderer.kt`** | Motor gráfico Canvas. Desenha fundo (temas procedurais do `ThemeCatalog` ou fundos customizados/embutidos), logomarca no rodapé em 3 modos com escala/offset e bounding box para toque direto, texto do mês, ano 3D com 4 camadas de relevo/sombra e swoosh, cartão e grade do calendário, letras dos dias da semana, numerais, "X" vermelho centralizado, algoritmo de auto-fit de nomes em 2 passos e caixas tracejadas em modo de edição. |
| **`CalendarEngine.kt`** | Lógica de datas baseada em `java.time.YearMonth`. Mapeamento de domingo como início de semana (padrão brasileiro `value % 7`), contagem dinâmica de semanas (5 ou 6 linhas) e geração das células do grid. |
| **`CalendarModels.kt`** | Definições de `CellData` (slot, dia, hasRedX, lista de nomes), `MonthData`, `EditableElement` (`NONE`, `MONTH`, `YEAR`, `GRID_CARD`, `LOGO`), `AppScreen` (`HOME`, `CALENDAR`), `MonthSummary` e `ClientSearchResult`. |
| **`LayoutConfig.kt`** | Armazena escalas, cores hexadecimais, fontes, coordenadas e configurações de tema/logomarca (`selectedThemeId`, `logoEnabled`, `logoMode`, `logoScale`, `logoOffsetX`, `logoOffsetY`). Garante total compatibilidade retroativa com backups legados. |
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

---

## 6. Status Atual das Funcionalidades e Validação

- **Fase A (100% Concluída):**
  - Identidade visual **Belezura** com Adaptive Icon 3D em alta definição gerado em todas as densidades mipmap com fundo sólido `#5B0224`.
  - Galeria de molduras integrada (`ThemeGallerySheet`) com temas procedurais e temas fotográficos embutidos (`Make Glam Luxo` e `Make Glam Floral`), exibindo miniaturas reais e aplicando paleta automática (`ThemePalette`) com escala adaptada da grade (`defaultCalendarScale = 0.88f`).
  - Suporte completo a logomarca no rodapé: diálogo de configuração (`LogoSettingsDialog`), 3 modos visuais (Original, Sem fundo branco, Selo redondo), toque direto na logo no canvas para abrir o painel de ajuste, e gestos de pinça e arraste livre com escala de 50% a 220%.
- **Suíte de Testes Automatizados:**
  - `ClientSearchTest.kt`: Validação de busca insensível a acentuação e ordenação multi-mês.
  - `BackupManagerTest.kt`: Validação de exportação/importação e integridade do schema JSON.
  - `CalendarEngineTest.kt`: Validação de offset de início de semana e linhas do grid.
- **Compilação e R8:** Builds `assembleDebug` e `assembleRelease` 100% funcionais, aprovados e validados no emulador `Belezura_Teste`.

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
    "cardAccentColor": "#FFE89BAE"
  },
  "months": {
    "month_data_2026_12": {
      "slots": [ ... ]
    }
  }
}
```

> **Regra Obrigatória:** Todas as leituras de JSON devem usar `optDouble`, `optInt` e `optString` com valores padrão de fallback, garantindo que o app nunca quebre ao carregar dados salvos em versões anteriores.

---

## 6. Status Atual das Funcionalidades e Validação

- **Fase A (100% Concluída):**
  - Identidade visual **Belezura** com Adaptive Icon 3D em alta definição gerado em todas as densidades mipmap com fundo sólido `#5B0224`.
  - Galeria de molduras integrada (`ThemeGallerySheet`) com temas procedurais e temas fotográficos embutidos (`Make Glam Luxo` e `Make Glam Floral`), exibindo miniaturas reais e aplicando paleta automática (`ThemePalette`) com escala adaptada da grade (`defaultCalendarScale = 0.88f`).
  - Suporte completo a logomarca no rodapé: diálogo de configuração (`LogoSettingsDialog`), 3 modos visuais (Original, Sem fundo branco, Selo redondo), toque direto na logo no canvas para abrir o painel de ajuste, e gestos de pinça e arraste livre com escala de 50% a 220%.
- **Tela Inicial (Home):** Resumo dinâmico do mês ativo, botão de compartilhamento PNG direto, carrossel de meses salvos e atalho `+ Outro mês`.
- **Busca Global de Clientes:** Motor `ClientSearchHelper` com normalização de diacríticos (`Normalizer.Form.NFD`) e case-insensitive; toque no resultado abre diretamente o mês e o bottom sheet de edição do dia.
- **Painel de Ajuste Zero-Rolagem:** Quatro abas de topo (`Mês`, `Ano`, `Calendário`, `Logo`), sub-abas de ferramentas (`Posição`, `Fonte`/`Estilo`, `Cor`), ajuste fino direcional (`← ↑ ↓ →`) e botão `Centralizar X`.
- **Suíte de Testes Automatizados:**
  - `ClientSearchTest.kt`: Validação de busca insensível a acentuação e ordenação multi-mês.
  - `BackupManagerTest.kt`: Validação de exportação/importação e integridade do schema JSON.
  - `CalendarEngineTest.kt`: Validação de offset de início de semana e linhas do grid.
- **Compilação e R8:** Builds `assembleDebug` e `assembleRelease` 100% funcionais e validados no emulador `Belezura_Teste`.
