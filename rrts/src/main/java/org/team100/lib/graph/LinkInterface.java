package org.team100.lib.graph;

/** Allows experimentation with link behaviors. */
public interface LinkInterface {

    // Path path();

    Node get_source();

    Node get_target();

    double get_linkDist();

    void set_PathDist(double d);

    double get_pathDist();

}
