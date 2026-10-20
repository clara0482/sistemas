# Sistema de reservas de mesas

Projeto de POO para reservar mesas em restaurantes pelo console ou pela interface JavaFX. Cada reserva
dura duas horas. Sem mesa compatível disponível, o pedido entra na fila de espera.
O cancelamento pode atender o próximo pedido elegível, mantendo o horário solicitado.

## Classes e requisitos de POO

| Classe de domínio | Responsabilidade |
|---|---|
| `Cliente` | Guarda e valida nome e telefone. |
| `Restaurante` | Gerencia as mesas do restaurante. |
| `Mesa` | Guarda número e capacidade e verifica se comporta o grupo. |
| `Reserva` | Guarda cliente, mesa, horário e quantidade; permite alterar e cancelar. |
| `RegistroFila` | Guarda o pedido de um cliente que aguarda uma mesa. |
| `FilaEspera` | Organiza e consulta os pedidos em ordem de entrada. |

As seis classes utilizam atributos privados, construtores, métodos e associações.
Hibernate/JPA faz a persistência no H2. `Main` cuida do menu,
`SistemaReservas` coordena as regras e transações, `Repositorio` reúne as consultas
e `HibernateUtil` configura a conexão. `StatusReserva` define os estados possíveis.

## Duas coleções em uso

- `Restaurante.mesas` (`List<Mesa>`): adiciona e procura mesas no cadastro inicial;
  é percorrida nas consultas de disponibilidade e na escolha da mesa para uma reserva.
- `FilaEspera.registros` (`List<RegistroFila>`): adiciona e procura pedidos,
  calcula posições, seleciona candidatos e remove pedidos atendidos nas reservas,
  trocas de mesa e cancelamentos. A fila é reconstruída com os registros salvos no banco.

## Banco H2 local

H2 é um banco Java que funciona dentro do aplicativo, sem servidor separado,
PostgreSQL, pgAdmin ou variáveis de ambiente. O Maven baixa suas bibliotecas.
O usuário local é `sa`, com senha vazia: configuração para demonstração local,
inadequada para um banco exposto em rede.

A URL `jdbc:h2:file:./dados/reservas` é relativa à **pasta de execução**.
Inicie o programa na pasta `APLICATIVO`, onde fica o `pom.xml`.
A pasta `dados` e o arquivo `dados/reservas.mv.db` são criados automaticamente.
Ao reiniciar nessa mesma pasta, o programa abre o mesmo banco e recupera os dados.
Iniciar em outra pasta sem copiar `dados` cria um banco independente nessa pasta.

As anotações JPA ligam os objetos às tabelas: cada mesa referencia seu restaurante;
cada reserva referencia cliente e mesa; cada pedido da fila referencia cliente e
restaurante. A `Session` carrega e salva esses objetos. `commit` confirma a
transação; `rollback` desfaz alterações não confirmadas. Ao sair pelo menu,
o programa fecha a fábrica de sessões e as conexões com o banco.

O Hibernate utiliza `update` para preparar as tabelas e manter os dados entre
execuções. Isso facilita o protótipo acadêmico, mas não substitui migrações de
esquema planejadas em produção.

A coluna de quantidade aceita `null` para preservar reservas antigas sem inventar
quantas pessoas participaram. O menu exibe **quantidade não informada** nesses casos;
uma alteração explícita pode confirmar a quantidade de uma reserva ativa futura.
Novas reservas sempre armazenam uma quantidade válida, inclusive as vindas da fila.

## Compilar e executar

Instale JDK 17 ou superior e Maven. A primeira execução precisa de acesso à
internet para baixar as dependências; depois elas ficam no cache do Maven.
Este projeto não entrega um JAR autossuficiente: a execução documentada usa Maven.

Abra a pasta `APLICATIVO` no VS Code com suporte a Java, aguarde a importação do
projeto Maven e execute `Main.java` por **Run Java** ou **F5**. A configuração F5
usa a pasta do projeto como diretório de trabalho. Também é possível usar um
terminal aberto nessa pasta, sem depender da IDE:

```text
mvn clean package
mvn compile exec:java "-Dexec.mainClass=Main"
```

Use o Maven para compilar com as dependências declaradas no `pom.xml`.

## Interface JavaFX e testes

Na pasta `APLICATIVO`, execute a interface com as dependências fornecidas pelo Maven:

```text
mvn javafx:run
```

