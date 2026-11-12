# Trabalho Final de Sistemas Operacionais

## Grupo
- Aluno 1: SANDRO DINARTE
- Aluno 2: NÍCOLAS BENINI
- Aluno 3: GABRIEL FERNANDO
- Aluno 4: DANIEL BIER
- Aluno 5: YURI CAMPOS
- Aluno 6: SAMUEL HORN

## Linguagem utilizada
JAVA

## Premissas do escalonador
- Quantum: 3 unidades
- Número máximo de processos: 5 processos
- Tempos de CPU: definidos por processo (P1=6, P2=5, P3=4, P4=3, P5=2)
- Tempos de I/O: disco=4, fita=3 e impressora=5 unidades
- Critério de geração dos processos: inicializados no início da simulação (t=0), todos com PPID 0
- Critério de finalização: a simulação termina quando todos os processos são finalizados
- Semente aleatória, se aplicável: não se aplica, os valores são fixos então a saída é sempre a mesma

## Como executar o projeto
```bash
# Execução tradicional de Java (a partir da raiz do repositório)
javac -d out src/Escalonador.java
java -cp out Escalonador

# Bônus Docker
docker build -t so-escalonador-grupo-x .
docker run --rm so-escalonador-grupo-x
```

## O que aparece na saída
[config]: exibe os parâmetros iniciais da execução, como valor do quantum e quantidade de processos.
[t=XXX] Px criado → fila ALTA: indica o momento em que um novo processo é gerado e inserido na fila de alta prioridade.
CPU executa Px por Y unidades: mostra o momento em que a CPU aloca o processador para o processo executar.
sofreu preempcao → fila BAIXA: quando o processo esgota o seu quantum sem finalizar, sendo rebaixado para a fila de menor prioridade.
solicitou I/O [DISCO/FITA/IMPRESSORA] por Z unidades: indica que o processo corta o uso da CPU para efetuar uma operação de entrada/saída.
retornou do [DISCO] → fila BAIXA / retornou da [FITA/IMPRESSORA] → fila ALTA: mostra o término da operação de I/O e o regresso do processo para a fila adequada.
CPU ociosa: indica que nenhum processo estava pronto naquele instante.
[fim] Processos finalizados: X/X | tempo total: Y: estatística final que encerra a execução ao concluir todos os processos.
Tabela final: mostra o PCB de cada processo (PID, PPID, tempo de CPU, I/O, fim, turnaround, espera e status) e as métricas de preempções, I/O por tipo, espera média, turnaround médio e CPU ociosa.

## Limitações conhecidas
Foi implementado em Java com abordagem estruturada, sem ser orientado a objetos e com uso de classes estáticas e listas.
O gerenciamento de I/O e das filas de dispositivos é simulado dentro da linha do tempo controlada pelo ciclo principal (loop) do escalonador.
Os processos e os tempos são fixos no código e cada processo faz no máximo um I/O.
