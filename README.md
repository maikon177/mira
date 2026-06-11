# Jarvis Tarefas Pata 3D

Assistente operacional pessoal para foco, tarefas, notificações inteligentes e IA revisora.

## Objetivo

Criar um app para celular e PC que ajude a decidir **qual é a próxima melhor ação agora**, evitando lista gigante de tarefas e notificações fáceis de ignorar.

O app não deve depender 100% da IA. A IA entra como camada revisora, organizadora e adaptativa por cima de um sistema fixo de tarefas, alertas, status, histórico e regras.

## Conceito central

> O sistema deve reduzir decisão, não criar mais uma lista de coisas para olhar.

## Funções principais

- Captura rápida de tarefas.
- Tela **Agora** com uma única próxima ação.
- Tela **Hoje** com no máximo 3 a 5 prioridades.
- Notificações com botões de decisão.
- IA revisora para organizar, priorizar e dividir tarefas.
- Memória compactada para aprender com o comportamento do usuário.
- Testes de abordagem para descobrir quais alertas funcionam melhor.

## Estrutura inicial

```text
docs/
  plano_acao_app_jarvis_tarefas.md
  mvp.md
  arquitetura.md
  roadmap.md
prompts/
  ia_revisora.md
database/
  schema_inicial.md
```

## MVP recomendado

Primeira versão:

- Cadastro de tarefa.
- Caixa de entrada.
- Tela Agora.
- Tela Hoje.
- Notificação com ações.
- Histórico básico.
- IA revisora simples.
- Memória compactada simples.

## Visão

Transformar o app em um assistente estilo Jarvis, mas com base prática:

- o app salva e executa regras;
- a IA revisa e melhora o plano;
- o histórico mostra o que funciona;
- a memória compactada adapta o sistema ao usuário.
