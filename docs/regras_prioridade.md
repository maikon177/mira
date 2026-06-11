# Regras de prioridade

## Objetivo

Definir como o app decide o que deve aparecer como próxima ação.

A prioridade deve ser calculada por regras fixas e pode ser revisada pela IA.

## Critérios principais

### 1. Prazo

Quanto mais próximo o prazo, maior a prioridade.

Regras:

- prazo hoje: prioridade alta;
- prazo amanhã: prioridade média/alta;
- sem prazo: revisão diária;
- prazo vencido: revisão imediata.

### 2. Cliente

Tarefas relacionadas a clientes têm peso maior.

Exemplos:

- responder orçamento;
- enviar foto;
- confirmar medida;
- finalizar pedido;
- entregar peça;
- avisar atraso.

### 3. Dinheiro envolvido

Tarefas que podem gerar venda, evitar perda ou liberar entrega sobem prioridade.

### 4. Produção bloqueada

Se uma tarefa impede produção, ela deve subir.

Exemplos:

- impressora parada;
- falta de filamento;
- peça travada no fatiamento;
- arquivo com erro;
- material sem compra.

### 5. Esforço

Tarefas rápidas com alto impacto devem subir.

Exemplo:

> responder orçamento em 10 minutos pode gerar venda.

### 6. Histórico de adiamento

Se a tarefa foi adiada várias vezes, o app não deve apenas insistir.

Regra:

- 1 adiamento: reagendar;
- 2 adiamentos: reforçar motivo;
- 3 adiamentos: chamar IA para dividir ou reescrever;
- 4+ adiamentos: pedir decisão de cancelar, dividir ou marcar como prioridade crítica.

## Pesos iniciais sugeridos

```text
Prazo: até 30 pontos
Cliente: até 25 pontos
Dinheiro: até 20 pontos
Risco: até 20 pontos
Produção bloqueada: até 25 pontos
Esforço baixo: até 10 pontos
Adiamentos: até 15 pontos
```

## Exemplos

### Responder orçamento de cliente hoje

Prioridade: alta.

Motivo:

- cliente aguardando;
- chance de venda;
- baixo esforço;
- risco de perder oportunidade.

### Testar ideia nova de logo

Prioridade: baixa, salvo se tiver prazo real.

Motivo:

- não bloqueia produção;
- não tem cliente imediato;
- pode virar distração.

### Arrumar impressora parada

Prioridade: alta.

Motivo:

- bloqueia produção;
- pode atrasar entregas;
- afeta dinheiro e prazo.

## Regra central

Quando houver dúvida, priorizar:

```text
cliente + prazo + dinheiro + produção bloqueada
```
