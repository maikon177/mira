# TODO — Jarvis Tarefas Pata 3D

## Ordem recomendada de execução

### 1. Fechar stack inicial

- [x] Escolher entre PWA, app Android nativo ou web primeiro.
- [x] Escolher backend: Node.js, Python FastAPI ou Supabase Functions.
- [x] Escolher banco: Supabase/PostgreSQL, Firebase ou SQLite local.
- [x] Definir se o MVP terá login ou uso local.

### 2. Testar API DeepSeek

- [x] Criar arquivo `.env` local com `DEEPSEEK_API_KEY`.
- [x] Criar função de chamada da DeepSeek.
- [x] Enviar uma entrada bagunçada de tarefa.
- [x] Receber resposta em JSON.
- [x] Validar se o JSON é confiável.

### 3. Criar base de tarefas

- [x] Criar modelo de tarefa.
- [x] Criar status de tarefa.
- [x] Criar categorias iniciais.
- [x] Criar histórico de eventos.
- [x] Criar cálculo simples de prioridade.

### 4. Criar fluxo principal

- [x] Criar Caixa de Entrada.
- [x] Criar Tela Agora.
- [x] Criar Tela Hoje.
- [x] Criar ação de concluir.
- [x] Criar ação de adiar.
- [x] Criar ação de cancelar.
- [ ] Criar ação de dividir tarefa.

### 5. Criar IA revisora

- [x] Usar prompt de `prompts/ia_revisora.md`.
- [x] Criar função `reviewTaskInput(input)`.
- [x] Retornar tarefas estruturadas.
- [x] Sugerir próxima ação.
- [x] Sugerir alerta.

### 6. Criar notificações decisivas

- [x] Criar modelo de notificação.
- [x] Criar botão iniciar.
- [x] Criar botão concluir.
- [x] Criar botão adiar.
- [ ] Criar botão dividir.
- [x] Registrar resultado da notificação.

### 7. Criar memória compactada

- [x] Registrar padrões simples.
- [x] Detectar tarefa vaga.
- [x] Detectar tarefa adiada várias vezes.
- [x] Salvar memória manual.
- [x] Permitir editar/apagar memória.

### 8. Criar laboratório de abordagens

- [x] Criar estratégias de alerta.
- [x] Medir resposta do usuário.
- [x] Comparar abordagem direta, financeira, passo pequeno e urgência.
- [x] Salvar estratégia vencedora por categoria.

## Regra principal

Não transformar o MVP em um projeto gigante.

Validar primeiro este fluxo:

```text
entrada bagunçada -> tarefa organizada -> prioridade -> próxima ação -> decisão -> histórico
```
