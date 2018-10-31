package pt.fabm;

import java.io.IOException;
import java.util.stream.Stream;

public interface LinesReader {
    Stream<String> getLines() throws IOException;
}
