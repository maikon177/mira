# Laboratório de abordagens

## Objetivo

Permitir que o app teste diferentes formas de notificar e descubra quais funcionam melhor para o usuário.

A IA pode sugerir novas abordagens, mas o app deve controlar quando testar e medir o resultado.

## Ciclo de aprendizado

```text
observar -> criar hipótese -> testar abordagem -> medir resultado -> salvar o que funcionou
```

## Abordagens iniciais

### 1. Direta

```text
Agora faça: responder João.
```

### 2. Motivo financeiro

```text
Responder João agora pode virar venda. Leva 10 minutos.
```

### 3. Passo pequeno

```text
Primeiro passo: abrir a conversa do João e ler o pedido.
```

### 4. Urgência

```text
Essa tarefa está atrasando a entrega de hoje.
```

### 5. Anti-procrastinação

```text
Faça só 5 minutos. Depois você decide se continua.
```

### 6. Checklist técnico

```text
Primeiro passo: verificar se o bico está entupido.
```

## Regras

- Não testar muitas abordagens no mesmo dia.
- Não mandar notificações excessivas.
- Não usar tom agressivo.
- Não transformar tudo em urgente.
- Sempre registrar resultado.
- Se uma abordagem falhar repetidamente, reduzir uso.

## Resultados medidos

- ignorou;
- abriu;
- iniciou;
- concluiu;
- adiou;
- cancelou;
- dividiu.

## Aprendizados esperados

Exemplos:

```text
Tarefas de orçamento funcionam melhor com motivo financeiro.
```

```text
Tarefas de modelagem funcionam melhor com passo pequeno.
```

```text
Tarefas de manutenção funcionam melhor como checklist técnico.
```

## Uso da IA

A IA pode ser chamada para gerar novas mensagens quando:

- uma categoria tem baixa conclusão;
- uma tarefa foi adiada muitas vezes;
- uma abordagem parou de funcionar;
- o usuário pediu uma cobrança diferente.

## Exemplo de prompt interno

```text
O usuário ignorou tarefas de atendimento 4 vezes esta semana. Crie 3 novas abordagens de notificação para aumentar a chance de execução, mantendo tom direto e sem pressão excessiva.
```
