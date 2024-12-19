package fr.univrennes1.cri.jtacl.parsers;

import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;


public class TestParboiled extends TestCase {

    public void test() {
        TestParser parser = Parboiled.createParser(TestParser.class);
        ParsingResult<?> result;
        ReportingParseRunner parseRunerParse = new ReportingParseRunner(parser.Hw());

        result = parseRunerParse.run("hello world");
        assertTrue(result.matched);
        assertEquals("+hello", parser.matched.get(0));
        assertEquals("+world", parser.matched.get(1));
    }

}
