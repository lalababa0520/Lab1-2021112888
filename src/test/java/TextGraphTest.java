import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TextGraphTest {

    private TextGraph textGraph;

    @BeforeEach
    public void setUp() {
        textGraph = new TextGraph();
        try {
            textGraph.readTextFile("./test.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPathExists() {
        String result = textGraph.calcShortestPath("apple", "banana");
        assertEquals("Shortest path from \"apple\" to \"banana\": apple -> cherry -> banana (Total weight: 2)", result);
    }

    @Test
    public void testNoPath() {
        String result = textGraph.calcShortestPath("apple", "egg");
        assertEquals("No path from \"apple\" to \"egg\"!", result);
    }

    @Test
    public void testWord1NotInGraph() {
        String result = textGraph.calcShortestPath("apple", "dog");
        assertEquals("No \"dog\" in the graph!", result);
    }

    @Test
    public void testWord2NotInGraph() {
        String result = textGraph.calcShortestPath("dog", "banana");
        assertEquals("No \"dog\" in the graph!", result);
    }

    @Test
    public void testBothWordsNotInGraph() {
        String result = textGraph.calcShortestPath("dog", "cat");
        assertEquals("No \"dog\" and \"cat\" in the graph!", result);
    }
}