# CompatMod — Estado Atual do Projeto

**Última atualização:** 30/07/2026
**Versão:** 8.2.0 (Minecraft 1.21.1, Forge 52.1.0)

---

## 1. O que é o CompatMod

Um mod de Forge para Minecraft 1.21.1 que aplica ajustes automáticos de compatibilidade visual em models de blocos ao carregar o jogo:

- **`glass_cullface`** — força `render_type: translucent` e desliga ambient occlusion em models de vidro (vanilla e modado, por casamento de texto no nome do model).
- **`ambient_occlusion_disable`** — desliga ambient occlusion em folhagem (`leaves`, `foliage`, `tall_grass`).
- **`ambient_occlusion_disable_flowers`** — mesma coisa para models tipo cross (`flower`, `cross`).

Também tem: lista de bloqueio (blacklist) de models por nome, modo seguro (`safemode`) que desliga tudo, comandos in-game (`/compatmod status|cache|reload|safemode|blacklist|patches`), log de transformações em arquivo separado, e um sistema de config via `.toml` do Forge.

---

## 2. Estado atual — resumo direto

| Camada | Status |
|---|---|
| Compila (`gradlew build`) | ✅ Confirmado — build de 30/07 compilou limpo |
| Estrutura do jar (sem Mixin/refmap) | ✅ Confirmado por inspeção direta do jar compilado |
| Reobfuscação (nomes SRG corretos no bytecode) | ✅ Confirmado — decompilei e as chamadas batem |
| Testes automatizados (`gradlew test`) | ✅ Passam (cobrem os matchers de patch e a config de safe mode) |
| **Funciona de verdade dentro do jogo** | ✅ **Confirmado** — Testado pelo usuário (vidro, animações, comandos e blocos interagíveis funcionais) |

Ou seja: hoje o mod está numa posição estável — compila, empacota corretamente e foi validado em jogo real, confirmando que a nova arquitetura baseada em eventos é funcional e robusta.

---

## 3. Arquitetura atual (pós-reescrita de 30/07)

O mod **não usa mais Mixin**. Toda a lógica de patch entra por um evento oficial e documentado do Forge:

```
ModelEvent.ModifyBakingResult  (evento do mod event bus, só client-side)
        │
        ▼
ModelBakeListener.onModifyBakingResult()
        │
        ├─ ModConfig.isSafeMode()?  → se sim, não faz nada
        │
        └─ para cada model já assado no registro:
              ├─ BlacklistConfig.isBlacklisted(location.toString())?
              ├─ CompatRegistry.getPatches() → testa cada ModelPatch.matcher()
              │      (casa por substring no location.toString(), ex: "glass", "leaves")
              └─ se algum patch bateu:
                    registry.put(location, new CompatBakedModel(original, disableAO, translucent))
```

`CompatBakedModel` estende `net.minecraftforge.client.model.BakedModelWrapper` (classe pública do Forge feita pra isso) e sobrescreve só dois métodos: `useAmbientOcclusion()` e `getRenderTypes()`. Tudo o mais é delegado pro model original.

### Arquivos principais

| Arquivo | Papel |
|---|---|
| `CompatMod.java` | Ponto de entrada (`@Mod`), inicializa config/blacklist/registry/logger |
| `patch/ModelBakeListener.java` | Escuta o evento de baking, decide o que vira patch |
| `patch/CompatBakedModel.java` | Wrapper que aplica o efeito visual (AO / render type) |
| `patch/CompatRegistry.java` | Define os 3 patches embutidos e filtra por `disabledPatches` do config |
| `patch/ModelPatch.java` | Estrutura de um patch: nome, matcher (`Predicate<String>`), flags |
| `config/BlacklistConfig.java` | Lista de models bloqueados (thread-safe) |
| `config/ModConfig.java` | Config `.toml` (safe mode, log, blacklist de patches) |
| `command/CompatCommand.java` | Comandos `/compatmod ...` |
| `cache/CacheInspector.java` | Contadores de cache/patch aplicados, usado no `/compatmod cache` |
| `logging/LegacyTransformLogger.java` | Log assíncrono em arquivo separado de cada patch aplicado |
| `safemode/SafeModeHandler.java` | Reporta se o mod está operacional (`/compatmod status`) |
| `bridge/VersionBridge.java` | Log de diagnóstico da versão MC/Forge no startup |
| `baking/ModelBaker.java` | **Não usado hoje** — utilitário de merge de JSON, sem ponto de integração no design atual (documentado no próprio arquivo) |

