package edu.unc.robotics.prrts.tree;

import java.util.concurrent.atomic.AtomicReference;

import edu.unc.robotics.prrts.State;

/**
 * Just shrinking the enormous PRRTStar class
 * 
 * Represents a single configuration in the RRT* tree. The path to the
 * node can be computed by following the parents until null, and then
 * reversing the order. This class is part of the public API, but is
 * also used internally. The package-private members are intentionally
 * not part of the public API as they are subject to change.
 *
 * The public API may safely be accessed while the PRRTStar is running.
 * There is a possibility that the path to a node will change while it
 * is being accessed, but the config member will not change. For
 * efficiency, the config member is exposed as a direct reference an array.
 * It should NOT be modified by the caller.
 */
public class Node<T extends State> {
    public final T config;
    public final boolean inGoal;
    public final AtomicReference<Link<T>> link = new AtomicReference<>();

    public Node(T config, boolean inGoal) {
        this.config = config;
        this.inGoal = inGoal;
        this.link.set(new Link<>(this));
    }

    public Node(T config, boolean inGoal, double linkDist, Link<T> parent) {
        this.config = config;
        this.inGoal = inGoal;

        Link<T> link = new Link<>(this, linkDist, parent);

        this.link.set(link);
        parent.addChild(link);
    }

    public Link<T> setLink(Link<T> oldLink, double linkDist, Link<T> parent) {
        Link<T> newLink = new Link<>(this, linkDist, parent);

        if (!link.compareAndSet(oldLink, newLink)) {
            return null;
        }

        assert newLink.pathDist <= oldLink.pathDist;
        parent.addChild(newLink);
        return newLink;
    }

    /**
     * Returns the configuration of this node in the RRT* tree. The
     * returned value is a direct reference to an array (not a copy)
     * and thus should NOT be modified by the caller.
     *
     * @return the configuration
     */
    public T getConfig() {
        return config;
    }

    /**
     * Returns the parent of this configuration. It is the best known
     * path to this configuration as of the time it is called. The
     * returned value may change while PRRT* is running. If null is
     * returned, the node represents the initial configuration. There
     * are no provisions to return the node's children.
     *
     * @return the nodes parent, or null if this is the root node.
     */
    public Node<T> getParent() {
        Link<T> parent = this.link.get().parent.get();
        return parent == null ? null : parent.node;
    }

}
