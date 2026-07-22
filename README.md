# CompatMod v7.9.0

> **Forge Compatibility Layer for Legacy Minecraft Mods**
> Fixes silent model breakage when loading 1.12-era mods on 1.20+ Forge.

## Compatibilidade

Compatibilidade de versões para Minecraft Forge 1.20.1

## Sobre o Mod

O CompatMod é um mod para Minecraft Forge que detecta, reporta e despacha automaticamente problemas de compatibilidade entre modpacks. Ele intercepta o pipeline de carregamento de modelos do Minecraft via Mixin, identifica falhas e envia patches para um servidor remoto que os versiona em Git.

A versão **7.9.0** introduziu o **subsistema de patch via Git** — o mod agora se comunica com um servidor HTTP (Python/Flask) que recebe os patches, escreve arquivos JSON e faz commit+push automático para um repositório Git.

## Funcionalidades

| Funcionalidade | Descrição |
|---|---|
| Patches via HTTP | Envia patches para servidor remoto com retry (3 tentativas) |
| Identificação de Modpack | Lê `modpack-id` ou `MODPACK_ID` env |
| Mixin Hooks | Intercepta `ModelBakery.bakeModel` e Minecraft `<init>` |
| Cache de Transformações | `ConcurrentHashMap` thread-safe com evicção LRU (512 entradas) |
| Logging Estruturado | SLF4J com markers: `PATCH`, `MIXIN`, `LIFECYCLE`, `DEBUG_CMD` |
| Health Check | Verificação de conectividade com patch server no startup |
| Comandos Debug | `/compatmod status`, `/compatmod modpack`, `/compatmod patch-test` |
| Configuração Externa | `config/compatmod.properties` com 5 opções |
| Patch Server | Servidor Flask que recebe patches e faz git commit+push |

## Fluxo de Patch

1. Modpack inicia -> ModpackIdentifier resolve o ID (modpack-id ou MODPACK_ID env -> "unknown-modpack")
2. ForgeCompatMod inicializa GitPatchManager se `git_patch_enabled=true`
3. MixinModelBakery intercepta erros na carga de modelos e `dispatchPatch()`
4. PatchProtocol envia mensagem JSON (COMPAT_PATCH/COMPAT_WARNING/COMPAT_ERROR)
5. GitPatchManager envia HTTP POST assíncrono com retry (1s, 2s, 3s)
6. Patch Server (Flask) salva em `patches/{hash}_{timestamp}_{type}.json` -> git commit+push

## Requisitos

### Mod (Cliente)

- Minecraft: 1.20.1
- Forge: 47.2.0+
- Java: 17+
- Gradle: 8.5 (wrapper incluso)

### Patch Server (Opcional)

- Python: 3.9+
- Flask: 3.0+
- GitPython: 3.1+

## Configuração

Na primeira execução, gera `config/compatmod.properties`:

| Propriedade | Padrão | Descrição |
|---|---|---|
| `git_patch_enabled` | `true` | Ativa/desativa envio de patches |
| `git_patch_server_url` | `http://localhost:9090/api/patch` | URL do patch server |
| `patch_retry_count` | `3` | Tentativas em caso de falha |
| `patch_timeout_ms` | `15000` | Timeout HTTP (ms) |
| `debug_commands_enabled` | `true` | Habilita `/compatmod` |

## Comandos

| Comando | Descrição |
|---|---|
| `/compatmod status` | Versão, modpack ID, status do patch server |
| `/compatmod modpack` | Modpack ID atual |
| `/compatmod patch-test` | Envia patch de teste |

## Estrutura do Projeto

(Resumo dos principais arquivos e diretórios)
- `build.gradle`, `settings.gradle`, `gradle.properties`
- `src/main/java/com/compatmod/...` (Arquivos fonte Java)
- `src/main/resources/...` (Recursos, `mixins.json`, `mods.toml`)
- `docs/`
- `git/` (GitPatchManager, PatchProtocol, ModpackIdentifier)
- `core/` (HealthChecker, Logging)
- `mixin/` (MixinCompatMgr, MixinModelBakery)

## Testes

- JUnit 5 (5 classes)
- `PatchProtocolTest`: Criação de mensagens + JSON round-trip

## Changelog v7.9.0

- **Adicionado**: Git-patch subsystem. Patch server Flask, ModpackIdentifier, comandos modpack/patch-test, MixinModelBakery dispatch, testes JUnit 5
- **Corrigido**: Box-drawing chars no `patch_server.py`, delimitador " no `VirtualModelLoader`, `sendPacket`->`sendPatch`

## Publicação

1. `./gradlew build` -> `build/libs/compatmod-7.9.0.jar`
2. Publique no CurseForge, Modrinth, GitHub Releases
3. Checklist build limpo, testes OK, `compatmod` status healthy, tag v7.9.0

Licença MIT - CompatMod Team 2026
