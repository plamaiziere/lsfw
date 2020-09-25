package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

import java.util.LinkedList;
import java.util.List;

public abstract class FgService extends FgObject {
	protected FgServiceType _type;
	protected String _comment;
    private List<IPRangeable> _ipRanges;
    private String _fqdn;

	protected List<Object> _linkedTo = new LinkedList<>();

	protected  MatchResult matchAddress(IPRangeable range) {
            if (_ipRanges == null) return MatchResult.ALL;
            int all = 0;
            int may = 0;

            for (IPRangeable r: _ipRanges) {
                if (r.contains(range)) all++;
                if (r.overlaps(range)) may++;
            }
            if (all > 0) return MatchResult.ALL;
            if (may > 0) return MatchResult.MATCH;
            return MatchResult.NOT;
    }


	public FgService(String name, String originKey, String comment, List<IPRangeable> ipRanges, String fqdn, FgServiceType type) {

		super(name, originKey);
		_type = type;
		_comment = comment;
		_ipRanges = ipRanges;
		_fqdn = fqdn;
	}

	public List<Object> getLinkedTo() {
		return _linkedTo;
	}

	public void linkWith(Object obj) {
		if (!_linkedTo.contains(obj)) {
			_linkedTo.add(obj);
		}
	}

	public FgServiceType getType() {
		return _type;
	}

	public String getComment() {
		return _comment;
	}

	public List<IPRangeable> getipRanges() { return _ipRanges; }

	public String getFqdn() { return _fqdn; }

    @Override
    public String toString() {
        String s = _name + ", " + _originKey + ", " + _comment + ", " + _type;
        if (_ipRanges != null) s += ", ipRanges=" + _ipRanges;
        if (_fqdn != null) s += ", fqdn = " + _fqdn;
        return s;
    }

    /**
	 * Returns the {@link FgServicesMatch} of the given {@link ProbeRequest}.
	 * @param probe the probe to test.
	 * @return the FgServicesMatch of the given ProbeRequest.
	 */
	public abstract FgServicesMatch matches(Probe probe);

}
