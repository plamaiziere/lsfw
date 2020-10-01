package fr.univrennes1.cri.jtacl.equipments.fortigate;

import java.util.LinkedList;

public class FgIfacesSpec extends LinkedList<String> {
    public boolean isAny() { return contains("any"); }
}
