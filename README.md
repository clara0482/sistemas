# Sistema de Reservas de Restaurantes

Projeto de Programação Orientada a Objetos para reservar mesas em restaurantes, com interface JavaFX e opção de uso pelo console.

## Funcionalidades

- Cadastro de clientes com validação de nome e telefone.
- Consulta de mesas disponíveis por restaurante, data e horário.
- Criação, alteração e cancelamento das próprias reservas.
- Fila de espera com atendimento conforme horário, capacidade e disponibilidade.
- Histórico de reservas e recuperação dos dados ao reiniciar.

Cada reserva dura duas horas. Uma mesa não aceita reservas ativas com períodos sobrepostos. Um telefone existente com nome diferente é rejeitado, e cancelar uma reserva não afeta as reservas de outros clientes.

## Classes principais

| Classes | Função |
|---|---|
| `MainFX` e `Main` | Recebem os dados pela interface gráfica ou pelo console e chamam `SistemaReservas`. |
| `SistemaReservas` | Coordena cadastro, disponibilidade, reservas, cancelamentos e transações. Usa `Repositorio` para consultar o banco. |
| `Cliente`, `Restaurante` e `Mesa` | Representam os clientes e os restaurantes com suas mesas e capacidades. |
| `Reserva` e `StatusReserva` | Associam cliente, mesa, horário e quantidade de pessoas ao estado da reserva. |
| `FilaEspera` e `RegistroFila` | Organizam os pedidos que aguardam uma mesa compatível. |
| `Repositorio` e `HibernateUtil` | Reúnem as consultas e configuram a conexão do Hibernate com o H2. |

## Tecnologias

Java 17 ou superior, JavaFX para a interface, Maven para dependências e compilação, Hibernate/JPA para persistência e H2 como banco local.

## Como executar

Instale JDK 17 ou superior e Maven. Na raiz do repositório:

```text
cd "PROJETO JV/APLICATIVO"
mvn clean package
mvn javafx:run
```

O Maven baixa as dependências; não é necessário instalar um SDK JavaFX separado. Para usar o console, na mesma pasta:

```text
mvn compile exec:java "-Dexec.mainClass=Main"
```

No VS Code, aguarde a importação do Maven e use a configuração de execução de `MainFX` ou `Main`.

## Banco e testes

Execute sempre na pasta `APLICATIVO`: os dados ficam em `dados/reservas.mv.db`. Feche uma execução antes de abrir outra com o mesmo banco. O banco local não é versionado.

No Windows, `H2.bat` abre o console do banco com Java disponível no PATH. Use `jdbc:h2:file:./dados/reservas`, usuário `sa` e senha vazia. Essa configuração é para demonstração local; nome e telefone não oferecem autenticação segura.

Na pasta `APLICATIVO`, execute os testes:

```text
mvn clean verify -Pjavafx-ui-tests
```

O comando verifica as regras, a persistência e a interface JavaFX; requer uma sessão gráfica. Os testes usam H2 temporário, sem alterar o banco local. Para testar sem abrir janelas, use `mvn verify`.

Os fontes ficam em `src/main/java`, os testes em `src/test/java` e as dependências no `pom.xml`.

## Equipe e contribuições

- **Victor Yoshio:** integração das tecnologias externas, incluindo Maven, Hibernate/JPA e H2.
- **Arthur Dantas:** identificação de erros, debugging, testes e auxílio nas correções.
- **Rafael Chada:** construção da estrutura inicial do código e organização das classes.
- **Maria Clara:** desenvolvimento do front-end utilizando JavaFX.
