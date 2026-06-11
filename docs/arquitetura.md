# Arquitetura do projeto

## Visão geral

O app deve ser construído em duas camadas:

1. **Camada fixa do sistema**
   - tarefas;
   - status;
   - alertas;
   - histórico;
   - regras automáticas;
   - banco de dados;
   - notificações.

2. **Camada de IA**
   - revisão;
   - organização;
   - priorização;
   - divisão de tarefas;
   - memória compactada;
   - teste de novas abordagens.

A IA não deve controlar tudo. Ela deve revisar, sugerir e melhorar o sistema.

## Arquitetura recomendada para começar

### Frontend mobile

Primeira opção:

- Android nativo; ou
- PWA responsivo para funcionar no celular e PC.

### Painel PC

Uma aplicação web simples para organizar tarefas com mais conforto.

### Backend

Opções recomendadas:

- Node.js;
- Python FastAPI;
- Supabase Edge Functions;
- Firebase Functions.

### Banco de dados

Opções:

- Supabase/PostgreSQL para projeto com sincronização;
- Firebase para app mobile rápido;
- SQLite para protótipo local.

Recomendação inicial: **Supabase**, por facilitar banco, autenticação e API.

## Módulos principais

### Módulo de tarefas

Responsável por criar, editar, concluir, adiar e cancelar tarefas.

### Módulo de prioridade

Calcula prioridade com base em:

- prazo;
- cliente;
- dinheiro envolvido;
- risco;
- dependência;
- esforço;
- histórico de adiamentos.

### Módulo de notificações

Envia alertas com botões de decisão.

### Módulo de IA revisora

Chama a API de IA em momentos estratégicos:

- criação de tarefa;
- começo do dia;
- fim do dia;
- tarefa adiada várias vezes;
- tarefa atrasada;
- pedido manual do usuário.

### Módulo de memória compactada

Salva aprendizados úteis sobre o usuário.

### Módulo de laboratório de abordagens

Testa diferentes formas de alerta e mede o que funciona melhor.

## Fluxo de dados

1. Usuário cadastra tarefa.
2. App salva no banco.
3. Regra fixa calcula prioridade inicial.
4. IA pode revisar e melhorar a tarefa.
5. App mostra próxima ação.
6. Usuário interage com a notificação.
7. App registra evento.
8. Memória compactada é atualizada em revisões periódicas.

## Regra de economia da API

Não enviar histórico inteiro para a IA.

Enviar apenas:

- tarefa atual;
- tarefas abertas mais importantes;
- memória compactada;
- regras principais;
- contexto do dia.
