# Prompt para iniciar no Claude

Use este prompt quando for pedir para o Claude continuar o projeto.

```text
Leia primeiro os arquivos README.md, CLAUDE.md, TODO.md, DECISIONS.md, docs/mvp.md, docs/arquitetura.md e prompts/ia_revisora.md.

Depois faça apenas uma análise inicial do projeto, sem programar ainda.

Quero que você:

1. confirme se entendeu o objetivo do app;
2. proponha a stack mais simples para criar o MVP;
3. explique por que essa stack é adequada;
4. liste os primeiros arquivos de código que devem ser criados;
5. proponha a ordem de implementação;
6. aponte riscos técnicos;
7. preserve a regra principal: o app não pode depender 100% da IA.

Não crie código ainda. Primeiro quero o plano técnico de execução.
```

## Prompt para quando for programar

```text
Agora implemente a primeira etapa do MVP.

Regras:

- siga o CLAUDE.md;
- use as decisões do DECISIONS.md;
- não coloque API key no código;
- use `.env` local;
- mantenha o app simples;
- crie código pronto para rodar;
- explique como testar;
- faça uma etapa por vez.
```
