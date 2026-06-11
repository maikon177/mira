# Memória compactada

## Objetivo

Permitir que o app aprenda com o usuário sem precisar treinar uma IA do zero.

A memória compactada transforma histórico de uso em aprendizados curtos, úteis e editáveis.

## O que o app pode aprender

Exemplos:

- o usuário conclui melhor tarefas de 10 a 30 minutos;
- o usuário ignora tarefas vagas;
- tarefas de cliente devem aparecer antes de tarefas criativas;
- tarefas técnicas funcionam melhor como checklist;
- ideias novas devem ir para revisão semanal;
- listas longas reduzem execução;
- notificações com motivo financeiro funcionam melhor para orçamentos.

## Tipos de memória

### 1. Preferência

Coisas que o usuário prefere.

Exemplo:

```text
O usuário prefere mensagens diretas e objetivas.
```

### 2. Rotina

Padrões de horário e execução.

Exemplo:

```text
O usuário costuma resolver melhor tarefas técnicas pela manhã.
```

### 3. Comportamento

Padrões detectados pelo uso.

Exemplo:

```text
O usuário tende a adiar tarefas com título vago.
```

### 4. Negócio

Regras do trabalho da Pata 3D.

Exemplo:

```text
Tarefas com cliente, prazo e produção bloqueada devem subir prioridade.
```

### 5. Estratégia de notificação

O que funciona melhor para cobrar o usuário.

Exemplo:

```text
Para tarefas de orçamento, abordagem com motivo financeiro tem melhor resposta.
```

## Como atualizar a memória

A memória não deve ser atualizada a cada clique.

Momentos recomendados:

- fim do dia;
- fim da semana;
- quando uma tarefa for adiada várias vezes;
- quando um padrão aparecer repetidamente;
- quando o usuário confirmar uma sugestão.

## Controle do usuário

O app deve permitir:

- ver o que foi aprendido;
- editar memória;
- apagar memória;
- desativar memória;
- impedir aprendizado de determinado tipo.

## Formato recomendado

```json
{
  "memory_type": "behavior",
  "content": "O usuário conclui melhor tarefas de 10 a 30 minutos.",
  "confidence": "alta",
  "is_active": true
}
```

## Regra importante

A memória deve ajudar a reduzir decisão, não manipular o usuário.
