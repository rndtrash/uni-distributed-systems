package org.example;

import mpi.MPI;
import mpi.Request;
import mpi.Status;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Task3 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        if (size < 3) {
            if (rank == 0)
                System.err.println("Ошибка: количество процессов третьего уровня должно быть чётное количество");

            MPI.Finalize();
            return;
        }
        int thirdRankCount = size - 3;

        if (rank > 2) {
            int thirdRank = rank - 3;
            boolean isFirstHalf = thirdRank < thirdRankCount / 2;
            int[] data = new int[]{(int) (Math.random() / Math.nextDown(1.0) * 100)};
            int target = isFirstHalf ? 1 : 2;
            System.out.println("Процесс третьего уровня " + rank + " отправил " + data[0] + " узлу " + target);
            MPI.COMM_WORLD.Isend(data, 0, data.length, MPI.INT, target, 0);
        } else if (rank > 0) {
            Request[] requests;
            int[] data;
            int count = thirdRankCount / 2;
            int rankOffset = 3;
            if (rank == 2) {
                rankOffset += count;
                // Для нечётного количества процессов на вторую половину приходится +1 элемент
                count += thirdRankCount % 2;
            }

            requests = new Request[count];
            data = new int[count];
            for (int i = 0; i < count; i++) {
                requests[i] = MPI.COMM_WORLD.Irecv(data, i, 1, MPI.INT, rankOffset + i, 0);
                System.out.println("Процесс второго уровня " + rank + " ждёт данных от узла " + (rankOffset + i));
            }
            Request.Waitall(requests);

            data = Arrays.stream(data).sorted().toArray();
            System.out.println("Процесс второго уровня " + rank + " отправил " + Arrays.toString(data) + " узлу " + 0);
            MPI.COMM_WORLD.Isend(data, 0, count, MPI.INT, 0, 0);
        } else { // Ранк 0
            Status status1 = MPI.COMM_WORLD.Probe(1, 0), status2 = MPI.COMM_WORLD.Probe(2, 0);
            int length1 = status1.count, length2 = status2.count;
            int[] data1 = new int[length1], data2 = new int[length2];
            Request.Waitall(new Request[]{MPI.COMM_WORLD.Irecv(data1, 0, length1, MPI.INT, 1, 0), MPI.COMM_WORLD.Irecv(data2, 0, length2, MPI.INT, 2, 0)});

            int[] endResult = IntStream.concat(Arrays.stream(data1), Arrays.stream(data2)).sorted().toArray();
            System.out.println("Конечный результат от узла первого уровня: " + Arrays.toString(endResult));
        }

        MPI.Finalize();
    }
}
