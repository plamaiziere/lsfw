package fr.univrennes1.cri.jtacl.equipments.proxmox;

import java.util.List;

public class AliasTemplate {
	protected String _name;
	protected List<String> _ipspec;

	public String getName() {
		return _name;
	}

	public boolean setName(String _name) {
		this._name = _name;
		return true;
	}

	public List<String> getIpSpec() {
		return _ipspec;
	}

	public boolean setIpSpec(List<String> _ipspec) {
		this._ipspec = _ipspec;
		return true;
	}
}