Não é necessário instalar um SDK JavaFX separado em `lib`. No VS Code, a configuração
**MainFX (interface JavaFX)** inicia a interface; **Main (console)** mantém o menu de texto.
As duas interfaces usam o mesmo serviço, as mesmas validações e o mesmo arquivo H2.
Feche uma interface antes de abrir a outra com esse banco.

Na interface, escolha restaurante, data futura, horário e quantidade de pessoas.
O mapa de mesas considera o período selecionado. A aba **Minhas reservas** permite
consultar o histórico, alterar a quantidade e cancelar. A aba da fila mostra a posição
dos pedidos persistidos, respeitando horário, capacidade e disponibilidade.

```text
mvn verify
mvn -Pjavafx-ui-tests verify
```

O primeiro comando executa os testes de integração sem abrir janelas. O segundo
inclui um teste que abre e fecha a interface e aciona seus controles; requer uma
sessão gráfica disponível. Todos os testes usam bancos H2 em diretórios temporários,
sem abrir ou modificar `dados/reservas.mv.db`. Os testes verificam cadastro,
validações, disponibilidade, reservas, alteração, cancelamento, fila e persistência
após fechar e reabrir o banco.

## Transportar, fazer backup e restaurar

Com o aplicativo **fechado**, copie a pasta do projeto para o outro computador.
Para levar os registros, copie também a pasta `dados` para junto do `pom.xml`.
Execute os mesmos comandos nessa nova pasta, com Java e Maven instalados.
O ZIP de código-fonte não inclui registros pessoais: sem `dados`, a primeira
execução começa com um banco novo.

Para fazer backup, feche o aplicativo e copie `dados/reservas.mv.db` para um
local separado. Para restaurar, feche o aplicativo, guarde uma cópia do banco
atual e coloque o arquivo de backup em `dados/reservas.mv.db`.
Use a mesma versão do projeto/H2 para abrir a cópia.

## Usar o menu

Informe nome e telefone com DDD; os mesmos dados recuperam o cliente cadastrado.
Um telefone existente com nome diferente é rejeitado; diferenças de maiúsculas e minúsculas são aceitas.
O cadastro existente é reutilizado sem sobrescrever o nome ou telefone.
Na primeira execução, o sistema prepara três restaurantes com cinco mesas cada.

1. **Fazer reserva:** escolha restaurante, data futura, horário exibido e 1 a 8 pessoas.
2. **Minhas reservas:** consulte o histórico e os estados das reservas.
3. **Cancelar reserva:** informe o número de uma reserva sua que esteja ativa.
4. **Acompanhar fila:** consulte a ordem entre pedidos do mesmo restaurante com
   períodos sobrepostos. A posição não garante atendimento: a capacidade também conta.
5. **Alterar reserva:** informe o ID de uma reserva sua ativa, que ainda não começou,
   e a nova quantidade. Depois do horário de início, a alteração é recusada sem modificar a reserva.
   Se couber, mantém a mesa; senão, procura outra no mesmo restaurante e horário.
   A troca preserva o ID, respeita a fila e só é confirmada se a operação toda funcionar.
   Sem mesa disponível, a reserva original fica intacta.
6. **Sair:** encerre o programa e feche as conexões.

Reservas e pedidos pendentes de um cliente não podem ter períodos sobrepostos,
mesmo em restaurantes diferentes. Horários consecutivos, como 19h e 21h, são
permitidos. Para mudar a quantidade de uma reserva, use **Alterar reserva**;
na fila, uma repetição com quantidade diferente é rejeitada e mantém o pedido original.
Pedidos vencidos são excluídos na inicialização e não entram nas consultas ou no atendimento.

## Limitações

- Abra apenas uma execução do aplicativo por arquivo de banco. Se ele estiver
  bloqueado ou a pasta não permitir gravação, o programa mostra uma mensagem;
  feche a outra execução ou corrija a permissão, mantendo o arquivo existente.
- A fila respeita a ordem dos pedidos elegíveis por horário, capacidade e
  disponibilidade. Ela é atendida em cancelamentos, trocas de mesa e novos pedidos compatíveis.
- `CONCLUIDA` existe no enum, mas não há conclusão automática de reservas.
- A identificação por nome e telefone não é autenticação segura; é uma limitação
  deste protótipo acadêmico, que não possui login ou senha de usuário.
- Os novos telefones são salvos só com dígitos. Dados importados com outras
  formatações devem passar pela validação e normalização de `Cliente.setTelefone`.
- O H2 começa vazio e é independente do PostgreSQL anterior. Os registros antigos
  permanecem no PostgreSQL: não foram apagados nem transferidos automaticamente.
  Uma importação futura precisa tratar os telefones e preservar IDs e associações.
