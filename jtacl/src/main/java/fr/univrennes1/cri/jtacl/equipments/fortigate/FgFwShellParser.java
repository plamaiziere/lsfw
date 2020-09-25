package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import org.parboiled.Rule;

/**
 * Fortigate sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgFwShellParser extends GenericEquipmentShellParser {

	protected String _service;
	protected String _network;


	@Override
	protected boolean clear() {
		_service = null;
		_network = null;
		return super.clear();
	}

	public String getService() {
		return _service;
	}

	public boolean setService(String service) {
		_service = service;
		return true;
	}

	public String getNetwork() {
		return _network;
	}

	public boolean setNetwork(String network) {
		_network = network;
		return true;
	}

	public Rule CommandLine() {
		return
			Sequence(
				clear(),
				FirstOf(
					CommandHelp(),
					CommandXrefIp(),
					CommandXrefService(),
					CommandShowService(),
					CommandShowNetwork(),
					CommandShowRules()
				)
			);
	}

	public Rule CommandShowService() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("service"),
				FirstOf(
					WhiteSpaces(),
					EOI
				),
				UntilEOI(),
				setService(match().trim()),
				setCommand("show-service")
		);
	}

	public Rule CommandShowNetwork() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("network"),
				FirstOf(
					WhiteSpaces(),
					EOI
				),
				UntilEOI(),
				setNetwork(match().trim()),
				setCommand("show-network")
		);
	}

	public Rule CommandShowRules() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("rules"),
				SkipSpaces(),
				EOI,
				setCommand("show-rules")
		);
	}

}
