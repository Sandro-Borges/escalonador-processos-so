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
- Tempos de CPU: variáveis por processo
- Tempos de I/O: 3 a 8 unidades para disco, fita e impressora
- Critério de geração dos processos: inicializados no início da simulação
- Semente aleatória, se aplicável: seed de 42

## Como executar o projeto
```bash
# Execução tradicional de java
javac src/Escalonador.java
java src.Escalonador


## O que aparece na saída
[config]: exibe os parâmetros iniciais da execução, como valor do quantum, quantidade de processos e seed.
[t=XXX] Px criado → fila ALTA: indica o momento em que um novo processo é gerado e inserido na fila de alta prioridade.
CPU executa Px por Y unidades: mostra o momento em que a CPU aloca o processador para o processo executar.
sofreu preempção → fila BAIXA: quando o processo esgota o seu quantum sem finalizar, sendo rebaixado para a fila de menor prioridade.
solicitou I/O [DISCO/FITA/IMPRESSORA]: indica que o processo corta o uso da CPU para efetuar uma operação de entrada/saída.
retornou do [DISCO] → fila BAIXA: mostra o término da operação de I/O e o regresso do processo para a fila adequada.
[fim] Processos finalizados: X/X | tempo total: Y: estatística final que encerra a execução ao concluir todos os processos.

## Limitações conhecidas
Foi implementado em Java com abordagem estruturada, sem ser orientado a objetos e com uso de classes estáticas e listas.
O gerenciamento de I/O e das filas de dispositivos é simulado dentro da linha do tempo controlada pelo ciclo principal (loop) do escalonador.
