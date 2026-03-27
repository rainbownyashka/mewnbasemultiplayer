/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.geom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import za.co.luma.geom.Segment;

public class SegmentSet
implements Iterable<Segment> {
    List<Segment> segments = new ArrayList<Segment>();

    public boolean addAll(Collection<? extends Segment> c) {
        return this.segments.addAll(c);
    }

    public boolean addAll(SegmentSet set) {
        boolean added = true;
        for (Segment segment : set) {
            added &= this.add(segment);
        }
        return added;
    }

    public boolean add(Segment segment) {
        return this.segments.add(segment);
    }

    public void clear() {
        this.segments.clear();
    }

    @Override
    public Iterator<Segment> iterator() {
        return this.segments.iterator();
    }

    public boolean remove(Object segment) {
        return this.segments.remove(segment);
    }

    public String toString() {
        String str = "";
        for (Segment s : this.segments) {
            str = str + s + " ";
        }
        return str;
    }
}

