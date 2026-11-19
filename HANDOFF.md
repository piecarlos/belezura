# HANDOFF TÉCNICO — AGENDA VISUAL (BELEZURA / SALÚ)

> **Documento de continuidade para desenvolvedores e agentes de IA** (Copilot, Claude, Gemini, Cursor, OpenCode).  
> Leia atentamente este documento antes de realizar qualquer alteração no código.

---

## 1. Visão Geral Técnica

Aplicativo Android nativo desenvolvido em **Kotlin** com interface declarativa em **Jetpack Compose (Material 3)**.  
O objetivo principal é gerar, editar interativamente e exportar agendas visuais mensais no formato vertical **9:16 (`1080×1920` px)** para divulgação em redes sociais (WhatsApp Status, Instagram Stories e envio direto a clientes).

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

# 4. Instalar APK Release no aparelho conectado via ADB
adb install -r app\build\outputs\apk\release\app-release.apk

# 5. Iniciar app via ADB
adb shell am start -n com.salu.calendario.v2/com.salu.calendario.MainActivity
```

---

## 3. Arquitetura e Mapa de Arquivos

```
app/src/main/
├── AndroidManifest.xml
├── java/com/salu/calendario/
│   ├── MainActivity.kt          # Activity raiz, navegação (AppScreen), scaffolding e painel de ajuste
│   ├── HomeScreen.kt            # Tela inicial, card do mês ativo, busca de clientes e atalhos
│   ├── CalendarViewModel.kt     # State holder reativo (StateFlow), undo/redo, busca e ações
│   ├── CalendarRenderer.kt      # Motor gráfico WYSIWYG Canvas 1080×1920 (preview e exportação)
│   ├── CalendarEngine.kt        # Cálculos de calendário, deslocamentos, nomes pt-BR e grid
│   ├── CalendarModels.kt        # Modelos imutáveis (CellData, EditableElement, AppScreen, etc.)
│   ├── LayoutConfig.kt          # Configuração geométrica e visual com serialização/deserialização JSON
│   ├── BackupManager.kt         # Exportação/importação de backup (.json) SAF
│   ├── BackgroundManager.kt     # Carregamento seguro da imagem de fundo base e customizada
│   ├── FontManager.kt           # Gerenciador e cache de Typefaces embutidas
│   └── ExportHelper.kt          # Renderização offscreen 1080×1920, salvamento MediaStore e FileProvider
└── res/
    ├── drawable-nodpi/bg_base.png   # Fundo base limpo oficial em 1080×1920 (sem upscaling)
    ├── font/                        # Fontes TrueType estáticas embutidas
    │   ├── cinzel_bold.ttf
    │   ├── fredoka_bold.ttf
    │   └── playfair_display_bold.ttf
    ├── raw/keep.xml                 # Blindagem contra remoção indevida pelo R8 shrinker
    └── xml/file_paths.xml           # Configuração de caminhos do FileProvider para compartilhamento
