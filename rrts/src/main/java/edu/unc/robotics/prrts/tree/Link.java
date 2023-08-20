package edu.unc.robotics.prrts.tree;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unc.robotics.prrts.Path;

/**
 * A doubly-connected directed graph which also has a linked list of siblings.
 */
public class Link {
    private static final Logger _log = Logger.getLogger(Link.class.getName());

    /**
     * head of this link
     * 
     * @Nonnull
     */
    private final Node _node;
    /** length, i.e. cost, of this edge */
    private final double _linkDist;
    /** total path length, i.e. cost, so far */
    private final double _pathDist;
    /**
     * tail of this link
     * it's weird that this is another link instead of a node. maybe that's some
     * sort of infinitesimal speedup?
     * 
     * @Nullable for root
     */
    private final Link _parent;
    /**
     * link to a node that this node is the parent of, if any
     * 
     * @Nullable
     */
    Link _firstChild;
    /**
     * link to a node with the same parent as this node
     * 
     * @Nullable
     */
    Link _nextSibling;

    public Link(Node root) {
        this(root, 0, 0, null);
    }

    /**
     * Create a new link pointing at the node, linkDist away from parent.
     * 
     * @param node     head of this link
     * @param linkDist distance to the parent
     * @param parent   link whose head is the parent node
     */
    public Link(Node node, double linkDist, Link parent) {
        this(node, linkDist, parent._pathDist + linkDist, parent);
    }

    /**
     * make the child the first child, make the current first child into the
     * sibling.
     */
    public void addChild(Link child) {
        child._nextSibling = _firstChild;
        _firstChild = child;
    }

    public boolean isExpired() {
        return _node.get_link() != this;
    }

    public Link removeFirstChild() {
        Link child;
        Link sibling;

        child = _firstChild;
        if (child == null) {
            return null;
        }
        sibling = child._nextSibling;
        _firstChild = sibling;

        child._nextSibling = null;

        return child;
    }

    /** @return true if success */
    public boolean removeChild(final Link child) {
        if (!child.isExpired()) {
            _log.log(Level.WARNING, "attempted to remove unexpired child");
            return false;
        }
        if (child._parent != this) {
            _log.log(Level.WARNING, "attempted to remove child not ours");
            return false;
        }

        Link sibling;
        Link n;
        Link p;

        outer: for (;;) {
            n = _firstChild;

            if (n == child) {
                sibling = child._nextSibling;
                if (_firstChild == child) {
                    _firstChild = sibling;
                    break;
                } else {
                    continue;
                }
            }

            if (n == null) {
                return false;
            }

            for (;;) {
                p = n;

                if (n != null)
                    n = n._nextSibling;

                if (n == null) {
                    return false;
                }

                if (n == child) {
                    sibling = child._nextSibling;

                    // TODO: double check this logic. could the child
                    // now be the first element in the list?

                    if (p._nextSibling == child) {
                        p._nextSibling = sibling;
                        break outer;
                    } else {
                        break;
                    }
                }
            }
        }

        child._nextSibling = null;

        return true;
    }

    public Path path() {
        Node node = get_node();
        List<double[]> configs = new LinkedList<double[]>();
        double pathDist = get_pathDist();
        while (node != null) {
            configs.add(node.get_config());
            node = node.get_parent_node();
        }
        Collections.reverse(configs);
        return new Path(pathDist, configs);
    }

    /** Head of this link */
    public Node get_node() {
        return _node;
    }

    public double get_linkDist() {
        return _linkDist;
    }

    /** Total path length from start to here */
    public double get_pathDist() {
        return _pathDist;
    }

    /** This is only used for removing child nodes */
    public Link get_parent() {
        return _parent;
    }

    public Node get_parent_node() {
        if (_parent == null)
            return null;
        return _parent.get_node();
    }

    //////////////////////////////////////////////////////////////////

    /**
     * @param node   tail of this link
     * @param parent root has a null parent
     */
    private Link(Node node,
            double linkDist,
            double pathDist,
            Link parent) {
        if (node == null)
            throw new IllegalArgumentException();
        if (linkDist < 0)
            throw new IllegalArgumentException();
        if (pathDist < 0)
            throw new IllegalArgumentException();
        _node = node;
        _linkDist = linkDist;
        _pathDist = pathDist;
        _parent = parent;
        _firstChild = null;
        _nextSibling = null;
    }

}
