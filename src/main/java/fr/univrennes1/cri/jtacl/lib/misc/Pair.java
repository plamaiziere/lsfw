package fr.univrennes1.cri.jtacl.lib.misc;

public class Pair<U, V> {
    private U _p1;
    private V _p2;

    public Pair(U p1, V p2) {
        this._p1 = p1;
        this._p2 = p2;
    }

    public U get1() {
        return _p1;
    }

    public V get2() {
        return _p2;
    }
}