```

### Detalhamento dos Componentes

| Arquivo | Responsabilidade Exata |
|---|---|
| **`MainActivity.kt`** | Ponto de entrada (`ComponentActivity`). Gerencia `BackHandler` contextual, transição entre `HomeScreen` e editor do calendário, `LayoutAdjustmentPanel` (zero rolagem com abas `Mês`, `Ano`, `Calendário` e sub-abas `Posição`, `Fonte`, `Cor`), `CellEditBottomSheet` e diálogos do sistema. |
| **`HomeScreen.kt`** | Painel inicial rápido. Hero card com contadores dinâmicos de agendamentos e dias com "X", botão rápido para compartilhar PNG sem abrir editor, busca global de clientes, carrossel "Meus Meses" (`LazyRow`) e atalhos de "Aparência e Backup". |
| **`CalendarViewModel.kt`** | Gerenciamento de estado (`CalendarUiState`), pilhas de Undo/Redo com snapshots de células, motor de busca sem acento (`ClientSearchHelper`), navegação de meses, ajuste fino de layout com arraste 2D e acionamento de exportação assíncrona. |
| **`CalendarRenderer.kt`** | Motor gráfico Canvas. Desenha fundo, texto do mês, ano 3D com 4 camadas de relevo/sombra e traço curvo (*swoosh*), cartão e grade do calendário, letras dos dias da semana, numerais, "X" vermelho centralizado, algoritmo de auto-fit de nomes em 2 passos e bounding boxes tracejadas em modo de edição. |
| **`CalendarEngine.kt`** | Lógica de datas baseada em `java.time.YearMonth`. Mapeamento de domingo como início de semana (padrão brasileiro `value % 7`), contagem dinâmica de semanas (5 ou 6 linhas) e geração das células do grid. |
| **`CalendarModels.kt`** | Definições de `CellData` (slot, dia, hasRedX, lista de nomes), `MonthData`, `EditableElement` (`NONE`, `MONTH`, `YEAR`, `GRID_CARD`), `AppScreen` (`HOME`, `CALENDAR`), `MonthSummary` e `ClientSearchResult`. |
| **`LayoutConfig.kt`** | Armazena escalas, cores hexadecimais, fontes e coordenadas absolutas/offsets (`monthX`, `monthY`, `monthOffsetX`, `monthOffsetY`, `yearX`, `yearY`, `yearOffsetX`, `yearOffsetY`, `cardOffsetX`, `cardOffsetY`, `cardScale`, `cardAccentColor`). Garante compatibilidade retroativa via fallbacks `optDouble`. |
| **`BackupManager.kt`** | Gera e consome backups universais em JSON estruturado (`SCHEMA_VERSION = 1`). Limpa e restaura atomicamente o `SharedPreferences` preservando integridade referencial. Formata data do último backup (`last_backup_at`). |
| **`BackgroundManager.kt`** | Decodifica `res/drawable-nodpi/bg_base.png` com `inScaled = false` para manter a escala 1:1. Processa fundos customizados da galeria com downsampling de memória e rotação EXIF. |
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
   - **Passo 2 (Horizontal):** Cada linha é medida individualmente (`measureText`). Nomes longos (ex: *"Maria Antonieta"*, *"Lysla Andreia Campos"*) sofrem redução individual para nunca ultrapassarem a largura útil da célula.
3. **Marcação com "X" Vermelho:**
   - Desenhado com proporção quadrada (`min(w, h) * 0.70f`), centralizado na célula e posicionado **atrás dos nomes** (`drawRedX` antes de `drawNames`) para manter a legibilidade dos textos caso ambos existam.
4. **Sincronização do Toque com Transformações do Grid:**
   - Ao tocar no preview, o cálculo de conversão `Offset` → célula considera `cardOffsetX`, `cardOffsetY` e `cardScale`. Modificações na escala ou translação do cartão devem refletir imediatamente na função `getCellAt`.
5. **Insets e Barras do Sistema:**
   - Todas as barras superiores, inferiores e o painel de ajuste devem conter `statusBarsPadding()` e `navigationBarsPadding()`. Nunca remova esses insets para não causar sobreposição com os botões virtuais do Android (gestos/3 botões).
6. **Proteção R8 e Shrinkage de Recursos:**
   - As fontes e o fundo base são protegidos pelo arquivo `res/raw/keep.xml` (`tools:keep="@font/*,@drawable/bg_base"`).
   - `FontManager` e `BackgroundManager` devem continuar referenciando recursos diretamente via identificadores estáticos gerados (`R.font.*` e `R.drawable.*`), evitando busca reflexiva por strings (`getIdentifier`).
7. **Autoridade do FileProvider Dinâmica:**
   - O `AndroidManifest.xml` utiliza `android:authorities="${applicationId}.fileprovider"`.
   - O `ExportHelper.kt` deve invocar `FileProvider.getUriForFile` com `"${context.packageName}.fileprovider"`. Isso permite que versões de teste rodem lado a lado sem conflito de autoridade de provedor.
8. **Decodificação de Bitmaps em 1:1:**
   - Bitmaps embutidos devem residir em `drawable-nodpi` e ser decodificados com `BitmapFactory.Options().apply { inScaled = false }` para evitar upscaling automático por densidade de tela (`xxxhdpi`).
9. **Zero Dependências Pesadas e Zero Permissão de Internet:**
   - Não adicione Room, SQLite, Ktor ou Retrofit. Mantenha persistência leve via `SharedPreferences`. Não solicite `android.permission.INTERNET` enquanto o aplicativo operar em modo local.

---

## 5. Esquema de Dados e Persistência

### 5.1 SharedPreferences (`salu_calendar_prefs`)
Os dados são armazenados de forma atômica no arquivo privado `salu_calendar_prefs.xml`:

| Chave | Tipo | Descrição |
|---|---|---|
| **`layout_config`** | `String (JSON)` | Serialização do objeto `LayoutConfig` (escalas, fontes, cores e coordenadas X/Y). |
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

- **Tela Inicial (Home):** Resumo dinâmico do mês ativo, botão de compartilhamento PNG direto, carrossel de meses salvos e atalho `+ Outro mês`.
- **Busca Global de Clientes:** Motor `ClientSearchHelper` com normalização de diacríticos (`Normalizer.Form.NFD`) e case-insensitive; toque no resultado abre diretamente o mês e o bottom sheet de edição do dia.
- **Painel de Ajuste Zero-Rolagem:** Três abas de topo (`Mês`, `Ano`, `Calendário`), sub-abas de ferramentas (`Posição`, `Fonte`, `Cor` com círculo indicador dinâmico), ajuste fino direcional (`← ↑ ↓ →`) e botão `Centralizar X`.
- **Movimento Livre 2D:** Mês, Ano 3D e Grade do Calendário possuem suporte total a movimentação horizontal e vertical, com atualização imediata das áreas de toque e contornos de seleção.
- **Suíte de Testes Automatizados:**
  - `ClientSearchTest.kt`: Validação de busca insensível a acentuação e ordenação multi-mês.
  - `BackupManagerTest.kt`: Validação de exportação/importação e integridade do schema JSON.
  - `CalendarEngineTest.kt`: Validação de offset de início de semana e linhas do grid.
- **Compilação e R8:** Builds `assembleDebug` e `assembleRelease` 100% funcionais e validados em dispositivo físico.
