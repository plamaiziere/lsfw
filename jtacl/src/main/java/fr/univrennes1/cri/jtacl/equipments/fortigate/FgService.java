package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;

import java.util.LinkedList;
import java.util.List;

public abstract class FgService extends FgObject {
	protected FgServiceType _type;
	protected String _comment;

	protected List<Object> _linkedTo = new LinkedList<>();

	public FgService(String name, String originKey, String comment, FgServiceType type) {

		super(name, originKey);
		_type = type;
		_comment = comment;
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

    @Override
    public String toString() {
        return getName() + ", " + getOriginKey() + ", " + _comment;
    }

    /**
	 * Returns the {@link FgServicesMatch} of the given {@link ProbeRequest}.
	 * @param request request to test.
	 * @return the FgServicesMatch of the given ProbeRequest.
	 */
	public abstract FgServicesMatch matches(ProbeRequest request);

}
