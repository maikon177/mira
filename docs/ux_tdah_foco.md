# UX para foco, TDAH e acessibilidade cognitiva

## Objetivo

Este documento define uma direção mais crítica para a interface do Jarvis Tarefas Pata 3D.

O app não deve ser apenas bonito. Ele deve ser projetado para ajudar uma pessoa com dificuldade de foco, excesso de tarefas, impulsividade de troca de assunto, procrastinação e sobrecarga mental.

O objetivo não é diagnosticar o usuário. O objetivo é criar uma interface mais amigável para pessoas neurodivergentes, pessoas com TDAH, ansiedade de tarefa, sobrecarga cognitiva ou rotina operacional muito fragmentada.

## Crítica ao mockup inicial

O mockup inicial mostra bem:

- tela Agora;
- próxima ação;
- botões rápidos;
- resumo;
- lista curta;
- bolha lateral.

Mas ainda parece um app comum de produtividade.

Problemas do mockup inicial:

- ainda mostra informação demais na tela;
- a lista Hoje pode virar distração;
- falta modo de transição para começar a tarefa;
- falta reduzir ansiedade antes da ação;
- falta proteger o usuário de ideias novas no meio do foco;
- falta acessibilidade sensorial;
- falta um sistema visual que entenda o estado mental do usuário.

## Nova direção de design

O app deve funcionar como um regulador de atenção.

Em vez de perguntar apenas:

```text
O que você precisa fazer?
```

Ele deve responder:

```text
O que sua cabeça consegue começar agora sem travar?
```

## Princípios principais

### 1. Uma decisão por tela

Cada tela deve pedir apenas uma decisão.

Exemplo:

```text
Começar agora?
Adiar?
Dividir?
Cancelar?
```

Evitar mostrar muitas opções ao mesmo tempo.

### 2. Tarefa grande vira primeiro passo

Nunca mostrar uma tarefa grande como ação principal.

Ruim:

```text
Terminar maquete.
```

Melhor:

```text
Abrir o arquivo da maquete e olhar qual é o próximo erro.
```

### 3. Reduzir atrito para começar

O app deve evitar comandos grandes como "trabalhar", "resolver" ou "finalizar".

Preferir verbos pequenos:

- abrir;
- olhar;
- responder;
- separar;
- salvar;
- conferir;
- iniciar;
- enviar.

### 4. Proteger contra fuga de foco

Quando o usuário tenta abrir uma ideia nova durante uma tarefa importante, o app deve oferecer um estacionamento de ideias.

Exemplo:

```text
Ideia salva para revisar às 18:00.
Agora volte para: responder João.
```

### 5. Mostrar motivo sem culpa

A notificação não deve usar pressão agressiva.

Ruim:

```text
Você está atrasado de novo.
```

Melhor:

```text
Essa tarefa voltou porque destrava uma venda. Comece por 5 minutos.
```

### 6. Modo baixa carga mental

O app deve ter um modo com interface simplificada:

- menos texto;
- menos botões;
- fonte maior;
- contraste confortável;
- sem animação excessiva;
- uma ação principal.

### 7. Modo crise de bagunça

Quando houver tarefas demais, o app não deve mostrar tudo.

Ele deve dizer:

```text
Tem coisa demais aqui. Vou escolher 3 blocos:
1. Cliente
2. Produção
3. Material
```

E depois perguntar:

```text
Quer começar por cliente agora?
```

### 8. Modo aquecimento

Antes de tarefa difícil, mostrar um passo de entrada.

Exemplo:

```text
Aquecimento de 2 minutos:
Abra a conversa do João.
Não precisa responder ainda.
```

Depois:

```text
Agora escreva só a primeira frase.
```

### 9. Modo foco guiado

Durante uma tarefa, mostrar apenas:

- tarefa atual;
- tempo curto;
- próximo micro-passo;
- botão de pausa;
- botão de concluir.

Exemplo:

```text
Foco 10 min
Tarefa: responder João
Passo: escrever valor e prazo
```

### 10. Resumo emocionalmente neutro

