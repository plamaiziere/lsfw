package fr.univrennes1.cri.jtacl.equipments.fortigate;

/**
 * Fortigate base object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgObject {
    protected String _name;
    protected String _originKey;

    /**
     * Contruct a new Fg object.
     * @param name name
     * @param originKey
     */
    public FgObject(String name, String originKey) {
        this._name = name;
        this._originKey = originKey;
    }

    public String getName() {
        return _name;
    }

    public String getOriginKey() {
        return _originKey;
    }

}
