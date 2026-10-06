# Sistema de Reservas de Restaurantes

Aplicação de console em Java para reservar mesas em restaurantes, com fila de espera quando não há mesa disponível. O projeto foi feito para praticar Programação Orientada a Objetos (POO), tratamento de exceções e a API de datas `java.time`.

## Funcionalidades

- Cadastro do cliente (nome e telefone) com validação.
- Listagem de restaurantes cadastrados.
- Exibição dos horários disponíveis e do mapa de mesas (livre ou ocupada) antes da reserva.
- Reserva de mesa conforme o número de pessoas e o horário escolhido.
- Fila de espera automática quando nenhuma mesa compatível está livre.
- Consulta da posição do cliente na fila de cada restaurante.
- Geração aleatória de mesas, capacidades e horários, para facilitar demonstrações.
- Tratamento de cada input com `try/catch` e exceção específica, repetindo a pergunta até receber um valor válido.

## Tecnologias

- Java 11 ou superior
- Pacote `java.time` (`LocalDateTime`, `LocalTime`, `LocalDate`, `DateTimeFormatter`)
- `Scanner` para entrada de dados no console

## Estrutura do projeto

| Arquivo | Responsabilidade |
|---|---|
| `Main.java` | Ponto de entrada. Cadastro do cliente, menu interativo e leitura validada dos inputs. |
| `Restaurante.java` | Gerencia mesas, horários, reservas e fila de espera. Gera os dados aleatórios. |
| `Reserva.java` | Representa uma reserva (cliente, mesa, horário e status). |
| `StatusReserva.java` | Enum com os status possíveis: `ATIVA`, `CONCLUIDA` e `CANCELADA`. |
| `Mesa.java` | Representa uma mesa (número, capacidade e disponibilidade). |
| `Cliente.java` | Representa o cliente (nome e telefone). |
| `FilaEspera.java` | Controla a fila de clientes aguardando mesa. |

## Como executar

### Pré-requisito

Ter o JDK 11 ou superior instalado. Para conferir:

```bash
java -version
javac -version
```

### Compilar e rodar

```bash
git clone https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git
cd SEU-REPOSITORIO

javac *.java
java Main
```

Se os arquivos estiverem dentro de uma pasta `src`, compile com `javac src/*.java -d out` e execute com `java -cp out Main`.

## Regras de negócio

- Ao criar uma reserva, a mesa é ocupada imediatamente.
- Uma mesa só é escolhida se estiver livre e tiver capacidade igual ou maior que o número de pessoas.
- O horário precisa estar entre os horários disponíveis do restaurante. A reserva é feita para a data de hoje.
- Se não houver mesa compatível, o cliente entra na fila de espera do restaurante.
- Mesas têm 2, 4, 6 ou 8 lugares. Cada restaurante recebe entre 4 e 8 mesas, e cerca de 40% começam ocupadas.

## Limitações conhecidas

- Os dados ficam apenas em memória e são perdidos ao encerrar o programa.
- A disponibilidade da mesa não considera conflitos de horário: a mesa fica ocupada assim que é reservada.
- O limite de 8 pessoas por reserva acompanha a maior capacidade gerada em `Restaurante`. Se alterar a capacidade das mesas, ajuste também o `lerInteiro` correspondente na `Main`.

## Próximos passos

- Interface gráfica com JavaFX.
- Persistência dos dados (arquivo ou banco de dados).
- Verificação de conflitos de reserva por horário.
- Cancelamento e conclusão de reservas pelo menu.
- Chamada automática do próximo cliente da fila quando uma mesa for liberada.

## Autor

Desenvolvido por **SEU NOME**.

## Licença

Defina a licença do projeto (por exemplo, MIT) e adicione o arquivo `LICENSE` ao repositório.
