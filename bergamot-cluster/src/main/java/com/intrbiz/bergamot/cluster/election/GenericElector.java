package com.intrbiz.bergamot.cluster.election;

import static com.intrbiz.bergamot.cluster.util.ZKPaths.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import com.intrbiz.bergamot.cluster.election.model.ElectionMember;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;

/**
 * A leader, follower election
 */
public abstract class GenericElector
{
    private static final Logger logger = Logger.getLogger(GenericElector.class);
    
    protected static final int SEQUENCE_LENGTH = 10;
    
    private final ZooKeeper zooKeeper;
    
    private final UUID id;
    
    private final String parentPath;
    
    private final String containerPath;
    
    private final String nodePrefix;
    
    private final int nodePrefixLength;
    
    private final String nodePathPrefix;
    
    private Consumer<ElectionState> leaderTrigger;
    
    private String electionNodePath;
    
    private ElectionState currentState;
    
    public GenericElector(ZooKeeper zooKeeper, String parent, String container, UUID id) throws KeeperException, InterruptedException
    {
        super();
        this.zooKeeper = Objects.requireNonNull(zooKeeper);
        this.id = Objects.requireNonNull(id);
        this.parentPath = zkPath(BERGAMOT, parent);
        this.containerPath = zkPath(this.parentPath, container);
        this.nodePrefix = this.id.toString() + "_";
        this.nodePrefixLength = this.nodePrefix.length();
        this.nodePathPrefix = this.containerPath + "/" + this.nodePrefix;
        // Ensure registration nodes are there
        this.createRoot();
        this.createParent();
        this.createContainer();
    }
    
    private void createRoot() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(BERGAMOT, false) == null)
        {
            this.zooKeeper.create(BERGAMOT, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
        }
    }
    
    private void createParent() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(this.parentPath, false) == null)
        {
            this.zooKeeper.create(this.parentPath, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
        }
    }
    
    private void createContainer() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(this.containerPath, false) == null)
        {
            this.zooKeeper.create(this.containerPath, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
        }
    }
    
    public synchronized ElectionState elect(Consumer<ElectionState> leaderTrigger) throws KeeperException, InterruptedException
    {
        this.leaderTrigger = Objects.requireNonNull(leaderTrigger);
        // Create our election node
        this.createElectionNode();
        // Watch ourself
        this.watchOurself();
        // Do the election
        this.doElection();
        // Return the election state
        return this.currentState;
    }
    
    public synchronized void release() throws KeeperException, InterruptedException
    {
        if (this.electionNodePath != null)
        {
            this.deleteElectionNode();
        }
    }
    
    public void releaseMember(UUID memberId) throws KeeperException, InterruptedException
    {
        String idStr = memberId.toString();
        for (String path : this.zooKeeper.getChildren(this.containerPath, false))
        {
            if (path.contains(idStr))
            {
                this.zooKeeper.delete(zkPath(this.containerPath, path), -1);
            }
        }
    }
    
    public synchronized ElectionState getOurElectionState()
    {
        return this.currentState;
    }
    
    public UUID getLeader() throws KeeperException, InterruptedException
    {
        List<String> nodes = this.getElectionNodes();
        return nodes.size() > 0 ? UUID.fromString(nodes.get(0).substring(0, this.nodePrefixLength - 1)) : null;
    }
    
    public Map<UUID, ElectionMember> getElectionMembers()
    {
        Map<UUID, ElectionMember> nodeElectionStates = new HashMap<>();
        try
        {
            int position = 0;
            for (String node : this.getElectionNodes())
            {
                UUID id = UUID.fromString(node.substring(0, this.nodePrefixLength - 1));
                nodeElectionStates.put(id, new ElectionMember(position == 0 ? ElectionState.LEADER : ElectionState.FOLLOWER, position));
                position ++;
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to get election members", e);
        }
        return nodeElectionStates;
    }
    
    protected synchronized void doElection() throws KeeperException, InterruptedException
    {
        // Get the nodes that are part of the election
        List<String> electionNodes = this.getElectionNodes();
        // What position are we in the election
        int position = this.findOurPosition(electionNodes);
        // Sanity check
        if (position < 0 || position >= electionNodes.size())
        {
            // Shite something has gone very wrong
            this.currentState = ElectionState.FAILED;
            logger.fatal("Leader election went horribly wrong!");
            return;
        }
        // Are we a leader of follower
        if (position == 0)
        {
            // We are the leader
            this.currentState = ElectionState.LEADER;
            logger.info("We are the leader!");
            // fire callback
            this.leaderTrigger.accept(this.currentState);
        }
        else
        {
            // We are a follower
            this.currentState = ElectionState.FOLLOWER;
            String followerOf = electionNodes.get(position - 1);
            logger.info("We are a follower of: " + followerOf);
            // Setup a watch on the node to follow
            this.followOtherMember(followerOf);
            // fire callback
            this.leaderTrigger.accept(this.currentState);
        }
    }
    
    protected void createElectionNode() throws KeeperException, InterruptedException
    {
        this.electionNodePath = this.zooKeeper.create(this.nodePathPrefix, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info("Created election: " + this.electionNodePath);
    }
    
    protected List<String> getElectionNodes() throws KeeperException, InterruptedException
    {
        List<String> nodes = new ArrayList<>(this.zooKeeper.getChildren(this.containerPath, false));
        Collections.sort(nodes, GenericElector::compareNodes);
        logger.info("Election nodes: " + nodes);
        return nodes;
    }
    
    protected void followOtherMember(String nodeToFollow) throws KeeperException, InterruptedException
    {
        this.zooKeeper.addWatch(this.containerPath + "/" + nodeToFollow, (watchedEvent) -> {
            logger.debug("Got follower event " + watchedEvent);
            if (watchedEvent.getType() == EventType.NodeDeleted)
            {
                logger.info("Following (" + watchedEvent.getPath() + ") node has failed");
                try
                {
                    this.doElection();
                }
                catch (KeeperException | InterruptedException e)
                {
                    logger.info("Failed to determine election status", e);
                }
            }
        }, AddWatchMode.PERSISTENT);
    }
    
    protected void watchOurself() throws KeeperException, InterruptedException
    {
        this.zooKeeper.addWatch(this.electionNodePath, (watchedEvent) -> {
            logger.debug("Got self event " + watchedEvent);
            if (watchedEvent.getType() == EventType.NodeDeleted)
            {
                try
                {
                    this.doElection();
                }
                catch (KeeperException | InterruptedException e)
                {
                    logger.info("Failed to determine election status", e);
                }
            }
        }, AddWatchMode.PERSISTENT);
    }
    
    protected void deleteElectionNode() throws KeeperException, InterruptedException
    {
        this.zooKeeper.delete(this.containerPath + "/" + this.electionNodePath, -1);
    }
    
    protected int findOurPosition(List<String> sortedNodes)
    {
        String idStr = this.id.toString();
        int position = -1;
        for (String node : sortedNodes)
        {
            position++;
            if (node.contains(idStr))
                break;
        }
        return position;
    }
    
    protected static int nodeSequence(String node)
    {
        return Integer.parseInt(node.substring(node.length() - SEQUENCE_LENGTH));
    }
    
    protected static int compareNodes(String a, String b)
    {
        return Integer.compare(nodeSequence(a), nodeSequence(b));
    }
}
