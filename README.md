# CompatMod v8.2.0

O **CompatMod** é um mod de compatibilidade avançado para Minecraft Forge, projetado para resolver conflitos de renderização e problemas de modelos entre diferentes mods. A versão 8.2.0 representa uma reescrita estrutural focada em robustez, performance e facilidade de manutenção.

## ✨ Funcionalidades

*   **Ajustes Visuais Dinâmicos**: Aplica patches em tempo real nos modelos JSON do Minecraft para corrigir problemas visuais comuns, como:
    *   `glass_cullface`: Melhora a renderização de blocos de vidro e painéis.
    *   `ambient_occlusion_disable`: Ajusta a iluminação em folhagens e plantas.
    *   `uv_normalization`: Corrige distorções de textura em modelos do tipo cross/flower.
*   **Sistema de Mixin Robusto**: Utiliza injeções seguras no `ModelBakery` para interceptar o carregamento de modelos sem comprometer a estabilidade do jogo.
*   **Safe Mode**: Mecanismo de segurança que desativa automaticamente as transformações em caso de falhas críticas detectadas.
*   **Cache Inteligente**: Sistema de cache LRU para minimizar o impacto na performance durante o carregamento de modelos.
*   **Configuração Flexível**: Permite desativar patches específicos ou colocar mods em uma blacklist via arquivo de configuração `.toml`.

## 🛠️ Requisitos

*   **Minecraft**: 1.21.1
*   **Forge**: 52.0.47+
*   **Java**: 21

## 🚀 Instalação

1.  Baixe o arquivo JAR da versão 8.2.0.
2.  Coloque o arquivo na pasta `mods` da sua instância do Minecraft Forge 1.21.1.
3.  Inicie o jogo normalmente.

## 💻 Comandos

O mod adiciona o comando `/compatmod` para gerenciamento in-game:

*   `/compatmod status`: Exibe o estado atual do mod e patches ativos.
*   `/compatmod cache`: Mostra estatísticas do cache de transformações.
*   `/compatmod reload`: Recarrega as configurações e patches (útil para desenvolvimento).

## 🔧 Para Desenvolvedores

### Compilação

Para compilar o projeto localmente, utilize o Gradle:

```bash
./gradlew clean build
```

O JAR reobfuscado para produção estará disponível em `build/libs/`.

### Testes

O projeto inclui uma suíte de testes unitários para validar a lógica de aplicação de patches:

```bash
./gradlew test
```

## 📄 Licença

Este projeto está sob a licença definida no arquivo `LICENSE` (se disponível) ou segue os termos padrão de uso da equipe CompatMod.

---
*Desenvolvido com foco em compatibilidade e performance para a comunidade Minecraft.*
