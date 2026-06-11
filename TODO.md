# TODO — Jarvis Tarefas Pata 3D

## Ordem recomendada de execução

### 1. Fechar stack inicial

- [ ] Escolher entre PWA, app Android nativo ou web primeiro.
- [ ] Escolher backend: Node.js, Python FastAPI ou Supabase Functions.
- [ ] Escolher banco: Supabase/PostgreSQL, Firebase ou SQLite local.
- [ ] Definir se o MVP terá login ou uso local.

### 2. Testar API DeepSeek

- [ ] Criar arquivo `.env` local com `DEEPSEEK_API_KEY`.
- [ ] Criar função de chamada da DeepSeek.
- [ ] Enviar uma entrada bagunçada de tarefa.
- [ ] Receber resposta em JSON.
- [ ] Validar se o JSON é confiável.

### 3. Criar base de tarefas

- [ ] Criar modelo de tarefa.
- [ ] Criar status de tarefa.
- [ ] Criar categorias iniciais.
- [ ] Criar histórico de eventos.
- [ ] Criar cálculo simples de prioridade.

### 4. Criar fluxo principal

- [ ] Criar Caixa de Entrada.
- [ ] Criar Tela Agora.
- [ ] Criar Tela Hoje.
- [ ] Criar ação de concluir.
- [ ] Criar ação de adiar.
- [ ] Criar ação de cancelar.
- [ ] Criar ação de dividir tarefa.

### 5. Criar IA revisora

- [ ] Usar prompt de `prompts/ia_revisora.md`.
- [ ] Criar função `reviewTaskInput(input)`.
- [ ] Retornar tarefas estruturadas.
- [ ] Sugerir próxima ação.
- [ ] Sugerir alerta.

### 6. Criar notificações decisivas

- [ ] Criar modelo de notificação.
- [ ] Criar botão iniciar.
- [ ] Criar botão concluir.
- [ ] Criar botão adiar.
- [ ] Criar botão dividir.
- [ ] Registrar resultado da notificação.

### 7. Criar memória compactada

- [ ] Registrar padrões simples.
- [ ] Detectar tarefa vaga.
- [ ] Detectar tarefa adiada várias vezes.
- [ ] Salvar memória manual.
- [ ] Permitir editar/apagar memória.

### 8. Criar laboratório de abordagens

- [ ] Criar estratégias de alerta.
- [ ] Medir resposta do usuário.
- [ ] Comparar abordagem direta, financeira, passo pequeno e urgência.
- [ ] Salvar estratégia vencedora por categoria.

## Regra principal

Não transformar o MVP em um projeto gigante.

Validar primeiro este fluxo:

```text
entrada bagunçada -> tarefa organizada -> prioridade -> próxima ação -> decisão -> histórico
```