---

## 4. Como chegamos até aqui (resumo da investigação)

A wallet completa está no arquivo `AUDITORIA_COMPATMOD_v7.9.0_a_v8.2.0.md` (Partes A–I). Resumo:

1. **v7.9.0** (jar original): decompilado e auditado sem acesso ao código-fonte. Achados principais — refmap do Mixin ausente do jar, tabela de compatibilidade sem dados pra nenhum dos mods testados (JEI/IC2/Thaumcraft/OptiFine/BuildCraft), várias classes nunca chamadas por nada, e um sistema de download de patch que baixava `.class` mas nunca carregava.
2. **v8.2.0 "fixed"**: acabou sendo uma reescrita quase total (não um patch em cima da v7.9.0), revivendo um pacote antigo abandonado (`com.example.compatmod` → `com.compatmod`). Corrigiu o bug do Gradle que causava o refmap ausente, mas trocou o escopo pra 3 patches visuais genéricos (não ataca mais JEI/IC2/etc.) e introduziu uma extração de JSON via reflection frágil, que corrigi (ler o JSON de verdade do resource pack em vez de tentar reconstruir a partir de um `BlockModel` já montado).
3. **Varredura de código morto**: achei e liguei várias classes/métodos que existiam mas nunca eram chamados (`VersionBridge`, `SafeModeHandler.isOperational()`, `LegacyTransformLogger.shutdown()`, `CacheInspector.reset()`, a opção de config `disabledPatches`).
4. **Primeiro crash real**: revelou dois bugs — um construtor com assinatura errada (`CompatMod(IEventBus)` em vez do clássico construtor vazio + `FMLJavaModLoadingContext.get()` — erro meu, corrigido) e uma falha de Mixin (`@Inject` não achava `ModelBakery.getModel` em produção).
5. **A saga do Mixin/SRG**: várias rodadas confirmando que o refmap, o reobf e a versão do Forge estavam tecnicamente corretos (inclusive cruzando com o mapeamento oficial público do MCPConfig), e mesmo assim o crash persistia de forma inconsistente entre builds. Depois de esgotar as hipóteses de configuração, decidimos abandonar o Mixin.
6. **Reescrita pra `ModelEvent.ModifyBakingResult`** (estado atual): tirou a dependência de Mixin/refmap/SRG por completo. Passou por duas rodadas de erro de compilação real (tipo errado assumido para o registro de models, e depois `ModelResourceLocation` não sendo mais subtipo de `ResourceLocation` como eu supunha) — ambos corrigidos, e a build de 30/07 compilou limpo.

---

## 5. O que foi perdido ou fica pendente

- **Patch `uv_normalization`** (recorte de UV fora do intervalo [0,16]) foi removido nesta reescrita — não tem equivalente limpo trabalhando em cima de `BakedModel` já assado (os vértices já estão em formato empacotado `int[]`, não floats soltos). Se precisar de volta, é uma tarefa separada (provavelmente via `QuadTransformers`).
- **Compatibilidade específica com JEI/IC2/Thaumcraft/OptiFine/BuildCraft** (o problema que motivou a auditoria original) nunca foi endereçada em nenhuma versão — o mod hoje resolve um problema diferente (ajustes visuais genéricos).
- **Teste em jogo real** confirmado com sucesso (01/08/2026).

---

## 6. Histórico de Validação

O mod foi validado com sucesso pelo usuário em **01/08/2026**, confirmando que:
1. O jogo carrega normalmente (sem crashes).
2. O vidro renderiza translúcido corretamente (patch `glass_cullface`).
3. Animações de blocos estão funcionando.
4. Comandos in-game (`/compatmod`) estão operacionais.
5. Blocos interagíveis funcionam conforme o esperado.

O projeto está agora em estado estável e funcional para Minecraft 1.21.1 e Forge 52.1.0.
