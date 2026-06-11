# Interface do aplicativo

## Objetivo da interface

A interface não é apenas estética. Neste projeto, ela faz parte da função principal: reduzir decisão e facilitar ação rápida.

O app deve ser visualmente agradável, direto e difícil de ignorar sem ser irritante.

## Princípios visuais

- Mostrar pouca informação por vez.
- Priorizar a próxima ação.
- Usar contraste forte para o que precisa ser feito agora.
- Evitar lista gigante.
- Permitir ações rápidas sem abrir várias telas.
- Ter aparência moderna, escura, limpa e tecnológica.

## Estilo sugerido

Tema principal:

```text
Dark mode tecnológico
```

Cores sugeridas:

```text
Fundo principal: #07111F
Cards: #0F172A
Cards internos: #111827
Azul destaque: #0EA5E9
Ciano destaque: #22D3EE
Roxo IA: #8B5CF6
Verde ação: #22C55E
Laranja adiamento: #F59E0B
Texto principal: #F8FAFC
Texto secundário: #CBD5E1
Texto apagado: #94A3B8
```

## Telas principais

### 1. Tela Agora

Esta é a tela mais importante do app.

Deve mostrar apenas uma próxima ação.

Elementos:

- título da tarefa;
- motivo da prioridade;
- tempo estimado;
- impacto;
- botões rápidos.

Botões:

- iniciar;
- concluir;
- adiar;
- dividir;
- cancelar;
- justificar.

Exemplo:

```text
Próxima ação:
Responder orçamento do João

Motivo:
Cliente aguardando, leva 10 minutos e pode virar venda.
```

### 2. Tela Hoje

Mostra no máximo 3 a 5 prioridades.

Não deve virar lista infinita.

### 3. Caixa de entrada

Local para jogar tarefas soltas.

### 4. Resumo Jarvis

Resumo rápido com:

- próxima ação;
- tarefas atrasadas;
- alerta de cliente;
- produção bloqueada;
- revisão do dia.

## Barra de notificação persistente

O app deve ter um modo de notificação persistente no Android.

Objetivo:

- manter o Jarvis visível;
- mostrar resumo rápido;
- permitir ações sem abrir o app;
- lembrar o usuário da próxima ação.

Exemplo de texto:

```text
Jarvis ativo
Hoje: 3 prioridades • 1 atrasada
Agora: responder João
```

Ações rápidas:

- iniciar;
- adiar;
- concluir;
- abrir resumo.

Observação técnica:

Esse recurso provavelmente deve ser implementado como notificação persistente/foreground service no Android. Deve ser usado com cuidado para não consumir bateria nem incomodar.

## Bolha lateral

O app pode ter uma bolha lateral flutuante.

Objetivo:

- abrir o resumo sem procurar o app;
- mostrar próxima ação rapidamente;
- funcionar como atalho estilo assistente;
- reduzir esquecimento.

Ao tocar na bolha, abrir um painel pequeno com:

- próxima ação;
- tarefa atrasada;
- alerta de produção;
- botão para abrir tela Agora.

Exemplo:

```text
Resumo Jarvis
• Próxima: responder João
• Atrasada: revisar maquete
• Máquina: verificar Ender
• Ideias novas: guardar para noite
```

Observação técnica:

A bolha pode ser feita de duas formas:

1. usando recurso de bolhas do Android quando aplicável;
2. usando sobreposição/overlay com permissão especial, se for necessário.

Para MVP, a bolha pode ficar para a segunda etapa, porque exige mais cuidado técnico e permissão do usuário.

## Prioridade de implementação

### MVP

- Tela Agora;
- Tela Hoje;
- Caixa de entrada;
- Notificação com ações;
- Resumo simples dentro do app.

### Versão 2

- notificação persistente;
- widget na tela inicial;
- resumo rápido;
- bolha lateral.

### Versão 3

- painel flutuante completo;
- comando por voz;
- integração com PC;
- modo foco visual.

## Recomendação

A interface deve ser pensada como parte do sistema de execução.

O visual bonito ajuda, mas o mais importante é que cada tela faça o usuário tomar uma decisão rápida.
