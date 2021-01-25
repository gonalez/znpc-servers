/*
 *
 * ZNServersNPC
 * Copyright (C) 2019 Gaston Gonzalez (ZNetwork)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package ak.znetwork.znpcservers.deserializer;

import ak.znetwork.znpcservers.ServersNPC;
import ak.znetwork.znpcservers.npc.ZNPC;
import ak.znetwork.znpcservers.npc.enums.NPCItemSlot;
import ak.znetwork.znpcservers.npc.enums.types.NPCType;
import ak.znetwork.znpcservers.utils.Utils;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ZNPCDeserializer implements JsonDeserializer<ZNPC> {

    private final ServersNPC serversNPC;

    public ZNPCDeserializer(ServersNPC serversNPC) {
        this.serversNPC = serversNPC;
    }

    public ZNPC deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        try {
            // Get equipment values
            HashMap<String, String> configMap = ServersNPC.getGson().fromJson(jsonObject.get("npcEquipments"), HashMap.class);

            // Load npc equipment
            EnumMap<NPCItemSlot, Material> loadMap = new EnumMap<>(NPCItemSlot.class);
            configMap.forEach((s, s2) -> loadMap.put(NPCItemSlot.fromString(s), Material.getMaterial(s2)));

            ZNPC npc = new ZNPC(this.serversNPC, jsonObject.get("id").getAsInt(), jsonObject.get("lines").getAsString(), jsonObject.get("skin").getAsString(), jsonObject.get("signature").getAsString(), ServersNPC.getGson().fromJson(jsonObject.get("location"), Location.class), NPCType.fromString(jsonObject.get("npcType").getAsString()), loadMap, jsonObject.get("save").getAsBoolean());

            // Fix NPC disappearing after world load/unload update
            npc.setWorldName(jsonObject.get("location").getAsJsonObject().get("world").getAsString());
            //

            JsonElement jsonObject1 = jsonObject.get("pathName");
            if (jsonObject1 != null && !jsonObject1.getAsString().equalsIgnoreCase("none"))
                npc.setPathName(jsonObject1.getAsString());

            npc.setActions(ServersNPC.getGson().fromJson(jsonObject.get("actions"), List.class)); // Load actions..

            npc.setHasLookAt(jsonObject.get("hasLookAt").getAsBoolean());
            npc.setHasGlow(jsonObject.get("hasGlow").getAsBoolean());
            npc.setHasMirror(jsonObject.get("hasMirror").getAsBoolean());
            npc.setHasToggleHolo(jsonObject.get("hasToggleHolo").getAsBoolean());
            npc.setHasToggleName(jsonObject.get("hasToggleName").getAsBoolean());
            npc.setReversePath(jsonObject.get("isReversePath").getAsBoolean());

            if (Utils.isVersionNewestThan(9))
                npc.toggleGlow(Optional.empty(), jsonObject.get("glowName").getAsString(), false);

            // Customization
            if (jsonObject.get("customizationMap") != null) {
                npc.setCustomizationMap(ServersNPC.getGson().fromJson(jsonObject.get("customizationMap"), new TypeToken<HashMap<String, List>>() {
                }.getType())); // Load actions..
            }

            return npc;
        } catch (Exception e) {
            throw new RuntimeException("Could not deserialize npc", e);
        }
    }
}