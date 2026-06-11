# Check-ins diários — captura de tarefas 3 vezes ao dia

## Objetivo

O app deve perguntar ao usuário, em momentos estratégicos do dia, se surgiram novas tarefas, mudanças de prioridade ou pendências esquecidas.

A ideia é evitar que o usuário dependa apenas da memória ou de abrir o app manualmente.

## Conceito

Em vez de o usuário precisar lembrar de organizar tudo, o Jarvis deve fazer perguntas curtas 3 vezes ao dia.

Esses check-ins devem ser simples, rápidos e com baixa carga mental.

## Frequência recomendada

### 1. Check-in da manhã

Horário sugerido:

```text
08:00 a 09:00
```

Objetivo:

- capturar tarefas novas;
- montar o plano do dia;
- escolher até 3 prioridades;
- identificar cliente, prazo e produção bloqueada.

Pergunta principal:

```text
Surgiu alguma tarefa nova para hoje?
```

Perguntas auxiliares:

```text
Tem algum cliente esperando resposta?
Tem alguma entrega com prazo hoje?
Alguma impressora ou material está bloqueando produção?
```

### 2. Check-in do meio do dia

Horário sugerido:

```text
12:00 a 14:00
```

Objetivo:

- revisar se o plano ainda faz sentido;
- capturar tarefas que surgiram pela manhã;
- reorganizar atrasos;
- reduzir lista se estiver grande demais.

Pergunta principal:

```text
Surgiu algo novo desde cedo?
```

Perguntas auxiliares:

```text
Alguma tarefa mudou de prioridade?
Tem algo que precisa ser feito antes do fim do dia?
Você está travado em alguma tarefa?
```

### 3. Check-in do fim do dia

Horário sugerido:

```text
17:00 a 19:00
```

Objetivo:

- fechar o ciclo do dia;
- capturar pendências finais;
- mandar tarefas para amanhã;
- revisar o que foi ignorado;
- atualizar memória compactada.

Pergunta principal:

```text
Ficou alguma tarefa nova ou pendência para amanhã?
```

Perguntas auxiliares:

```text
O que ficou parado?
O que precisa ser feito primeiro amanhã?
Algum cliente precisa receber resposta?
```

## Formato ideal do check-in

O check-in deve ser curto.

Exemplo:

```text
Check-in rápido
Surgiu alguma tarefa nova?

Botões:
+ Adicionar tarefa
Nada novo
Reorganizar meu dia
Estou travado
```

## Regras de acessibilidade cognitiva

O check-in não deve parecer cobrança pesada.

Evitar:

```text
Você esqueceu de adicionar tarefas?
```

Preferir:

```text
Quer descarregar alguma coisa da cabeça?
```

Ou:

```text
Surgiu algo novo que eu devo guardar?
```

## Integração com IA

Quando o usuário responder de forma bagunçada, a IA revisora deve organizar automaticamente.

Entrada:

```text
tem o cliente joao, comprar filamento, terminar boneco e ver maquete
```

A IA transforma em tarefas com:

- título;
- categoria;
- prioridade;
- prazo sugerido;
- próxima ação;
- alerta sugerido.

## Integração com memória compactada

O app deve aprender com os check-ins.

Exemplos de aprendizado:

```text
O usuário costuma lembrar tarefas novas no meio do dia.
```

```text
O usuário adiciona tarefas demais à noite; mostrar só as 3 mais importantes para amanhã.
```

```text
Check-in da manhã funciona melhor com perguntas de cliente e produção.
```

## Modos do check-in

### Modo normal

Pergunta direta:

```text
Surgiu alguma tarefa nova?
```

### Modo baixa carga mental

Pergunta mais simples:

```text
Quer só jogar algo aqui para eu organizar depois?
```

### Modo reset mental

Quando o usuário está perdido:

```text
Vamos reduzir. Escolha uma área:
Cliente / Produção / Manutenção / Ideias
```

### Modo voz futuro

O usuário fala livremente e a IA organiza.

## Regra importante

O check-in não deve virar uma reunião longa.

Tempo ideal:

```text
30 segundos a 2 minutos
```

## Resultado esperado

Depois de cada check-in, o app deve atualizar:

- caixa de entrada;
- prioridades do dia;
- próxima ação;
- alertas;
- tarefas para amanhã;
- memória compactada, se houver padrão repetido.

## Frase-guia

```text
Não espere a cabeça lembrar. O app pergunta na hora certa.
```
