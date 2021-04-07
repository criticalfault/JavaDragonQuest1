package dq1.core;

import static dq1.core.Settings.*;
import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * EventFactory class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class EventFactory {

    //npc {event_id} {sprite_start_id} {pos_col} {pos_row} \
    // STATIC {sprite_direction} | RANDOM {rect_col} {rect_row} 
    //                                    {rect_width} {rect_height} \
    // {texts_global_var_0} ... {texts_global_var_n} | conditional_msgs \
    //    {var_a} {comparisson_operator} {var_b} {messages_count} 
    //            {texts_global_var_0} ... {texts_global_var_n} \
    //    {var_c} {comparisson_operator} {var_d} 
    //            {messages_count} 
    //            {texts_global_var_0} ... {texts_global_var_n} \
    //    {var_e} {comparisson_operator} 
    //            options 2 (no. of options) 
    //                    2 (default start option [not implemented!]) 
    //                    0 (cancel_option/-1 forces to choose one of options) \
    //                    {yes_text} {messages_count} 
    //                           {texts_global_var_0} ... {texts_global_var_n} \
    //                    {no_text} {messages_count} 
    //                           {texts_global_var_0} ... {texts_global_var_n}
    public static Event createNpcEvent(String[] args) throws Exception {
        Event event = new Event();
        String eventId = args[1]; // {event_id} 
        event.setId(eventId);
        event.setType("npc");
        // {sprite_start_id} 
        int startSpriteId = Integer.parseInt(args[2]);
        BufferedImage tileset = Resource.getImage(RES_CHARS_IMAGE);
        int tilesetCols = tileset.getWidth() / 20;
        int tilesetRows = tileset.getHeight() / 20;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("down"
                , new int[] { startSpriteId + 0, startSpriteId + 1 });

        event.createAnimation("left"
                , new int[] { startSpriteId + 2, startSpriteId + 3 });

        event.createAnimation("up"
                , new int[] { startSpriteId + 4, startSpriteId + 5 });

        event.createAnimation("right"
                , new int[] { startSpriteId + 6, startSpriteId + 7 });
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[3])
                , 16 * Integer.parseInt(args[4]));
        
        String spriteDirection = null;
        int argIndex = 5;
        if (args[argIndex].equals("STATIC")) {
            event.setMovementType(Event.MovementType.STATIC);
            // {sprite_direction}
            spriteDirection = args[++argIndex];
            event.changeAnimation(spriteDirection);
        }
        else if (args[argIndex].equals("RANDOM")) {
            event.setMovementType(Event.MovementType.RANDOM);
            // {rect_col} {rect_row} {rect_width} {rect_height} 
            event.setWalkArea(16 * Integer.parseInt(args[++argIndex])
                    , 16 * Integer.parseInt(args[++argIndex])
                    , 16 * Integer.parseInt(args[++argIndex])
                    , 16 * Integer.parseInt(args[++argIndex]));
        }
        else {
            throw new Exception(
                    "STATIC or RANDOM was expected in npc command !");
        }

        event.setVisible(true);
        event.setBlocked(true);
        event.setFireRequired(true);

        if (args[++argIndex].equals("conditional_msgs")) {
            String src = "    on_event_trigger:\n" +
                "        change_event_turn_to_player \"" + eventId + "\"\n" +
                "        force_redraw\n";
            
            String labelEnd = "label_end_" + UUID.randomUUID().toString();
            while (argIndex < args.length - 1) {
                String varA = args[++argIndex];
                String compOp = args[++argIndex];
                String varB = args[++argIndex];
                // if var1 "==|!=|>|<|>=|<=" var2 "label"
                String labelTrue = "label_true_" + UUID.randomUUID().toString();
                String labelFalse = "label_false_" 
                        + UUID.randomUUID().toString();
                
                src += "        if " + varA + " \"" + compOp + "\" " 
                        + varB + " \"" + labelTrue + "\"\n";
                
                src += "        goto \"" + labelFalse + "\"\n";
                src += "    " + labelTrue + ":\n";
                
                if (!args[++argIndex].equals("options")) {
                    int messages_count = Integer.parseInt(args[argIndex]);
                    String srcTmp = "";
                    int optIndex = argIndex + messages_count + 1;
                    for (int i = 0; i < messages_count; i++) {
                        String cont = i < messages_count - 1 ? "2" : "1";
                        if (i == messages_count - 1 
                                && optIndex < args.length - 1 
                                && args[optIndex].equals("options")) {
                            
                            src += "        show_player_simplified_status\n";
                            cont = "0";
                        }
                        srcTmp += "        show_dialog 1 " + cont + " " 
                                                    + args[++argIndex] + "\n";
                    }
                    src += srcTmp;
                }
                // more arguments with options ?
                if (argIndex + 1 < args.length - 1 
                        && args[argIndex + 1].equals("options")) {
                    
                    argIndex++;
                    //if_option_menu_select 9 3 2 @@option_yes @@option_no 
                    //                      "yes_label" "no_label" "no_label"
                    // 2 (no. of options) 
                    int optionsCount = Integer.parseInt(args[++argIndex]);
                    // 0 (default start option) 
                    // not implemented in if_option_menu_select command !
                    int startOption = Integer.parseInt(args[++argIndex]);
                    // 0 (cancel_option) 
                    int cancelOption = Integer.parseInt(args[++argIndex]);
                    src += "        sleep 250\n";
                    src += "        if_option_menu_select 9 3 " 
                                                    + optionsCount + " ";
                    
                    String[] optionLabels = new String[optionsCount];
                    String[] optionScripts = new String[optionsCount];
                    for (int i = 0; i < optionsCount; i++) {
                        String optionText = args[++argIndex];
                        src += optionText + " ";
                        optionLabels[i] = "label_option_" + i + "_" 
                                                + UUID.randomUUID().toString();
                        
                        int optionMessagesCount 
                                        = Integer.parseInt(args[++argIndex]);
                        
                        if (optionScripts[i] == null) {
                            optionScripts[i] = "";
                        }
                        optionScripts[i] += "    " + optionLabels[i] + ":\n";
                        for (int m = 0; m < optionMessagesCount; m++) {
                            String cont 
                                    = m < optionMessagesCount - 1 ? "2" : "1";
                            
                            optionScripts[i] += "        show_dialog 1 " 
                                    + cont + " " + args[++argIndex] + "\n";
                        }
                        optionScripts[i] += "        goto \"" 
                                                        + labelEnd + "\"\n";
                    }

                    for (int l = 0; l < optionLabels.length; l++) {
                        src += "\"" + optionLabels[l] + "\" ";
                    }
                    // if cancelOption < 0 then force you 
                    // to choose one of options
                    if (cancelOption >= 0) {
                        src += "\"" + optionLabels[cancelOption] + "\"\n";
                    }
                    else {
                        src += "\"" + labelTrue + "\"\n";
                    }
                    
                    for (int i = 0; i < optionsCount; i++) {
                        src += optionScripts[i];
                    }
                }
                else {
                    src += "        goto \"" + labelEnd + "\"\n";
                }
                src += "    " + labelFalse + ":\n";
            }
            src += "    " + labelEnd + ":\n";
            src += "        close_dialog\n";
            if (spriteDirection != null) {
                src +=  "        change_event_animation \"" 
                           + eventId + "\" \"" + spriteDirection + "\"\n";
            }
            src += "        ret\n";
            event.setScript(new Script(src));
        }
        // NPC without conditional messages
        else {
            String src = 
                "    on_event_trigger:\n" +
                "        change_event_turn_to_player \"" + eventId + "\"\n" +
                "        force_redraw\n" +
                "        open_dialog\n";
            
            for (int i = argIndex; i < args.length; i++) {
                String cont = i < args.length - 1 ? "2" : "1";
                src += "        show_dialog 1 " + cont + " " + args[i] + "\n";
            }
            src += "        close_dialog\n";
            if (spriteDirection != null) {
                src +=  "        change_event_animation \"" 
                           + eventId + "\" \"" + spriteDirection + "\"\n";
            }
            src += "        ret\n";
            event.setScript(new Script(src));
        }
        return event;
    }

    // teleport {event_id} {sprite_id} {pos_col} {pos_row} {map_id} \
    //          {player_col} {player_row} {player_direction} \
    //          {sound_id|no_sound|default_sound} {use_fade_effect->true|false]
    //          {musicId} {isDark->true|false} {repelHasNoEffect->true|false} 
    //          {resetRepel->true|false]
    public static Event createTeleportEvent(String[] args) {
        Event event = new Event();
        event.setId(args[1]); // {event_id} 
        event.setType("teleport");
        // {sprite_start_id} 
        int startSpriteId = Integer.parseInt(args[2]);
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { startSpriteId });
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[3])
                , 16 * Integer.parseInt(args[4]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(false);
        event.setFireRequired(false);
        int playerRow = Integer.parseInt(args[7]);
        int playerCol = Integer.parseInt(args[6]);
        String soundSrc = "";
        if (!args[9].equals("no_sound")) {
            String soundId = args[9];
            // 19 = default sound id
            if (args[9].equals("default_sound")) {
                soundId = "19";
            }
            soundSrc = "        play_sound " + soundId + "\n";
        }
        
        String useFadeEffect = Boolean.parseBoolean(args[10]) ? "1" : "0";
        String musicId = args[11];
        String isDark = Boolean.parseBoolean(args[12]) ? "1" : "0"; 
        String repelHasNoEffect = Boolean.parseBoolean(args[13]) ? "1" : "0";
        String resetRepel = Boolean.parseBoolean(args[14]) ? "1" : "0";
        String resetLight  = Boolean.parseBoolean(args[15]) ? "1" : "0";

        String src = "    on_event_trigger:\n" +
                     "        ; entrance or stairs sound id = 19\n" +
                     soundSrc +
                     "        sleep 250\n" +
                     "        teleport \"" + args[5] + "\" " + playerCol + 
                     " " + playerRow + " \"" + args[8] + "\" " + useFadeEffect + 
                     " \"" + musicId + "\" " + isDark + " " + repelHasNoEffect + 
                     " " + resetRepel + " " + resetLight + " \n" +
                     "        ret\n";
                    
        event.setScript(new Script(src));
        return event;
    }

    //key item ids: 46 47 48
    //door {event_id} {sprite_id} {pos_col} {pos_row} 
    //     {persistent->true/false}
    public static Event createDoorEvent(TileMap map, String[] args) {
        Event event = new Event();
        String eventId = args[1];
        boolean persistent = Boolean.parseBoolean(args[5]);
        String localGlobal = persistent ? "##" + map.getId() + "_" : "#";
        String switchVar = localGlobal + eventId + "_door_opened";
        event.setId(eventId); // {event_id} 
        event.setType("door");
        // {sprite_start_id} 
        int startSpriteId = Integer.parseInt(args[2]);
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { startSpriteId });
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[3])
                , 16 * Integer.parseInt(args[4]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(true);
        event.setFireRequired(true);
        String src = 
            "    on_map_enter:\n" +
            "            if_set " + switchVar + " \"hide_event\"\n" +
            "            change_event_visibility \"" + eventId + "\" 1\n" + 
            "            ret\n" +
            "        hide_event:\n" +
            "            change_event_visibility \"" + eventId + "\" 0\n" +
            "            ret\n" +
            "\n" +
            "    on_event_trigger:\n" +
            "            log \"checking key 46\"\n" +
            "            if_not_set ##player_item_46 \"check_key_47\"\n" +
            "            if ##player_item_46 \">\" 0 \"key_46_available\"\n" +
            "        check_key_47:\n" +
            "            log \"checking key 47\"\n" +
            "            if_not_set ##player_item_47 \"check_key_48\"\n" +
            "            if ##player_item_47 \">\" 0 \"key_47_available\"\n" +
            "        check_key_48:\n" +
            "            log \"checking key 48\"\n" +
            "            if_not_set ##player_item_48 \"key_not_available\"\n" +
            "            if ##player_item_48 \">\" 0 \"key_48_available\"\n" +
            "        key_not_available:\n" +
            "            log \"key not available !\"\n" +
            "            open_dialog\n" +
            "            show_dialog 0 1 @@door_player_has_not_keys\n" +
            "            close_dialog\n" +
            "            ret\n" +
            "        key_46_available:\n" +
            "            sub ##player_item_46 ##player_item_46 1\n" +
            "            goto \"key_used_successfully\"\n" +
            "        key_47_available:\n" +
            "            sub ##player_item_47 ##player_item_47 1\n" +
            "            goto \"key_used_successfully\"\n" +
            "        key_48_available:\n" +
            "            sub ##player_item_48 ##player_item_48 1\n" +
            "        key_used_successfully:\n" +
            "            ;open_dialog\n" +
            "            ;show_dialog 0 1 \"Door was opened.\"\n" +
            "            ;close_dialog\n" +
            "            ; door open sound = 15\n" +
            "            play_sound 15\n" +
            "            sleep 500\n" +
            "            change_event_visibility \"" + eventId + "\" 0\n" +
            "            set " + switchVar + " 1\n" +
            "            ret\n";
        
        event.setScript(new Script(src));
        return event;
    }

    //chest {event_id} {pos_col} {pos_row} {item_id / gold_{value}} 
    //      {persistent->true/false}
    public static Event createChestEvent(TileMap map, String[] args) {
        Event event = new Event();
        String eventId = args[1];
        boolean persistent = Boolean.parseBoolean(args[5]);
        String localGlobal = persistent ? "##" + map.getId() + "_" : "#";
        String switchVar = localGlobal + eventId + "_chest_opened";
        event.setId(eventId); // {event_id} 
        event.setType("chest");
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { 23 }); // 23 = chest sprite
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        String itemId = args[4].trim();
        String itemName = "";
        String goldValue = "";
        Item item = null;
        boolean isEquip = false;
        boolean isGold = false;
        if (itemId.startsWith("gold_")) {
            String[] values = itemId.split("_");
            goldValue = "1";
            if (values.length == 2) {
                goldValue = values[1];
            }
            else if (values.length == 3) {
                int min = Integer.parseInt(values[1]);
                int max = Integer.parseInt(values[2]);
                goldValue = "" + Util.random(min, max);
            }
            isGold = true;
        }
        else {
            item = Resource.getItemById(Integer.parseInt(itemId));
            itemName = item.getName();
            isEquip = item.isEquip();
        }
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(false);
        event.setFireRequired(true);
        String src = 
            "    on_map_enter:\n" +
            "            if_set " + switchVar + " \"hide_event\"\n" +
            "            change_event_visibility \"" + eventId + "\" 1\n" +
            "            ret\n" +
            "        hide_event:\n" +
            "            change_event_visibility \"" + eventId + "\" 0\n" +
            "            ret\n" +
            "    on_event_trigger:\n" +
            "            show_player_simplified_status\n" +
            "            change_event_visibility \"" + eventId + "\" 0\n" +
            "            force_redraw\n" +
            "            set " + switchVar + " 1\n" +
            "            ; 13 = chest opened sound\n" +
            "            play_sound 13\n" +
              (isGold ? "            goto \"found_gold\"\n" 
            : isEquip ? "            goto \"found_equip\"\n" 
                      : "            goto \"found_item\"\n" ) +
            "\n" +
            "        found_equip:\n" +
            "            set #chest_tmp_item_id " + itemId + "\n" +
            "            set $$chest_item_name \"" + itemName + "\"\n" +
            "            if ##player_weapon_id \"==\" #chest_tmp_item_id "
                + "\"player_already_have_it\"\n" +    
            "            if ##player_armor_id \"==\" #chest_tmp_item_id "
                + "\"player_already_have_it\"\n" +    
            "            if ##player_shield_id \"==\" #chest_tmp_item_id "
                + "\"player_already_have_it\"\n" +    
            "        equip_player:\n" +    
            "            show_dialog 0 0 @@chest_found_item_1\n" +
            "            show_dialog 0 1 @@chest_found_item_2\n" +
            "            equip_player #chest_tmp_item_id\n" +
            "            goto \"equip_exit\"\n" +
            "        player_already_have_it:\n" +
            "            show_dialog 0 1 @@chest_empty\n" +
            "            goto \"equip_exit\"\n" +
            "        equip_exit:\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +
            "            ret\n" +
            "\n" +
            "        found_item:\n" +
            "            set #chest_tmp_item_id " + itemId + "\n" +
            "            set $$chest_item_name \"" + itemName + "\"\n" +
            "            ; >=0 = itemId \n" +
            "            ;  -1 = no more inventory empty slot\n" +
            "            ;  -2 = already have max allowed number of item\n" +
            "            ;  -3 = player has one of parent items\n" +
            "            can_have_item #chest_tmp_item_id\n" +
            "            if #chest_tmp_item_id \"==\" -3 \"cant_have_more\"\n" +
            "            if #chest_tmp_item_id \"==\" -2 \"cant_have_more\"\n" +
            "            show_dialog 0 0 @@chest_found_item_1\n" +
            "            if #chest_tmp_item_id \"==\" -1 "
                + "\"dialog_with_exchange\"\n" +
            "            show_dialog 0 1 @@chest_found_item_2\n" +
            "            goto \"dialog_continue\"\n" +
            "        dialog_with_exchange:\n" +
            "            show_dialog 0 2 @@chest_found_item_2\n" +
            "        dialog_continue:\n" +
            "            if #chest_tmp_item_id \"==\" -1 \"inventory_full\"\n" +
            "            goto \"inventory_ok\"\n" +
            "        cant_have_more:\n" +
            "            show_dialog 0 1 @@chest_empty\n" +
            "            goto \"item_exit\"\n" +
            "        inventory_full:\n" +
            "            show_dialog 0 0 @@chest_inventory_full\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"exchange_item_label\" \"give_up_item_label\" "
                + "\"give_up_item_label\"\n" +
            "        exchange_item_label:\n" +
            "            show_dialog 0 0 @@chest_what_drop\n" +
            "            sleep 500\n" +
            "            ;discard_item ##item_id $$item_name\n" +
            "            ;##discard_item_id >0 = itemId\n" +
            "            ;                  -1 = doesn't have any items \n" +
            "            ;                  -2 = can't discard the item\n" +
            "            ;                  -3 = canceled\n" +
            "            ;                  -4 = cursed item !\n" +
            "            discard_item ##discard_item_id "
                + "$$discard_item_name\n" +
            "            if ##discard_item_id \"==\" -4 "
                + "\"cursed_item_label\"\n" +
            "            if ##discard_item_id \"==\" -3 "
                + "\"give_up_item_label\"\n" +
            "            if ##discard_item_id \"==\" -2 "
                + "\"cant_discard_item\"\n" +
            "        exchange_ok:\n" +
            "            drop_item ##discard_item_id\n" +
            "            set $$chest_drop_item_name $$discard_item_name\n" +
            "            show_dialog 0 1 @@chest_drop_confirmation\n" +
            "            goto \"inventory_ok\"\n" +
            "        cursed_item_label:\n" +
            "            show_dialog 0 0 @@cursed_item_4\n" +
            "            sleep 500\n" +
            "            goto \"exchange_item_label\"\n" +
            "        cant_discard_item:\n" +
            "            show_dialog 0 0 @@chest_cant_drop\n" +
            "            sleep 500\n" +
            "            goto \"exchange_item_label\"\n" +
            "        give_up_item_label:\n" +
            "            show_dialog 0 1 @@chest_give_up\n" +
            "            goto \"item_exit\"\n" +
            "        inventory_ok:\n" +
            "            set_if_not_exist ##player_item_" + itemId + " 0\n" +
            "            add ##player_item_" + itemId 
                + " ##player_item_" + itemId + " 1\n" +
            "        item_exit:\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +
            "            ret\n" +
            "\n" +
            "        found_gold:\n" +
            "            set ##chest_gold " + goldValue + "\n" +
            "            add ##player_g ##player_g ##chest_gold\n" +
            "            show_player_simplified_status\n" +
            "            open_dialog\n" +
            "            show_dialog 0 1 @@chest_found_gold\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +
            "            ret";
        
        event.setScript(new Script(src));
        return event;
    }

    //item_on_ground {event_id} {pos_col} {pos_row} {item_id} 
    //               {persistent->true/false}
    public static Event createItemOnGroundEvent(TileMap map, String[] args) {
        Event event = new Event();
        String eventId = args[1];
        boolean persistent = Boolean.parseBoolean(args[5]);
        String localGlobal = persistent ? "##" + map.getId() + "_" : "#";
        String switchVar = 
                localGlobal + eventId + "_item_on_ground_already_took";
        
        event.setId(eventId); // {event_id}
        event.setType("item_on_ground");
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { 41 }); // 41 = transparent
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        String itemId = args[4].trim();
        Item item = Resource.getItemById(Integer.parseInt(itemId));
        String itemName = item.getName();
        boolean isEquip = item.isEquip();
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(false);
        event.setFireRequired(true);
        String src = 
            "    on_event_trigger:\n" +
            "            show_player_simplified_status\n" +    
            "            set #item_on_ground_id " + itemId + "\n" +
            "            set $$item_on_ground_item_name \"" 
                                                        + itemName + "\"\n" +
            "            set $$chest_item_name $$item_on_ground_item_name\n" +
            "            show_dialog 0 2 @@item_on_ground\n" +
            "            if_set " + switchVar + " \"player_already_took\"\n";
        if (isEquip) {
            src +=
                "            if ##player_weapon_id \"==\" #item_on_ground_id "
                    + "\"player_already_took\"\n" +    
                "            if ##player_armor_id \"==\" #item_on_ground_id "
                    + "\"player_already_took\"\n" +    
                "            if ##player_shield_id \"==\" #item_on_ground_id "
                    + "\"player_already_took\"\n" +
                "            show_dialog 0 0 @@item_on_ground_found\n";
        }
        else {
            src +=
                "            ; >=0 = itemId \n" +
                "            ;  -1 = no more inventory empty slot\n" +
                "            ;  -2 = you already have maximum "
                                                + "allowed number of item\n" +
                "            ;  -3 = player has one of parent items\n" +
                "            can_have_item #item_on_ground_id\n" +
                "            if #item_on_ground_id \"==\" -3 "
                                                + "\"player_already_took\"\n" +
                "            if #item_on_ground_id \"==\" -2 "
                                                + "\"player_already_took\"\n" +
                "            if #item_on_ground_id \"==\" -1 "
                                                + "\"dialog_with_exchange\"\n" +
                "            show_dialog 0 0 @@item_on_ground_found\n";
        }
        src +=
            "        found_item:\n";
        if (isEquip) {
            src +=
                "            ; take equip\n" +
                "            equip_player " + itemId + "\n";
        }
        else {
            src +=
                "            ; take item\n" +
                "            add_inventory_item " + itemId + "\n";
        }
        src +=
            "            set " + switchVar + " 1\n" +
            "            wait_for_any_key\n" +
            "            goto \"exit_label\"\n" +
            "        dialog_with_exchange:\n" +
            "            show_dialog 0 2 @@item_on_ground_found\n" +
            "            show_dialog 0 0 @@chest_inventory_full\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"exchange_item_label\" \"give_up_item_label\" "
                + "\"give_up_item_label\"\n" +
            "        exchange_item_label:\n" +
            "            show_dialog 0 0 @@chest_what_drop\n" +
            "            sleep 500\n" +
            "            ; discard_item ##item_id $$item_name\n" +
            "            ; ##item_id > 0 = itemId\n" +
            "            ; -1 = player doesn't have any items yet\n" +
            "            ; -2 = can't discard item (important item!)\n" +
            "            ; -3 = canceled\n" +
            "            ; -4 = cursed item !\n" +
            "            discard_item ##discard_item_id $$discard_item_name\n" +
            "            if ##discard_item_id \"==\" -4 "
                + "\"cursed_item_label\"\n" +
            "            if ##discard_item_id \"==\" -3 "
                + "\"give_up_item_label\"\n" +
            "            if ##discard_item_id \"==\" -2 "
                + "\"cant_discard_item\"\n" +
            "        exchange_ok:\n" +
            "            drop_item ##discard_item_id\n" +
            "            set $$chest_drop_item_name $$discard_item_name\n" +
            "            show_dialog 0 0 @@chest_drop_confirmation\n" +
            "            goto \"found_item\"\n" +
            "        cursed_item_label:\n" +
            "            show_dialog 0 0 @@cursed_item_4\n" +
            "            sleep 500\n" +
            "            goto \"exchange_item_label\"\n" +
            "        cant_discard_item:\n" +
            "            show_dialog 0 0 @@chest_cant_drop\n" +
            "            sleep 500\n" +
            "            goto \"exchange_item_label\"\n" +
            "        give_up_item_label:\n" +
            "            show_dialog 0 1 @@chest_give_up\n" +
            "            goto \"exit_label\"\n" +
            "        player_already_took:\n" +
            "            show_dialog 0 1 @@item_on_ground_empty\n" +
            "            goto \"exit_label\"\n" +
            "        exit_label:\n" +
            "            hide_player_simplified_status\n" +    
            "            close_dialog\n" +
            "            ret\n";
        
        event.setScript(new Script(src));
        return event;
    }

    //save_point {event_id} {pos_col} {pos_row}
    public static Event createSavePointEvent(String[] args) {
        Event event = new Event();
        String eventId = args[1];
        event.setId(eventId); // {event_id} 
        event.setType("save_point");
        event.setAnimation(Resource.getImage(RES_SAVE_POINT_IMAGE), 5, 1, 100);
        event.createAnimation("static", new int[] { 0, 1, 2, 3, 4 }); 
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(false);
        event.setFireRequired(true);
        String src = 
            "    on_event_trigger:\n" +
            "        show_player_simplified_status\n" +
            "        show_dialog 0 0 @@game_save_confirmation\n" +
            "        sleep 250\n" +
            "        if_option_menu_select 9 3 2 @@option_yes @@option_no "
                                + "\"yes_label\" \"no_label\" \"no_label\"\n" +
            "        yes_label:\n" +
            "            save_game\n" +
            "        no_label:\n" +
            "            close_dialog\n" +
            "            ret\n";
        
        event.setScript(new Script(src));
        return event;
    }
    
    //inn {event_name} {pos_col} {pos_row} {inn_price}
    public static Event createInnEvent(String[] args) {
        Event event = new Event();
        String eventId = args[1];
        event.setId(eventId); // {event_id} 
        event.setType("inn");
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { 33 }); 
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(true);
        event.setFireRequired(true);
        String src = "" +
            "    on_event_trigger:\n" +
            "            show_player_simplified_status\n" +
            "            set ##inn_price " + args[4] + "\n" +
            "            open_dialog\n" +
            "            show_dialog 1 0 @@inn_welcome\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"yes_label\" \"no_label\" \"no_label\"\n" +
            "        no_label:\n" +
            "            show_dialog 1 1 @@inn_no\n" +
            "            goto \"exit\"\n" +
            "        yes_label:\n" +
            "            if ##player_g \"<\" ##inn_price "
                + "\"no_enough_money\"\n" +
            "            sub ##player_g ##player_g ##inn_price\n" +
            "            show_player_simplified_status\n" +
            "            show_dialog 1 0 @@inn_yes_1\n" +
            "            sleep 500\n" +
            "            save_current_music\n" +
            "            stop_music\n" +
            "            screen_fade_out\n" +
            "            set ##player_hp ##player_max_hp\n" +
            "            set ##player_mp ##player_max_mp\n" +
            "            show_player_simplified_status\n" +
            "            ; 29 = inn sound\n" +
            "            play_sound 29\n" +
            "            sleep 2000\n" +
            "            screen_fade_in\n" +
            "            restore_play_saved_music\n" +
            "            show_dialog 1 2 @@inn_yes_2\n" +
            "            show_dialog 1 1 @@inn_yes_3\n" +
            "            goto \"exit\"\n" +
            "        no_enough_money:\n" +
            "            show_dialog 1 1 @@inn_no_enough_money\n" +
            "        exit:\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +    
            "            ret\n";
        
        event.setScript(new Script(src));
        return event;
    } 

    //shop_item {event_name} {pos_col} {pos_row} \
    //{item_id} {buy_price->value|default_price} 
    //          {sell_price->value|default_price} \
    //{item_id} {buy_price->value|default_price} 
    //          {sell_price->value|default_price} \
    //{item_id} {buy_price->value|default_price} 
    //          {sell_price->value|default_price}
    public static Event createShopItemEvent(String[] args) {
        Event event = new Event();
        String eventId = args[1];
        event.setId(eventId); // {event_id} 
        event.setType("shop_item");
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { 33 }); 
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(true);
        event.setFireRequired(true);
        String src = 
            "    on_event_trigger:\n" +
            "            show_player_simplified_status\n" +
            "            show_dialog 1 0 @@shop_items_welcome\n" +
            "        shop_again:\n" +
            "            if_option_menu_select 9 3 2 @@shop_buy @@shop_sell "
                + "\"buy_label\" \"sell_label\" \"bye_label\"\n" +
            "        ;--- BUY ---\n" +
            "        buy_label:\n" +
            "            show_dialog 1 0 @@shop_items_what_you_want\n" +
            "            ; create shop\n" +
            "            clear_shop\n";
            //"            add_shop_item 43 -1 -1\n" +
            for (int i = 4; i < args.length; i += 3) {
                String itemId = args[i];
                String buyPrice = args[i + 1];
                String sellPrice = args[i + 2];
                buyPrice = buyPrice.equals("default_price") ? "-1" : buyPrice;
                sellPrice = sellPrice.equals("default_price") 
                                                        ? "-1" : sellPrice;
                
                src += "            add_shop_item " + itemId + " " 
                                        + buyPrice + " " + sellPrice + "\n";
            }
        src +=
            "            show_shop_buy ##buy_item_id "
                + "$$buy_item_name ##buy_item_price\n" +
            "            set $$shop_item_name $$buy_item_name\n" +
            "            ; buy operation canceled ?\n" +
            "            if ##buy_item_id \"==\" -3 \"bye_label\"\n" +
            "            ; check if player has enough money\n" +
            "            if ##player_g \"<\" ##buy_item_price "
                + "\"no_enough_money\"\n" +
            "            ; player has no more empty inventory slot\n" +
            "            if ##buy_item_id \"==\" "
                + "-1 \"no_more_empty_inventory_slot\"\n" +
            "            ; player cannot carry more items\n" +
            "            if ##buy_item_id \"==\" "
                + "-2 \"cannot_carry_more_item\"\n" +
            "            \n" +
            "            ; buy selected item\n" +
            "            sub ##player_g ##player_g ##buy_item_price\n" +
            "            show_player_simplified_status\n" +
            "            add_inventory_item ##buy_item_id\n" +
            "            show_dialog 1 2 @@shop_items_buy_confirmed\n" +
            "            goto \"ask_anything_else\"\n" +
            "            \n" +
            "        no_more_empty_inventory_slot:\n" +
            "            show_dialog 1 2 @@shop_slot_not_available\n" +
            "            goto \"ask_anything_else\"\n" +
            "        cannot_carry_more_item:\n" +
            "            show_dialog 1 2 @@shop_items_already_max_size\n" +
            "        ask_anything_else:\n" +
            "            show_dialog 1 0 @@shop_items_anything_else\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"buy_label\" \"bye_label\" \"bye_label\"\n" +
            "        no_enough_money:\n" +
            "            show_dialog 1 2 @@shop_no_enough_money\n" +
            "            goto \"ask_anything_else\"\n" +
            "        ;--- SELL ---\n" +
            "        sell_label:\n" +
            "            has_items_to_sell ##items_to_sell_ret\n" +
            "            if ##items_to_sell_ret \"==\" "
                + "1 \"nothing_to_sell\"\n" +
            "            show_dialog 1 0 @@shop_what_sell\n" +
            "            sleep 500\n" +
            "            show_shop_sell ##sell_item_id "
                + "$$sell_item_name ##sell_item_price\n" +
            "            set $$shop_item_name $$sell_item_name\n" +
            "            set ##shop_item_sell_price ##sell_item_price\n" +
            "            ; select item to sell canceled\n" +
            "            if ##sell_item_id \"==\" -3 \"bye_label\"\n" +
            "            if ##sell_item_price \"<=\" 0 \"cant_sell_item\"\n" +
            "        sell_confirmation:\n" +
            "            show_dialog 1 0 @@shop_sell_confirmation\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"sell_yes_label\" \"sell_anything_else\" "
                + "\"sell_anything_else\"\n" +
            "        sell_yes_label:\n" +
            "        check_cursing_player_item:\n" +
            "            has_player_specific_curse \"##item_cursing_player\" "
                + "##sell_item_id \n" +
            "            if ##item_cursing_player \"==\" 1 "
                + "\"cant_sell_cursing_player_item\" \n" +
            "            goto \"not_is_cursing_player_item\"\n" +
            "        cant_sell_cursing_player_item:\n" +
            "            show_dialog 0 2 @@cursed_item_4\n" +
            "            show_dialog 1 2 @@cursed_item_5\n" +
            "            goto \"sell_anything_else\"\n" +
            "        not_is_cursing_player_item:\n" +    
            "            add ##player_g ##player_g ##sell_item_price\n" +
            "            show_player_simplified_status\n" +
            "            remove_inventory_item ##sell_item_id\n" +
            "        sell_anything_else:    \n" +
            "            show_dialog 1 0 @@shop_sell_anything_else\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"sell_label\" \"bye_label\" \"bye_label\"\n" +
            "            goto \"bye_label\"\n" +
            "        nothing_to_sell:\n" +
            "            show_dialog 1 0 @@shop_nothing_to_sell\n" +
            "            sleep 500\n" +
            "            goto \"bye_label\"\n" +
            "        cant_sell_item:\n" +
            "            show_dialog 1 0 @@shop_cant_sell_item\n" +
            "            sleep 500\n" +
            "            goto \"sell_anything_else\"\n" +
            "\n" +
            "        ;--- EXIT ---\n" +
            "        bye_label:\n" +
            "            show_dialog 1 1 @@shop_bye\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +
            "            ret\n";
        
        event.setScript(new Script(src));
        return event;
    } 

    //shop_equip {event_name} {pos_col} {pos_row} \
    //{item_id} {buy_price->value|default_price} 
    //          {sell_price->value|default_price} \
    //{item_id} {buy_price->value|default_price} 
    //          {sell_price->value|default_price} \
    //{item_id} {buy_price->value|default_price} 
    //          {sell_price->value|default_price}
    public static Event createShopEquipEvent(String[] args) {
        Event event = new Event();
        String eventId = args[1];
        event.setId(eventId); // {event_id} 
        event.setType("shop_equip");
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { 33 }); 
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(true);
        event.setFireRequired(true);
        String src = 
            "    on_event_trigger:\n" +
            "            show_player_simplified_status\n" +
            "            show_dialog 1 0 @@shop_weapon_armor_welcome\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"buy_label\" \"bye_label\" \"bye_label\"\n" +
            "\n" +
            "        ;--- BUY ---\n" +
            "        buy_label:\n" +
            "            show_dialog 1 0 @@shop_weapon_armor_what_you_want\n" +
            "\n" +
            "            ; create shop\n" +
            "            clear_shop\n";
            //"            add_shop_item 43 -1 -1\n" +
            for (int i = 4; i < args.length; i += 3) {
                String itemId = args[i];
                String buyPrice = args[i + 1];
                String sellPrice = args[i + 2];
                buyPrice = buyPrice.equals("default_price") ? "-1" : buyPrice;
                sellPrice = sellPrice.equals("default_price") 
                                                        ? "-1" : sellPrice;
                
                src += "            add_shop_item " + itemId + " " 
                                        + buyPrice + " " + sellPrice + "\n";
            }
        src +=
            "            show_shop_buy_weapon_armor ##buy_item_id "
                + "$$buy_item_name ##buy_item_price "
                + "$$sell_item_name ##sell_item_price\n" +
            "            set $$shop_item_name $$buy_item_name\n" +
            "            set $$shop_current_equip $$sell_item_name\n" +
            "            set ##shop_current_equip_price ##sell_item_price\n" +
            "            ; buy operation canceled ?\n" +
            "            if ##buy_item_id \"==\" -3 \"bye_label\"\n" +
            "            ; check if current equip can be sold\n" +
            "            if ##buy_item_id \"==\" -1 "
                + "\"equip_cannot_be_sold\"\n" +
            "            goto \"equip_can_be_sold\"\n" +
            "        equip_cannot_be_sold:\n" +    
            "            show_dialog 1 2 @@shop_cant_buy_equip\n" +
            "            goto \"ask_anything_else\"\n" +
            "        equip_can_be_sold:\n" +    
            "            ; check if player has enough money\n" +
            "            if ##player_g \"<\" ##buy_item_price "
                + "\"no_enough_money\"\n" +
            "            ; check if it's necessary to make exchange\n" +
            "            if ##sell_item_price \">\" 0 \"equip_exchange\"\n" +
            "            goto \"equip_exchange_ignore\"\n" +
            "        equip_exchange:\n" +
            "            ; buy selected item\n" +
            "            show_dialog 1 2 "
                + "@@shop_weapon_armor_buy_confirmation_1\n" +
            "            show_dialog 1 2 "
                + "@@shop_weapon_armor_buy_confirmation_2\n" +
            "            show_dialog 1 0 "
                + "@@shop_weapon_armor_buy_confirmation_3\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"equip_exchange_yes_label\" \"equip_exchange_no_label\" "
                + "\"equip_exchange_no_label\"\n" +
            "        equip_exchange_yes_label:\n" +
            "            add ##player_g ##player_g ##sell_item_price\n" +
            "        equip_exchange_ignore:\n" +
            "            sub ##player_g ##player_g ##buy_item_price\n" +
            "            show_player_simplified_status\n" +
            "            equip_player ##buy_item_id\n" +
            "            show_dialog 1 0 @@shop_weapon_armor_thanks\n" +
            "            sleep 500\n" +
            "            goto \"ask_anything_else\"\n" +
            "        equip_exchange_no_label:\n" +
            "            show_dialog 1 2 "
                + "@@shop_weapon_armor_buy_confirmation_no\n" +
            "            goto \"ask_anything_else\"\n" +
            "        ask_anything_else:\n" +
            "            show_dialog 1 0 @@shop_weapon_armor_anything_else\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"buy_label\" \"bye_label\" \"bye_label\"\n" +
            "        no_enough_money:\n" +
            "            show_dialog 1 2 @@shop_no_enough_money\n" +
            "            goto \"ask_anything_else\"\n" +
            "        ;--- EXIT ---\n" +
            "        bye_label:\n" +
            "            show_dialog 1 1 @@shop_weapon_armor_bye\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +
            "            ret\n";
        
        event.setScript(new Script(src));
        return event;
    } 

    // shop_keys {event_name} {pos_col} {pos_row} {key_price}
    public static Event createShopKeysEvent(String[] args) {
        Event event = new Event();
        String eventId = args[1];
        event.setId(eventId); // {event_id} 
        event.setType("shop_keys");
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { 33 }); 
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(true);
        event.setFireRequired(true);
        String keyPrice = args[4];
        String src = 
            "    on_event_trigger:\n" +
            "            set #key_item_id 46\n" +
            "            set ##shop_key_price " + keyPrice + "\n" +
            "            show_player_simplified_status\n" +
            "            show_dialog 1 0 @@shop_keys_welcome\n" +
            "        shop_again:\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"yes_label\" \"bye_label\" \"bye_label\"\n" +
            "            goto \"bye_label\"\n" +
            "        yes_label:\n" +
            "            ; check if player has enough money\n" +
            "            if ##player_g \"<\" ##shop_key_price "
                + "\"no_enough_money\"\n" +
            "            ; player can't buy more keys\n" +
            "            if ##player_item_46 \">=\" 6 "
                + "\"cannot_buy_more_keys\"\n" +
            "            ; key purchased\n" +
            "            sub ##player_g ##player_g ##shop_key_price\n" +
            "            show_player_simplified_status\n" +
            "            add_inventory_item #key_item_id\n" +
            "            show_dialog 1 0 @@shop_keys_purchased\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"yes_label\" \"bye_label\" \"bye_label\"\n" +
            "            goto \"bye_label\"\n" +
            "        cannot_buy_more_keys:\n" +
            "            show_dialog 1 2 @@shop_keys_cant_sell_anymore\n" +
            "            goto \"bye_label\"\n" +
            "        no_enough_money:\n" +
            "            show_dialog 1 2 @@shop_keys_no_enough_money\n" +
            "            goto \"bye_label\"\n" +
            "        bye_label:\n" +
            "            show_dialog 1 1 @@shop_keys_bye\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +
            "            ret";
        
        event.setScript(new Script(src));
        return event;
    } 

    // shop_fairy_water {event_name} {pos_col} {pos_row} {fairy_water_price}
    public static Event createShopFairyWaterEvent(String[] args) {
        Event event = new Event();
        String eventId = args[1];
        event.setId(eventId); // {event_id} 
        event.setType("shop_fairy_water");
        BufferedImage tileset = Resource.getImage(RES_TILESET_IMAGE);
        int tilesetCols = tileset.getWidth() / 16;
        int tilesetRows = tileset.getHeight() / 16;
        event.setAnimation(tileset, tilesetCols, tilesetRows, 100);
        event.createAnimation("static", new int[] { 33 }); 
         // {pos_col} {pos_row} 
        event.setLocation(16 * Integer.parseInt(args[2])
                , 16 * Integer.parseInt(args[3]));
        
        event.setMovementType(Event.MovementType.STATIC);
        event.setVisible(true);
        event.setBlocked(true);
        event.setFireRequired(true);
        String fairyWaterPrice = args[4];
        String src = 
            "    on_event_trigger:\n" +
            "            set #fairy_water_id 45\n" +
            "            set ##shop_fairy_water_price " 
                + fairyWaterPrice + "\n" +
            "            show_player_simplified_status\n" +
            "            show_dialog 1 0 @@shop_fairy_water\n" +
            "        shop_again:\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"yes_label\" \"bye_label\" \"bye_label\"\n" +
            "            goto \"bye_label\"\n" +
            "        yes_label:\n" +
            "            ; check if player has enough money\n" +
            "            if ##player_g \"<\" ##shop_fairy_water_price "
                + "\"no_enough_money\"\n" +
            "            ; >=0 = itemId \n" +
            "            ;  -1 = no more inventory empty slot\n" +
            "            ;  -2 = you already have maximum "
                + "allowed number of item\n" +
            "            can_have_item #fairy_water_id\n" +
            "            if #fairy_water_id \"<\" 0 \"cant_buy_more\"\n" +
            "        inventory_ok:\n" +
            "            ; fairy water purchased\n" +
            "            sub ##player_g ##player_g ##shop_fairy_water_price\n" +
            "            show_player_simplified_status\n" +
            "            add_inventory_item #fairy_water_id\n" +
            "            show_dialog 1 0 @@shop_fairy_water_purchased\n" +
            "            if_option_menu_select 9 3 2 @@option_yes @@option_no "
                + "\"yes_label\" \"bye_label\" \"bye_label\"\n" +
            "            goto \"bye_label\"\n" +
            "        cant_buy_more:\n" +
            "            show_dialog 1 2 "
                + "@@shop_fairy_water_cannot_carry_anymore\n" +
            "            goto \"bye_label\"\n" +
            "        no_enough_money:\n" +
            "            show_dialog 1 2 @@shop_fairy_water_no_enough_money\n" +
            "            goto \"bye_label\"\n" +
            "        bye_label:\n" +
            "            show_dialog 1 1 @@shop_fairy_water_bye\n" +
            "            close_dialog\n" +
            "            hide_player_simplified_status\n" +
            "            ret\n";
        
        event.setScript(new Script(src));
        return event;
    } 

}


