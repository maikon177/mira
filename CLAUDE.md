# Instruções para Claude — Jarvis Tarefas Pata 3D

## Objetivo do projeto

Criar um assistente operacional pessoal para tarefas, foco, alertas inteligentes e IA revisora.

O app deve ajudar o usuário a responder uma pergunta central:

> Qual é a próxima ação mais importante agora?

O projeto não deve virar apenas uma agenda comum nem uma lista gigante de tarefas.

## Direção principal

O sistema deve ter duas camadas:

1. Camada fixa do app:
   - tarefas;
   - status;
   - alertas;
   - histórico;
   - regras automáticas;
   - banco de dados;
   - notificações.

2. Camada de IA:
   - revisar tarefas;
   - organizar entrada bagunçada;
   - sugerir prioridade;
   - dividir tarefas grandes;
   - gerar próxima ação;
   - compactar memória;
   - testar abordagens de notificação.

A IA não deve ser obrigatória para o app funcionar. O app precisa continuar útil mesmo se a API estiver fora do ar.

## API escolhida inicialmente

A API escolhida para o MVP é a DeepSeek, por custo baixo.

Variáveis esperadas:

```env
DEEPSEEK_API_KEY=coloque_sua_chave_real_aqui
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-v4-flash
```

## Segurança

Nunca enviar `.env` para o GitHub.

O repositório já contém:

- `.gitignore`
- `.env.example`

A chave real deve ficar somente no ambiente local ou no servidor.

## Arquivos principais do projeto

```text
README.md
docs/plano_acao_app_jarvis_tarefas.md
docs/mvp.md
docs/arquitetura.md
docs/roadmap.md
prompts/ia_revisora.md
database/schema_inicial.md
.env.example
.gitignore
```

## MVP recomendado

Primeira versão deve conter apenas o essencial:

- cadastro rápido de tarefa;
- caixa de entrada;
- tela Agora;
- tela Hoje;
- status de tarefa;
- histórico básico;
- IA revisora simples;
- memória compactada simples;
- notificações decisivas.

Evitar no MVP:

- WhatsApp;
- voz avançada;
- dashboard complexo;
- automações pesadas;
- IA rodando o tempo todo;
- dependência total da API.

## Comportamento esperado da IA revisora

A IA deve receber uma entrada bagunçada, por exemplo:

```text
Tenho que responder João, terminar a maquete, comprar filamento e arrumar o bico da Ender.
```

E devolver JSON estruturado com:

- tarefas organizadas;
- categoria;
- prioridade;
- tempo estimado;
- motivo da prioridade;
- próxima ação;
- alerta sugerido.

Consultar o arquivo:

```text
prompts/ia_revisora.md
```

## Regras de prioridade

Priorizar:

- cliente aguardando;
- prazo próximo;
- chance de venda;
- máquina parada;
- tarefa que desbloqueia produção;
- tarefa rápida com alto impacto;
- tarefa adiada várias vezes.

Evitar priorizar:

- ideias novas sem prazo;
- pesquisa aleatória;
- tarefa vaga;
- tarefa criativa quando há cliente pendente.

## Telas principais

### Tela Agora

Mostra apenas uma próxima ação.

Botões recomendados:

- iniciar;
- concluir;
- adiar;
- dividir;
- cancelar;
- justificar.

### Tela Hoje

Mostra no máximo 3 a 5 prioridades.

### Caixa de entrada

Recebe tarefas soltas.

### Revisão do dia

Mostra o que foi concluído, atrasado e reagendado.

## Próxima tarefa técnica recomendada

Criar uma implementação mínima para testar a DeepSeek com uma entrada fixa.

Sugestão de ordem:

1. Escolher stack inicial.
2. Criar cliente da DeepSeek.
3. Criar função `reviewTaskInput(input)`.
4. Usar o prompt em `prompts/ia_revisora.md`.
5. Forçar resposta em JSON.
6. Validar o JSON.
7. Criar teste com tarefa bagunçada.
8. Documentar o resultado.

## Stack sugerida

Para MVP rápido:

- Backend: Node.js ou Python FastAPI.
- Banco: Supabase/PostgreSQL.
- Frontend: PWA responsivo ou Android depois.

A decisão final da stack pode ser ajustada conforme preferência do desenvolvedor.

## Critério de sucesso do MVP

O MVP funciona quando o usuário consegue:

1. cadastrar tarefas rapidamente;
2. receber uma próxima ação clara;
3. concluir ou adiar;
4. ver histórico;
5. pedir revisão da IA;
6. receber uma tarefa reescrita de forma objetiva.

## Observação importante

Não transformar o projeto em um app grande logo no início.

O foco é validar o fluxo:

```text
entrada bagunçada -> tarefa organizada -> prioridade -> próxima ação -> decisão -> histórico
```
