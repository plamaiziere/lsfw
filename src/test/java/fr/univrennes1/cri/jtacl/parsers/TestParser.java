package fr.univrennes1.cri.jtacl.parsers;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

import java.util.ArrayList;
import java.util.List;

public class TestParser extends BaseParser<Object> {
	//public TestParser() {}
	List<String> matched = new ArrayList();

	public Rule Hw() {
		return Sequence(Hello(),
				String(" "),
				World());

	}

	public Rule Hello() {
		return Sequence(
				String("hello")
				, matched.add("+" + match())
		);
	}

	public Rule World() {
		return Sequence(
				FirstOf(
						"world",
						"all")
				, matched.add("+" + match())
		);
	}
}
