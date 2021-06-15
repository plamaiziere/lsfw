package fr.univrennes1.cri.jtacl.equipments.proxmox;

import java.util.List;

/**
 * Template to build rule. This class is used at parsing time
 * as an intermediate storage.
 * @see
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class RuleTemplate {

	protected boolean disabled = false;
	protected String direction;
	protected String action;
	protected String macro;
	protected List<String> sourceIpSpec;
	protected List<String> destIpSpec;
	protected List<String> sourcePortSpec;
	protected List<String> destPortSpec;
	protected String icmpType;
	protected String proto;
	protected String iface;
	protected String groupName;

	public boolean isDisabled() {
		return disabled;
	}

	public boolean setDisabled(boolean disabled) {
		this.disabled = disabled;
		return true;
	}

	public String getDirection() {
		return direction;
	}

	public boolean setDirection(String direction) {
		this.direction = direction;
		return true;
	}

	public String getAction() {
		return action;
	}

	public boolean setAction(String action) {
		this.action = action;
		return true;
	}

	public String getMacro() {
		return macro;
	}

	public boolean setMacro(String macro) {
		this.macro = macro;
		return true;
	}

	protected boolean setIcmpType(String s) {
		icmpType = s;
		return true;
	}

	protected boolean setProto(String s) {
		proto = s;
		return true;
	}

	protected boolean setIface(String s) {
		iface = s;
		return true;
	}

	public String getIcmpType() {
		return icmpType;
	}

	public String getProto() {
		return proto;
	}

	public String getIface() {
		return iface;
	}

	public List<String> getSourcePortSpec() {
		return sourcePortSpec;
	}

	public boolean setSourcePortSpec(List<String> sourcePortSpec) {
		this.sourcePortSpec = sourcePortSpec;
		return true;
	}

	public List<String> getDestPortSpec() {
		return destPortSpec;
	}

	public boolean setDestPortSpec(List<String> destPortSpec) {
		this.destPortSpec = destPortSpec;
		return true;
	}

	public List<String> getSourceIpSpec() {
		return sourceIpSpec;
	}

	public boolean setSourceIpSpec(List<String> sourceIpSpec) {
		this.sourceIpSpec = sourceIpSpec;
		return true;
	}

	public List<String> getDestIpSpec() {
		return destIpSpec;
	}

	public boolean setDestIpSpec(List<String> destIpSpec) {
		this.destIpSpec = destIpSpec;
		return true;
	}

	public String getGroupName() {
		return groupName;
	}

	public boolean setGroupName(String groupName) {
		this.groupName = groupName;
		return true;
	}

	public boolean isGroup() {
		return groupName != null;
	}
}
