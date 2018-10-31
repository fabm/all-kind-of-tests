package pt.fabm;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

public class LinesAdder {
    public static String addFragments(LinesReader linesReader, IntPredicate checkContentPredicate, IntFunction<String> indexedContent){
        try (Stream<String> lines = linesReader.getLines()){
            return withLines(lines.iterator(),checkContentPredicate,indexedContent);
        } catch (IOException e){
            throw new IllegalStateException(e);
        }
    }
    private static String withLines(Iterator<String> linesIterator, IntPredicate checkContentPredicate, IntFunction<String> indexedContent) throws IOException{
        int i=-1;
        StringJoiner stringJoiner = new StringJoiner("\n");
        while (linesIterator.hasNext()){
            if(checkContentPredicate.test(++i)){
                stringJoiner.add(indexedContent.apply(i));
            }
            stringJoiner.add(linesIterator.next());
        }
        return stringJoiner.toString();
    }
}
