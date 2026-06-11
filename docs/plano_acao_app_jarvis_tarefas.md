# Plano de ação — App assistente operacional tipo Jarvis

Projeto: assistente pessoal de foco, tarefas, alertas inteligentes e IA revisora.

## 1. Diagnóstico

O problema principal não é falta de agenda. O problema real é que agendas e notificações comuns apenas avisam, mas não ajudam a decidir prioridade, não revisam atrasos e são fáceis de ignorar.

O app precisa resolver:

- excesso de tarefas soltas;
- dificuldade de escolher o que fazer agora;
- notificações comuns sem força;
- tarefas vagas demais;
- foco indo para tarefas menos importantes;
- falta de revisão automática;
- falta de aprendizado sobre o comportamento do usuário.

## 2. Conceito

O app deve ser um gerenciador de tarefas com IA revisora, memória compactada e notificações decisivas.

A frase central do projeto é:

> Mostrar a próxima melhor ação, não uma lista gigante.

## 3. Estrutura correta

O app deve ter duas camadas.

### Camada fixa

Funciona mesmo sem IA:

- tarefas;
- status;
- calendário;
- alertas;
- histórico;
- regras automáticas;
- banco de dados;
- notificações.

### Camada de IA

Usada para melhorar o sistema:

- organizar tarefas bagunçadas;
- revisar tarefas vagas;
- sugerir prioridade;
- dividir tarefas grandes;
- replanejar o dia;
- criar memória compactada;
- testar novas abordagens de alerta.

## 4. Telas principais

### Caixa de entrada

Entrada rápida para jogar tarefas sem organizar na hora.

### Agora

Tela principal. Mostra apenas a próxima ação recomendada.

Exemplo:

> Responder cliente João sobre orçamento. Leva 10 minutos e pode virar venda.

Botões:

- iniciar;
- concluir;
- adiar;
- dividir;
- cancelar;
- justificar.

### Hoje

Mostra no máximo 3 a 5 prioridades.

### Projetos

Organiza trabalhos maiores, como maquete, linha de bonecos, manutenção, catálogo ou app.

### Revisão do dia

Mostra o que foi concluído, o que atrasou e o que deve ir para amanhã.

### Memória do usuário

Mostra o que o app aprendeu e permite editar ou apagar memórias.

## 5. Status das tarefas

Status recomendados:

- entrada;
- planejada;
- em andamento;
- pausada;
- concluída;
- atrasada;
- cancelada;
- aguardando.

## 6. Campos de cada tarefa

Campos iniciais:

- título;
- descrição;
- categoria;
- projeto;
- cliente;
- prazo;
- prioridade manual;
- prioridade calculada;
- tempo estimado;
- tempo real;
- status;
- número de adiamentos;
- data de criação;
- data de conclusão;
- último alerta;
- próxima revisão;
- motivo de atraso;
- subtarefas;
- observações.

## 7. Prioridade

A prioridade deve considerar:

- prazo;
- dinheiro envolvido;
- risco;
- dependência;
- tempo estimado;
- categoria;
- histórico de adiamentos.

Regra importante:

> Cliente + prazo hoje = prioridade alta.

## 8. Notificações inteligentes

Notificação comum:

> Fazer orçamento.

Notificação ideal:

> Agora: responder cliente João. Leva 10 minutos. Pode virar venda. Iniciar / Adiar / Dividir / Cancelar.

A notificação precisa pedir decisão.

## 9. Memória compactada

O app deve aprender sem treinar uma IA do zero.

Ele deve compactar padrões como:

- o usuário conclui melhor tarefas de 10 a 30 minutos;
- o usuário ignora tarefas vagas;
- tarefas de cliente precisam subir prioridade;
- listas longas devem ser evitadas;
- tarefas técnicas funcionam melhor como checklist;
- ideias novas devem ir para revisão, não para prioridade imediata.

## 10. Testes de abordagem

A IA pode criar abordagens diferentes de alerta e o app mede o resultado.

Exemplos:

- abordagem direta;
- motivo financeiro;
- passo pequeno;
- urgência;
- anti-procrastinação;
- checklist técnico.

O app deve medir:

- ignorou;
- abriu;
- iniciou;
- concluiu;
- adiou;
- cancelou;
- dividiu.

## 11. MVP

Primeira versão recomendada:

- cadastro rápido de tarefa;
- caixa de entrada;
- tela Agora;
- tela Hoje;
- notificações com botões;
- histórico básico;
- IA revisora simples;
- memória compactada simples.

Não colocar no MVP:

- WhatsApp;
- voz avançada;
- dashboard grande;
- automações complexas;
- muitas telas;
- IA rodando toda hora.

## 12. Próximas etapas

1. Definir tecnologia inicial.
2. Criar banco de dados.
3. Criar protótipo de telas.
4. Implementar tarefas e status.
5. Implementar tela Agora.
6. Implementar IA revisora.
7. Implementar notificações decisivas.
8. Implementar memória compactada.
9. Implementar laboratório de abordagens.

## 13. Decisão recomendada

Começar com um MVP simples, focado em uma pergunta:

> Qual é a próxima ação mais importante agora?

Esse é o núcleo do projeto.