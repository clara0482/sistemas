# Sistema de Reservas de Restaurantes

Projeto de Programação Orientada a Objetos para reservar mesas em restaurantes pelo console ou pela interface JavaFX. Usa Java 17 ou superior, Maven, Hibernate/JPA e banco H2 local.

## Funcionalidades

- Cadastro de clientes com validação de nome e telefone.
- Consulta de mesas disponíveis por restaurante, data e horário.
- Criação, alteração e cancelamento das próprias reservas.
- Fila de espera quando não há mesa compatível disponível.

As reservas duram duas horas. Uma mesa não pode ter reservas ativas com períodos sobrepostos. O mesmo telefone com nome diferente é rejeitado, e cancelar uma reserva não afeta reservas de outros clientes.

## Como executar

Instale JDK 17 ou superior e Maven. Na raiz do repositório, entre na pasta do aplicativo:

```text
cd "PROJETO JV/APLICATIVO"
mvn clean package
mvn javafx:run
```

O Maven baixa as dependências; não é necessário instalar um SDK JavaFX separado. Para executar pelo console, na mesma pasta:

```text
mvn compile exec:java "-Dexec.mainClass=Main"
```

No VS Code, aguarde a importação do projeto Maven e use as configurações de execução de `MainFX` ou `Main`.

## Banco H2

Execute sempre na pasta `APLICATIVO`. Os dados são salvos automaticamente em `dados/reservas.mv.db` e recuperados ao reiniciar. Feche uma execução antes de abrir outra com o mesmo banco.

Para inspecionar o banco no Windows, use `H2.bat` com Java disponível no PATH. No console H2, informe a URL `jdbc:h2:file:./dados/reservas`, usuário `sa` e senha vazia. Essa configuração é para demonstração local. Nome e telefone identificam o cadastro, mas não oferecem autenticação segura.

O banco local não é versionado. Para fazer backup, feche o aplicativo e copie o arquivo do banco para um local separado.

## Testes

Na pasta `APLICATIVO`:

```text
mvn clean verify -Pjavafx-ui-tests
```

Esse comando verifica as regras de reserva, persistência e interface JavaFX. Requer uma sessão gráfica e usa bancos H2 temporários, sem alterar o banco local. Para testar sem abrir janelas, use `mvn verify`.

Os fontes ficam em `src/main/java`, os testes em `src/test/java` e as dependências no `pom.xml`.

## Equipe

Desenvolvido por Arthur Dantas, Maria Clara, Victor Yoshi, Rafael Chada
