import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TextGraphwTest {

    private TextGraph graph;

    @BeforeEach
    public void setUp() {
        graph = new TextGraph();
        try {
            graph.readTextFile("./test1.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testQueryBridgeWords_NoWordsInGraph() {
        String result=graph.queryBridgeWords("apple", "banana");
        assertEquals("No \"apple\" and \"banana\" in the graph!",result);
    }

    @Test
    public void testQueryBridgeWords_NoWord1InGraph() {
        String result=graph.queryBridgeWords("apple", "orange");
        assertEquals("No \"apple\" in the graph!",result);
    }

    @Test
    public void testQueryBridgeWords_NoWord2InGraph() {
        String result=graph.queryBridgeWords("orange", "banana");
        assertEquals("No \"banana\" in the graph!",result);
    }

    @Test
    public void testQueryBridgeWords_NoBridgeWords() {
        String result=graph.queryBridgeWords("orange", "lemon");
        assertEquals("No bridge words from \"orange\" to \"lemon\"!",result);
    }

    @Test
    public void testQueryBridgeWords_SingleBridgeWord() {
        String result=graph.queryBridgeWords("orange", "grape");
        assertEquals("The bridge words from \"orange\" to \"grape\" is: mango",result);
    }

    @Test
    public void testQueryBridgeWords_MultipleBridgeWords() {
        String result=graph.queryBridgeWords("orange", "cherry");
        assertEquals("The bridge words from \"orange\" to \"cherry\" are: kiwi, peach",result);
    }
}