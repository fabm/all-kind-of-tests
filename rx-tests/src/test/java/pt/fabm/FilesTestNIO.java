package pt.fabm;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FilesTestNIO {
    @Test
    public void writeWithChannels() throws IOException, ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        Path temp = Files.createTempDirectory("myFiles");
        List<Future<Integer>> operations = new ArrayList<>();
        List<AsynchronousFileChannel> channels = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            final String fileName = "temp".concat(String.valueOf(j)).concat(".txt");
            final Path path = temp.resolve(fileName);
            if (!path.toFile().exists()) {
                Files.createFile(path);
            }

            final HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.WRITE);
            final AsynchronousFileChannel fileChannel1 = AsynchronousFileChannel.open(path, options,executorService);
            final AsynchronousFileChannel fileChannel2 = AsynchronousFileChannel.open(path, options,executorService);
            int sum = 0;
            for (int i = 0; i < 100_000; i++) {
                final byte[] bytes ="0123456789".getBytes();

                sum += bytes.length;
                final ByteBuffer buffer1 = ByteBuffer.wrap(bytes);
                operations.add(fileChannel1.write(buffer1, sum));

                sum += bytes.length;
                final ByteBuffer buffer2 = ByteBuffer.wrap(bytes);
                operations.add(fileChannel2.write(buffer2, sum));
            }
            channels.add(fileChannel1);
        }

        for (Future<?> operation : operations) {
            operation.get();
        }
        for (AsynchronousFileChannel channel : channels) {
            channel.close();
        }
        FileUtils.deleteDirectory(temp.toFile());

    }
}