No fim do dia, evitar julgamento.

Ruim:

```text
Você falhou em 4 tarefas.
```

Melhor:

```text
Hoje você concluiu 3 tarefas. 2 ficaram abertas e já foram reorganizadas para amanhã.
```

## Novas telas recomendadas

### Tela Agora 2.0

A tela principal deve ter:

- uma ação principal;
- motivo curto;
- micro-passo;
- botão grande de iniciar;
- botão secundário de dividir;
- botão discreto de adiar.

A lista Hoje deve ficar escondida ou minimizada para não competir com a ação principal.

### Tela Aquecimento

Antes de começar tarefa travada.

Exemplo:

```text
Só 2 minutos
Abra o arquivo.
Não precisa resolver tudo.
```

### Tela Foco Guiado

Depois de iniciar.

Exemplo:

```text
10 minutos de foco
Próximo passo: conferir medidas da maquete
```

### Tela Estacionamento de Ideias

Para não perder ideias novas sem abandonar a tarefa principal.

Exemplo:

```text
Ideia salva: criar logo nova
Revisão: hoje às 18:00
Voltar para: responder João
```

### Tela Reset Mental

Quando o usuário se perde.

Exemplo:

```text
Respira. Vamos reduzir.
Escolha uma:
1. Cliente
2. Produção
3. Manutenção
```

## Notificação persistente revisada

A notificação deve ser calma, objetiva e útil.

Exemplo:

```text
Jarvis: próxima ação
Responder João • 10 min
Começar / Adiar / Dividir
```

Evitar excesso de texto na notificação.

## Bolha lateral revisada

A bolha lateral deve ser discreta.

Ela não deve piscar nem chamar atenção o tempo todo.

Ao tocar, mostrar:

```text
Agora: responder João
Depois: verificar Ender
Ideias novas: 2 salvas para revisar depois
```

Botões:

- começar;
- salvar ideia;
- resetar plano;
- abrir app.

## Acessibilidade sensorial

Configurações recomendadas:

- reduzir animações;
- aumentar fonte;
- modo alto contraste;
- modo menos contraste;
- modo sem sons;
- modo vibração leve;
- modo notificação calma;
- modo notificação firme;
- modo foco sem lista.

## Acessibilidade cognitiva

O app deve evitar:

- telas longas;
- excesso de opção;
- termos vagos;
- mensagens com culpa;
- notificações repetidas sem mudança;
- exigir memória do usuário;
- obrigar o usuário a reorganizar tudo manualmente.

O app deve oferecer:

- instruções curtas;
- passos pequenos;
- botões grandes;
- labels claros;
- consistência visual;
- reversão fácil;
- ajuda contextual;
- resumo automático.

## Regras de escrita da interface

Usar frases curtas.

Preferir:

```text
Comece por 5 minutos.
```

Evitar:

```text
Você precisa finalizar essa tarefa atrasada o quanto antes.
```

Preferir:

```text
Abrir arquivo.
```

Evitar:

```text
Resolver problema do modelo.
```

## Modo de aprendizado humano

O app deve aprender sem pressionar.

Exemplo:

```text
Percebi que você conclui melhor quando a tarefa começa com um passo de 5 minutos. Quer usar isso como padrão?
```

O usuário pode:

- aceitar;
- editar;
- recusar;
- apagar depois.

## Métrica principal

Não medir apenas quantidade de tarefas concluídas.

Medir:

- quantas tarefas importantes foram iniciadas;
- quantas tarefas travadas foram destravadas;
- quantas vezes a pessoa voltou ao foco;
- quais notificações realmente ajudaram;
- quais telas reduziram adiamento.

## Nova frase-guia do produto

```text
Menos cobrança. Mais direção.
```

## Conclusão

A interface precisa ser pensada para uma mente real em dia real, não para uma pessoa perfeitamente organizada.

O app deve aceitar que o usuário se distrai, atrasa, muda de foco e esquece. O trabalho do Jarvis é reorganizar sem julgamento e trazer o usuário de volta para uma próxima ação pequena e possível.
