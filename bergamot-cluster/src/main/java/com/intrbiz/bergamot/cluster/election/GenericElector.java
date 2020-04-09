package com.intrbiz.bergamot.cluster.election;

import static com.intrbiz.bergamot.cluster.util.ZKPaths.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Transaction;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import com.intrbiz.bergamot.cluster.election.model.ElectionMember;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;
import com.intrbiz.bergamot.cluster.util.ZKPaths;

/**
 * A leader, follower election
 */
public abstract class GenericElector
{
    private static final Logger logger = Logger.getLogger(GenericElector.class);
    
    protected static final int SEQUENCE_LENGTH = 10;
    
    protected static final int MAX_START_DELAY = 250;
    
    protected final ZooKeeper zooKeeper;
    
    protected final UUID id;
    
    protected final String parentPath;
    
    protected final String containerPath;
    
    protected final String nodePrefix;
    
    protected final String nodePathPrefix;
    
    protected Consumer<ElectionState> leaderTrigger;
    
    protected String electionNodePath;
    
    protected ElectionState currentState;
    
    public GenericElector(ZooKeeper zooKeeper, String parent, String container, UUID id) throws KeeperException, InterruptedException
    {
        super();
        this.zooKeeper = Objects.requireNonNull(zooKeeper);
        this.id = Objects.requireNonNull(id);
        this.parentPath = zkPath(BERGAMOT, parent);
        this.containerPath = zkPath(this.parentPath, container);
        this.nodePrefix = this.id.toString() + "_";
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
        this.startElection();
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
        ElectionMember member = this.getElectionMembers().get(memberId);
        if (member != null)
        {
            logger.debug("Releasing election member " + member.getPath());
            this.zooKeeper.delete(zkPath(this.containerPath, member.getPath()), -1);
        }
    }
    
    public void releaseMembers(Set<UUID> memberIds) throws KeeperException, InterruptedException
    {
        // Get all the node paths we need to delete
        List<ElectionMember> membersToRelease = new LinkedList<>();
        for (Entry<UUID, ElectionMember> member : this.getElectionMembers().entrySet())
        {
            if (memberIds.contains(member.getKey()))
                membersToRelease.add(member.getValue());
        }
        if (membersToRelease.size() > 0)
        {
            // Delete all nodes as a transaction
            logger.debug("Releasing election members " + membersToRelease);
            Transaction transaction = this.zooKeeper.transaction();
            for (ElectionMember path : membersToRelease)
            {
                transaction.delete(zkPath(this.containerPath, path.getPath()), -1);
            }
            transaction.commit();
        }
    }
    
    public void promoteMember(UUID memberId) throws KeeperException, InterruptedException
    {
        // Get all the node paths we need to delete
        LinkedHashMap<UUID, ElectionMember> members = this.getElectionMembers();
        List<ElectionMember> membersToRelease = new LinkedList<>();
        for (Entry<UUID, ElectionMember> member : members.entrySet())
        {
            if (memberId.equals(member.getKey()))
                break;
            membersToRelease.add(member.getValue());
        }
        if (membersToRelease.size() != members.size() && membersToRelease.size() > 0)
        {
            // Delete all nodes as a transaction
            Transaction transaction = this.zooKeeper.transaction();
            for (ElectionMember member : membersToRelease)
            {
                transaction.delete(zkPath(this.containerPath, member.getPath()), -1);
            }
            transaction.commit();
        }
    }
    
    public void releaseAll() throws KeeperException, InterruptedException
    {
        List<String> paths = this.getElectionNodes();
        // Delete all nodes as a transaction
        logger.debug("Releasing all election members " + paths);
        Transaction transaction = this.zooKeeper.transaction();
        for (String path : paths)
        {
            transaction.delete(zkPath(this.containerPath, path), -1);
        }
        transaction.commit();
    }
    
    public synchronized ElectionState getOurElectionState()
    {
        return this.currentState;
    }
    
    public UUID getLeader() throws KeeperException, InterruptedException
    {
        return this.getElectionMember(0);
    }
    
