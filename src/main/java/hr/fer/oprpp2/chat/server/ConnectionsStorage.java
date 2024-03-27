package hr.fer.oprpp2.chat.server;

import java.net.SocketAddress;
import java.util.*;

public class ConnectionsStorage {
    private final HashMap<SocketAddress, List<ConnectionDescriptor>> connections = new HashMap<>();

    public ConnectionsStorage() {
    }

    public void storeConnection(SocketAddress socketAddress, ConnectionDescriptor connectionDescriptor) {
        if (connections.containsKey(socketAddress)) {
            connections.get(socketAddress).add(connectionDescriptor);
        } else {
            connections.put(socketAddress, new ArrayList<>(List.of(connectionDescriptor)));
        }
    }

    public void removeConnection(SocketAddress socketAddress, ConnectionDescriptor connectionDescriptor) {
        List<ConnectionDescriptor> connectionDescriptors = getConnectionsForSocketAddress(socketAddress);

        if (connectionDescriptors.isEmpty()) return;

        connections.put(
                socketAddress,
                connectionDescriptors.stream().filter(
                        desc -> desc.UID() != connectionDescriptor.UID()
                ).toList()
        );
    }

    public List<ConnectionDescriptor> getAllConnectionDescriptors() {
        return connections.values().stream().flatMap(Collection::stream).toList();
    }


    public Optional<ConnectionDescriptor> getConnectionDescriptorByRandKey(SocketAddress address, long randKey) {
        List<ConnectionDescriptor> connectionDescriptors = getConnectionsForSocketAddress(address);

        if (connectionDescriptors == null) return Optional.empty();

        return connectionDescriptors.stream().filter(desc -> desc.clientsRandKey() == randKey).findFirst();
    }

    public Optional<ConnectionDescriptor> getConnectionDescriptorByUID(SocketAddress address, long UID) {
        List<ConnectionDescriptor> connectionDescriptors = getConnectionsForSocketAddress(address);

        if (connectionDescriptors.isEmpty()) return Optional.empty();

        return connectionDescriptors.stream().filter(desc -> desc.UID() == UID).findFirst();
    }

    private List<ConnectionDescriptor> getConnectionsForSocketAddress(SocketAddress address) {
        return connections.get(address);
    }


}
