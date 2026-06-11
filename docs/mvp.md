# MVP — Primeira versão do Jarvis Tarefas

## Objetivo do MVP

Criar uma primeira versão simples que já resolva o problema principal: ajudar o usuário a decidir **o que fazer agora**, sem depender de uma lista gigante de tarefas nem de notificações comuns fáceis de ignorar.

## Princípio do MVP

O app deve funcionar mesmo sem IA.

A IA será usada como revisora e organizadora, mas as tarefas, status, histórico, alertas e regras básicas precisam existir dentro do próprio app.

## Funções obrigatórias

### 1. Cadastro rápido de tarefa

Campos mínimos:

- título;
- descrição;
- categoria;
- prazo;
- prioridade manual;
- tempo estimado;
- status.

### 2. Caixa de entrada

Local onde o usuário joga tarefas soltas sem precisar organizar na hora.

Exemplo:

> responder cliente João, comprar filamento, arrumar bico da Ender, terminar maquete.

A IA pode revisar depois e transformar em tarefas melhores.

### 3. Tela Agora

Tela central do app.

Ela deve mostrar apenas uma próxima ação.

Exemplo:

> Agora: responder cliente João sobre o orçamento da maquete. Leva 10 minutos e pode virar venda.

Botões:

- iniciar;
- concluir;
- adiar 15 minutos;
- adiar 1 hora;
- dividir;
- cancelar;
- justificar.

### 4. Tela Hoje

Mostrar no máximo 3 a 5 prioridades do dia.

Regra: evitar lista grande.

### 5. Notificações decisivas

A notificação não deve apenas avisar. Ela precisa pedir uma decisão.

Exemplo ruim:

> Fazer orçamento.

Exemplo bom:

> Agora: responder orçamento do João. Leva 10 minutos. Pode virar venda. Iniciar / Adiar / Dividir / Cancelar.

### 6. Histórico básico

Registrar:

- tarefa criada;
- tarefa iniciada;
- tarefa concluída;
- tarefa adiada;
- tarefa cancelada;
- alerta enviado;
- alerta ignorado.

### 7. IA revisora simples

A IA deve:

- organizar tarefa bagunçada;
- sugerir categoria;
- sugerir prioridade;
- dividir tarefa grande;
- sugerir próxima ação.

### 8. Memória compactada simples

Salvar aprendizados como:

- usuário conclui melhor tarefas de 10 a 30 minutos;
- usuário ignora tarefas vagas;
- tarefas de cliente devem subir prioridade;
- listas longas devem ser evitadas.

## O que não entra no MVP

Evitar no começo:

- integração com WhatsApp;
- comando por voz avançado;
- dashboard complexo;
- muitas telas;
- automações pesadas;
- IA rodando o tempo todo;
- sincronização complicada demais.

## Resultado esperado

O MVP deve responder bem a esta pergunta:

> Qual é a próxima ação mais importante agora?
