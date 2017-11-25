package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class UpdateMessage extends MapLike {
    public static final String TYPE = "UPDATE";
    List<MapLike> changes = new ArrayList<>();

    public UpdateMessage(List<MapLike> changes) {
        this.changes = changes;
    }

    public UpdateMessage(Map<String, Object> from) {
        super(from);
        Collection<Map<String, Object>> updateMaps = (Collection<Map<String, Object>>) from.get("updates");
        this.changes = updateMaps.stream().map(map -> {
            switch ((String) map.get("type")) {
                case AppearData.TYPE:
                    return new AppearData(map);
                case DisappearData.TYPE:
                    return new DisappearData(map);
                case StateChangeData.TYPE:
                    return new StateChangeData(map);
                default:
                    return null;
            }
        }).collect(toList());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("updates", changes.stream().map(MapLike::toMap).collect(toList()));
        return result;
    }
}
