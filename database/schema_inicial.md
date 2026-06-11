# Schema inicial do banco de dados

Este documento descreve uma estrutura inicial de banco para o MVP.

## Tabela: tasks

Armazena as tarefas principais.

Campos:

```sql
id uuid primary key,
title text not null,
description text,
category text,
project_id uuid,
client_name text,
due_at timestamp,
manual_priority integer,
calculated_priority integer,
estimated_minutes integer,
actual_minutes integer,
status text not null,
snooze_count integer default 0,
created_at timestamp default now(),
updated_at timestamp default now(),
completed_at timestamp,
last_alert_at timestamp,
next_review_at timestamp,
delay_reason text,
notes text
```

Status possíveis:

- inbox;
- planned;
- in_progress;
- paused;
- completed;
- delayed;
- canceled;
- waiting.

## Tabela: task_events

Registra o histórico de comportamento.

```sql
id uuid primary key,
task_id uuid references tasks(id),
event_type text not null,
event_data jsonb,
created_at timestamp default now()
```

Tipos de evento:

- task_created;
- task_started;
- task_completed;
- task_snoozed;
- task_canceled;
- alert_sent;
- alert_ignored;
- task_split;
- priority_changed;
- ai_reviewed.

## Tabela: projects

Organiza tarefas maiores.

```sql
id uuid primary key,
name text not null,
description text,
status text,
due_at timestamp,
created_at timestamp default now(),
updated_at timestamp default now()
```

## Tabela: user_memories

Salva aprendizados compactados.

```sql
id uuid primary key,
memory_type text not null,
content text not null,
confidence text,
is_active boolean default true,
created_at timestamp default now(),
updated_at timestamp default now()
```

Tipos de memória:

- preference;
- routine;
- behavior;
- business;
- notification_strategy.

Exemplo:

```text
Tipo: behavior
Conteúdo: O usuário conclui melhor tarefas quebradas em blocos de 10 a 30 minutos.
Confiança: alta
Ativa: sim
```

## Tabela: notification_strategies

Armazena estratégias de abordagem.

```sql
id uuid primary key,
name text not null,
description text,
category text,
is_active boolean default true,
created_at timestamp default now()
```

Estratégias iniciais:

- direta;
- motivo financeiro;
- passo pequeno;
- urgência;
- anti-procrastinação;
- checklist técnico.

## Tabela: notification_results

Mede o resultado das abordagens.

```sql
id uuid primary key,
task_id uuid references tasks(id),
strategy_id uuid references notification_strategies(id),
message text,
result text,
created_at timestamp default now()
```

Resultados possíveis:

- ignored;
- opened;
- started;
- completed;
- snoozed;
- canceled;
- split.

## Observação

O banco deve ser simples no começo. O objetivo é validar o fluxo antes de criar automações complexas.
