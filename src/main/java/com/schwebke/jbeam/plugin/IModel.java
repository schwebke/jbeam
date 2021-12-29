package com.schwebke.jbeam.plugin;

import com.schwebke.jbeam.model.*;


public interface IModel {

    void addNode(Node node);

    void addBeam(Beam beam);

    int getNodeIndex(Node node);

    int getBeamIndex(Beam node);

    Node getIndexNode(int id);

    Beam getIndexBeam(int id);

    Iterable<Node> getNodeIterator();

    Iterable<Beam> getBeamIterator();
}
