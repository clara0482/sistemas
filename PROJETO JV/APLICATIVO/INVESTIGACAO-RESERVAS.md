# Investigação de reservas

Reprodução anterior à correção: teste executado com sucesso usando BancoTeste.abrir e @TempDir (H2 em arquivo temporário). Ana, telefone (11) 99999-0001, e Bia, telefone 11999990001, receberam cliente_id=1. As duas solicitações devolveram reserva_id=1. Consulta SQL direta a reserva antes do cancelamento:

    id=1, cliente_id=1, mesa_id=6, horario=2026-10-21 19:00, status=ATIVA

Depois do cancelamento pela sessão de Bia:

    id=1, cliente_id=1, mesa_id=6, horario=2026-10-21 19:00, status=CANCELADA

Não havia duas reservas persistidas nesse cenário. salvarCliente reutilizava o cliente pelo telefone normalizado sem conferir o nome; buscarReservaExistente devolvia corretamente a reserva já existente desse mesmo cliente. A tela mostrava confirmação nas duas sessões. Não foi reproduzida duplicação com clientes de IDs distintos.

## Verificação das camadas

- Reserva.id já usa IDENTITY, chave primária gerada pelo H2. cliente_id e mesa_id são associações ManyToOne sem cascata de cancelamento.
- listarReservas filtra cliente_id. cancelarReserva recebe reservaId e clienteId, verifica o proprietário e altera somente a entidade selecionada. Não cancela por mesa ou horário.
- Não foi encontrada coleção estática de reservas nem compartilhamento indevido de objetos de cliente. A identidade persistida era reutilizada pelo cadastro.
- mesaOcupada considera somente ATIVA e intervalos abertos entre início menos duas horas e início mais duas horas. Reservas consecutivas são permitidas.
- solicitarMesa, alterarReserva e cancelarReserva já serializam a alocação com bloqueio pessimista do restaurante na transação. Testes concorrentes com dois serviços verificam o mesmo horário e sobreposição de 30 minutos.
- O defeito reproduzido está na lógica de identificação Java. Não foi encontrada falha de geração de IDs ou atualização em cascata do Hibernate/H2.

## Alterações mínimas

- SistemaReservas.salvarCliente: rejeita telefone existente com nome diferente (ignorando caixa). Mantém reentrada com os mesmos dados, sem sobrescrever cadastro.
- SistemaReservas.criarReserva: confere disponibilidade imediatamente antes de persistir, como proteção adicional nos fluxos de reserva e atendimento da fila, dentro da transação já bloqueada.
- MainFX.reservar recarrega as mesas; os handlers de alteração/cancelamento recarregam as reservas. Ao selecionar as abas, a interface consulta novamente reservas, mesas ou fila no banco. O método auxiliar atualizarEstado existe, mas não está ligado aos handlers; a descrição anterior dessa ligação estava incorreta. O comportamento validado nos testes manuais foi preservado.
- cancelarReserva, mapeamentos e consultas não precisaram ser alterados.

## Testes

IdentidadeReservaTest cobre rejeição de identidade trocada, normalização, IDs distintos, cancelamento por outro cliente, cancelamento repetido, independência na mesma mesa em horários consecutivos, reabertura do H2 e concorrência. Consulta SQL registra as linhas persistidas. MainFXTest cobre rejeição no cadastro e os fluxos de reserva, alteração, cancelamento, fila e atualização visual. Os testes antigos que aceitavam outro nome para o mesmo telefone foram ajustados para reentrada com o nome original.

Todos os bancos dos testes são criados em @TempDir. Nenhum teste usa HibernateUtil para abrir dados/reservas. O banco original não foi aberto nem migrado. Nenhum commit ou push foi feito.

Limite: nome e telefone não constituem autenticação. Esta correção impede a associação acidental reproduzida, mas não impede alguém que conheça ambos os dados de entrar no mesmo cadastro. Autenticação exigiria um fluxo separado. A proteção de disponibilidade vale para os fluxos de SistemaReservas; gravações SQL externas podem contornar a lógica da aplicação.