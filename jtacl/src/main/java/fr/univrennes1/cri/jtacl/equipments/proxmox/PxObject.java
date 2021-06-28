package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

public class PxObject {
	protected ParseContext _context;

	public PxObject(ParseContext context) {
		_context = context;
	}

	public ParseContext getContext() {
		return _context;
	}

}
