# Sistemas

Projeto em grupo POO. Este guia explica, como utilizar a branch.


**Ninguém mexe direto na `main`.** Cada pessoa trabalha na sua própria branch (uma "cópia paralela" do projeto) e depois juntamos tudo.

---

## 1. Baixar o projeto (só na primeira vez)

Abra o terminal e rode:

```bash
git clone https://github.com/clara0482/sistemas.git
cd sistemas
code .
```

O último comando abre a pasta no VS Code.

## 2. Criar a sua branch (só na primeira vez)

Troque `seu-nome` pelo seu nome:

```bash
git checkout -b seu-nome
```

Exemplo: `git checkout -b clara`

> Pelo VS Code: clique em `main` no canto inferior esquerdo, escolha **Create new branch...**, digite seu nome e aperte Enter.

## 3. Trabalhar no dia a dia

**Antes de começar**, atualize sua branch com o que os outros já fizeram:

```bash
git checkout main
git pull origin main
git checkout seu-nome
git merge main
```

**Depois de editar os arquivos**, salve e envie:

```bash
git add .
git commit -m "escreva aqui o que você fez"
git push -u origin seu-nome
```

Nos próximos envios, basta usar `git push`.

## 4. Juntar seu trabalho na main

1. Entre no repositório no GitHub.
2. Clique no botão **Compare & pull request**.
3. Clique em **Create pull request**.
4. Outra pessoa do grupo confere e clica em **Merge pull request**.

---

## Comandos úteis

| Comando | O que faz |
|---|---|
| `git status` | Mostra em qual branch você está e o que mudou |
| `git branch` | Lista as branches |
| `git checkout nome` | Muda para outra branch |
| `git pull` | Baixa as novidades do GitHub |
| `git log --oneline` | Mostra o histórico de commits |

## Dicas para o grupo

- Deu erro ou conflito? Não force o envio. Peça ajuda ao grupo.

## Integrantes

| Nome |

Maria Clara
Rafael Chada
Victor Yoshio
Arthus Dantas
