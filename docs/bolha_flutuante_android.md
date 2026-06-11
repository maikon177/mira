# Bolha flutuante estilo gravador de tela

## Origem da ideia

Uma sugestão importante para o projeto foi usar um sistema parecido com aplicativos de gravação de tela: uma bolinha flutuante que fica disponível sobre outras telas.

A função não é vigiar o usuário de forma invasiva. A função é manter o Jarvis acessível, como um atalho permanente para capturar tarefas, ver a próxima ação e abrir o resumo rápido.

## Objetivo

A bolha flutuante deve reduzir o atrito de abrir o app.

Em vez de o usuário precisar procurar o aplicativo, ele toca na bolha e acessa rapidamente:

- próxima ação;
- tarefas de hoje;
- captura rápida de tarefa;
- chat com IA;
- reset mental;
- estacionamento de ideias.

## Por que isso é importante

Para pessoas com dificuldade de foco, TDAH, sobrecarga mental ou rotina fragmentada, abrir um aplicativo pode ser uma barreira.

A bolha cria um ponto fixo de retorno.

Ela funciona como:

```text
atalho visual constante -> captura rápida -> volta ao foco
```

## Comportamento ideal

A bolha deve ser discreta.

Ela não deve:

- piscar o tempo todo;
- bloquear a tela;
- atrapalhar outros apps;
- parecer alerta urgente constante;
- consumir muita bateria;
- ler tela sem permissão clara.

Ela deve:

- ficar em uma lateral da tela;
- ser movível;
- abrir painel rápido ao tocar;
- permitir esconder temporariamente;
- ter modo discreto durante trabalho;
- aparecer com mais força em horários livres.

## Painel ao tocar na bolha

Ao tocar na bolha, abrir um painel pequeno com:

```text
Agora:
Responder João — 10 min

Hoje:
1. Responder João
2. Verificar Ender
3. Abrir arquivo da maquete

Ações:
Começar
Adicionar tarefa
Falar com IA
Reset mental
```

## Botões principais

- Começar próxima ação;
- Adicionar tarefa rápida;
- Abrir chat com IA;
- Ver tarefas de hoje;
- Guardar ideia para depois;
- Reset mental;
- Pausar Jarvis.

## Captura rápida

O usuário pode tocar na bolha e dizer ou digitar:

```text
cliente novo pediu orçamento
```

O app salva na caixa de entrada e a IA organiza depois.

## Chat com IA pela bolha

A bolha deve permitir abrir um chat pequeno com a IA.

Uso esperado:

```text
Estou perdido, o que faço agora?
```

Ou:

```text
surgiu cliente novo, comprar resina e terminar maquete
```

A IA responde com baixa carga mental:

```text
Vou organizar. Agora faça só isso: responder cliente novo.
```

## Estacionamento de ideias

Se o usuário tiver uma ideia enquanto está focado, a bolha permite salvar sem sair da tarefa.

Exemplo:

```text
Ideia salva: criar novo modelo de logo.
Revisão: hoje às 18:00.
Voltar para: responder João.
```

## Privacidade e segurança

A bolha não deve ler a tela por padrão.

Se no futuro houver recurso de leitura de tela ou contexto visual, isso deve ser:

- opcional;
- explicado claramente;
- desligado por padrão;
- dependente de permissão explícita;
- configurável pelo usuário.

Para o MVP, a bolha deve ser apenas um atalho flutuante, não um sistema de monitoramento de tela.

## Implementação técnica Android

Possibilidades:

### 1. Overlay com permissão especial

Usar permissão de sobrepor outros apps.

Vantagem:

- funciona parecido com apps de gravação de tela.

Desvantagem:

- exige permissão sensível;
- precisa UX clara para o usuário entender;
- pode ter restrições em algumas versões do Android.

### 2. Bubble API do Android

Usar recurso nativo de bolhas quando aplicável.

Vantagem:

- mais integrado ao sistema.

Desvantagem:

- pode ser mais limitado dependendo do uso.

### 3. Notificação persistente primeiro

Para MVP, pode ser mais simples começar com notificação persistente e depois evoluir para bolha.

## Prioridade recomendada

### MVP

- notificação persistente;
- ações rápidas;
- abrir app pela notificação.

### Versão 2

- bolha lateral flutuante;
- painel rápido;
- adicionar tarefa pela bolha;
- abrir chat com IA.

### Versão 3

- modo avançado de contexto;
- leitura de tela opcional, se houver motivo real;
- automações mais profundas.

## Regra de produto

A bolha deve ser um ponto de retorno ao foco, não uma distração nova.

Frase-guia:

```text
A bolha existe para descarregar a cabeça sem abandonar o que estava fazendo.
```
