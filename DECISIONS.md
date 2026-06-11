# DECISIONS — Decisões do projeto

Este arquivo registra decisões importantes para evitar mudanças sem direção.

## Decisão 1 — O app não depende 100% da IA

A IA deve ser uma camada revisora e organizadora.

O app precisa funcionar com:

- tarefas;
- status;
- histórico;
- regras;
- notificações;
- banco de dados.

Mesmo se a API estiver fora do ar, o app deve continuar funcionando como gerenciador de tarefas.

## Decisão 2 — API inicial

API inicial escolhida:

```text
DeepSeek
```

Modelo inicial:

```text
deepseek-v4-flash
```

Motivo:

- baixo custo;
- bom para texto curto;
- adequado para JSON;
- suficiente para organizar tarefas e criar próxima ação.

## Decisão 3 — Não enviar chave para o GitHub

A chave real deve ficar apenas no arquivo local:

```text
.env
```

O repositório contém apenas:

```text
.env.example
```

## Decisão 4 — Centro do produto

A tela principal é:

```text
Agora
```

Ela deve mostrar apenas uma próxima ação.

O app não deve virar uma lista gigante de tarefas.

## Decisão 5 — MVP simples

O MVP deve validar o fluxo principal antes de adicionar funções grandes.

Fluxo:

```text
entrada bagunçada -> tarefa organizada -> prioridade -> próxima ação -> decisão -> histórico
```

## Decisão 6 — Stack ainda não fechada

Opções recomendadas:

1. PWA + Supabase
2. Android + Firebase
3. Web + FastAPI + PostgreSQL

Decisão pendente:

```text
Escolher stack inicial.
```

## Decisão 7 — Prioridade de negócio

Para o usuário, tarefas ligadas a cliente, prazo, venda, entrega e máquina parada têm mais peso que ideias novas ou pesquisa aleatória.
