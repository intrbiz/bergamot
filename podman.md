bergamotmonitoring/bergamot-ui-nginx

podman pod create --publish 8080:8080 --publish 5701:5701 --name bergamot

podman create --pod bergamot --name bergamot-ui-nginx --restart always --pull always bergamotmonitoring/bergamot-ui-nginx:4.0.0-SNAPSHOT

podman create --pod bergamot --name bergamot-ui --restart always --pull always --volume /etc/bergamot:/etc/bergamot bergamotmonitoring/bergamot-ui:4.0.0-SNAPSHOT





podman pod create --publish 15080:15080 --name bergamot

podman create --pod bergamot --name bergamot-worker --restart always --env hazelcast.nodes=bergamot-dev-ui1.bergamot-dev-cluster.dev-bergamot-cloud.user.intrbiz.cloud:5701,bergamot-dev-ui2.bergamot-dev-cluster.dev-bergamot-cloud.user.intrbiz.cloud:5701,bergamot-dev-ui3.bergamot-dev-cluster.dev-bergamot-cloud.user.intrbiz.cloud:5701 bergamotmonitoring/bergamot-worker:4.0.0-SNAPSHOT