    public UUID getElectionMember(int level) throws KeeperException, InterruptedException
    {
        List<String> nodes = this.getElectionNodes();
        return nodes.size() > level ? ZKPaths.uuidPrefixFromName(nodes.get(level)) : null;
    }
    
    public int getElectionMemberCount() throws KeeperException, InterruptedException
    {
        return this.getElectionNodes().size();
    }
    
    public LinkedHashMap<UUID, ElectionMember> getElectionMembers() throws KeeperException, InterruptedException
    {
        LinkedHashMap<UUID, ElectionMember> nodeElectionStates = new LinkedHashMap<>();
        int position = 0;
        for (String node : this.getElectionNodes())
        {
            UUID id = ZKPaths.uuidPrefixFromName(node);
            nodeElectionStates.put(id, new ElectionMember(id, position == 0 ? ElectionState.LEADER : ElectionState.FOLLOWER, position, node));
            position ++;
        }
        return nodeElectionStates;
    }
    
    protected synchronized void startElection() throws KeeperException, InterruptedException
    {
        // Wait a randomised period of time before trying to win the election
        try
        {
            Thread.sleep(new SecureRandom().nextInt(MAX_START_DELAY));
        }
        catch (InterruptedException e)
        {
        }
        // Create our election node
        this.createElectionNode();
        // Watch ourself
        this.watchOurself();
        // Do the election
        this.doElection();
    }
    
    protected synchronized void doElection() throws KeeperException, InterruptedException
    {
        // Are we a leader of follower
        List<String> electionNodes = this.getElectionNodes();
        int position = this.findOurPosition(electionNodes);
        logger.debug("Doing election (" + this.toString() + ") we " + this.id + " are " + position + " in " + electionNodes);
        if (position < 0 || position >= electionNodes.size())
        {
            // We couldn't find our node, start the election process again
            this.startElection();
        }
        else if (position == 0)
        {
            // We are the leader
            this.currentState = ElectionState.LEADER;
            logger.debug("We are the leader!");
            // fire callback
            this.leaderTrigger.accept(this.currentState);
        }
        else
        {
            // We are a follower
            this.currentState = ElectionState.FOLLOWER;
            String followerOf = electionNodes.get(position - 1);
            logger.debug("We are a follower of: " + followerOf);
            // Setup a watch on the node to follow
            this.followOtherMember(followerOf);
            // fire callback
            this.leaderTrigger.accept(this.currentState);
        }
    }
    
    protected void createElectionNode() throws KeeperException, InterruptedException
    {
        this.electionNodePath = this.zooKeeper.create(this.nodePathPrefix, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.debug("Created election: " + this.electionNodePath);
    }
    
    protected List<String> getElectionNodes() throws KeeperException, InterruptedException
    {
        List<String> nodes = new ArrayList<>(this.zooKeeper.getChildren(this.containerPath, false));
        Collections.sort(nodes, GenericElector::compareNodes);
        logger.debug("Election nodes: " + nodes);
        return nodes;
    }
    
    protected void followOtherMember(String nodeToFollow) throws KeeperException, InterruptedException
    {
        this.zooKeeper.addWatch(this.containerPath + "/" + nodeToFollow, (watchedEvent) -> {
            logger.debug("Got follower event " + watchedEvent);
            if (watchedEvent.getType() == EventType.NodeDeleted)
            {
                logger.debug("Following (" + watchedEvent.getPath() + ") node has failed");
                try
                {
                    this.doElection();
                }
                catch (KeeperException | InterruptedException e)
                {
                    logger.warn("Failed to determine election status", e);
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
                    logger.warn("Failed to determine election status", e);
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
        return findPosition(sortedNodes, this.id);
    }
    
    protected static int findPosition(List<String> sortedNodes, UUID id)
    {
        String idStr = id.toString();
        int position = 0;
        for (String node : sortedNodes)
        {
            if (node.contains(idStr))
                return position;
            position++;
        }
        return -1;
    }
    
    protected static int compareNodes(String a, String b)
    {
        return Integer.compare(ZKPaths.sequenceFromPath(a), ZKPaths.sequenceFromPath(b));
    }
}
