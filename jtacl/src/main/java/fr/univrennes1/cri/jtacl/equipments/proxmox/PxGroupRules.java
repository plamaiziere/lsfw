package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

import java.util.ArrayList;

public class PxGroupRules extends ArrayList<PxRule> {
	protected String _name;
	protected ParseContext _context;

	public PxGroupRules(String name, ParseContext context) {
		super();
		_name = name;
		_context = context;
	}

	public String getName() { return _name; }

	public ParseContext getContext() { return _context; }
}
