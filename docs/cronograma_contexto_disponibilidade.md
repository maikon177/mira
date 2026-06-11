# Cronograma semanal e contexto de disponibilidade

## Origem da ideia

Uma sugestão importante para o projeto foi criar um cronograma semanal com os horários em que o usuário normalmente trabalha, pausa, está livre ou não deve receber tarefas complexas.

Isso permite que a IA entenda o contexto antes de sugerir uma próxima ação.

Exemplo:

```text
O usuário sai cedo para trabalhar.
Pausa entre 11:00 e 13:00.
Volta para casa por volta de 17:30 em dias úteis.
Tem mais tempo livre aos sábados e domingos.
```

## Objetivo

Evitar que o Jarvis sugira tarefas no momento errado.

O app precisa saber:

- quando o usuário está trabalhando;
- quando está em pausa;
- quando está indo ou voltando;
- quando está em casa;
- quando tem tempo livre;
- quando só pode receber tarefas rápidas;
- quando pode receber tarefas longas.

## Regra principal

A IA não deve sugerir uma tarefa que o usuário não consegue executar naquele contexto.

Exemplo ruim:

```text
Agora faça acabamento da maquete.
```

Se o usuário está no trabalho fora de casa, isso não faz sentido.

Exemplo melhor:

```text
Você está no horário de trabalho. Posso guardar essa tarefa para 18:00. Agora, se tiver 2 minutos, apenas responda uma mensagem rápida.
```

## Tipos de contexto

### 1. Trabalho externo

Quando o usuário está no trabalho e não pode executar tarefas da Pata 3D.

Permitir apenas:

- captura rápida de ideias;
- resposta curta, se permitido;
- planejamento leve;
- salvar tarefas para depois.

Evitar:

- tarefa de impressão;
- tarefa de acabamento;
- tarefa longa;
- manutenção de impressora;
- modelagem pesada.

### 2. Pausa

Exemplo: 11:00 a 13:00.

Permitir:

- check-in rápido;
- responder cliente;
- organizar tarefas;
- revisar prioridades;
- tarefa de até 10 minutos, se fizer sentido.

Evitar:

- tarefas longas;
- tarefas que exigem oficina ou PC;
- trabalho que crie estresse durante descanso.

### 3. Depois do trabalho

Exemplo: após 17:30.

Permitir:

- produção;
- impressão 3D;
- modelagem;
- acabamento;
- revisão do dia;
- preparação do dia seguinte.

### 4. Fim de semana

Permitir tarefas maiores, mas com cuidado para não lotar o dia.

Exemplo:

```text
Hoje tem mais tempo livre, mas vou sugerir só 3 blocos principais.
```

## Cronograma inicial sugerido

Este cronograma deve ser editável pelo usuário.

```json
{
  "segunda_a_sexta": {
    "manha_trabalho": "07:00-11:00",
    "pausa": "11:00-13:00",
    "tarde_trabalho": "13:00-17:30",
    "tempo_livre": "18:00-22:00"
  },
  "sabado": {
    "tempo_livre": "09:00-18:00",
    "revisao": "18:00-19:00"
  },
  "domingo": {
    "planejamento_semana": "17:00-18:00",
    "tempo_livre": "10:00-17:00"
  }
}
```

## Níveis de disponibilidade

Cada horário pode ter um nível de disponibilidade.

### Indisponível

Não sugerir tarefas. Apenas salvar.

### Baixa disponibilidade

Só sugerir tarefas de 2 a 5 minutos.

### Média disponibilidade

Sugerir tarefas de 5 a 20 minutos.

### Alta disponibilidade

Sugerir blocos maiores de 30 a 90 minutos.

## Exemplos práticos

### Horário de trabalho

Entrada:

```text
terminar maquete
```

Resposta do app:

```text
Salvei para depois do trabalho. No horário atual, isso não é executável.
```

### Pausa de almoço

Entrada:

```text
cliente perguntou valor
```

Resposta:

```text
Essa tarefa cabe na pausa. Próxima ação: responder cliente com valor inicial.
```

### Depois das 18:00

Entrada:

```text
arrumar impressora
```

Resposta:

```text
Agora é executável. Primeiro passo: verificar se o bico está entupido.
```

## Check-ins baseados no cronograma

O app deve ajustar os check-ins ao cronograma.

Sugestão:

- manhã antes do trabalho ou no início do dia;
- pausa do almoço;
- depois do trabalho;
- revisão no fim do dia.

Se o usuário estiver indisponível, o check-in deve ser leve:

```text
Quer só salvar algo para eu organizar depois?
```

## Integração com IA

Sempre que a IA for escolher próxima ação, enviar contexto compacto:

```text
Contexto atual:
- Dia: terça-feira
- Horário: 14:20
- Estado: trabalho externo
- Disponibilidade: baixa
- Pode fazer: salvar tarefa, responder algo rápido
- Não pode fazer: impressão, acabamento, manutenção, modelagem longa
```

## Formato de contexto para IA

```json
{
  "current_context": {
    "day": "terça-feira",
    "time": "14:20",
    "availability_level": "baixa",
    "location_context": "trabalho externo",
    "allowed_task_types": ["captura rápida", "resposta curta", "planejamento leve"],
    "blocked_task_types": ["impressão 3D", "acabamento", "manutenção", "modelagem longa"]
  }
}
```

## Configuração pelo usuário

O app deve ter uma tela de rotina semanal.

Campos:

- dias úteis;
- horário de trabalho;
- pausa;
- horário livre;
- fim de semana;
- horários de check-in;
- exceções.

## Exceções

O usuário deve poder marcar:

- folga;
- dia cheio;
- viagem;
- trabalho extra;
- compromisso;
- modo não incomodar.

## Regra de UX

O app não deve apenas perguntar “qual tarefa é importante?”.

Ele deve perguntar:

```text
Essa tarefa cabe no seu momento atual?
```

## Frase-guia

```text
Tarefa certa, na hora certa, com o tamanho certo.
```
