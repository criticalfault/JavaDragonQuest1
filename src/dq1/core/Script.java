package dq1.core;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Script class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Script {

    private static final Map<String, Object> VARS = new HashMap();
    private static final Map<String, Command> GLOBAL_COMMANDS = new HashMap<>();
    
    static {
        registerGlobalCommand("nop", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                return false;
            }
        });
        
        registerGlobalCommand("log", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                System.out.print("log: ");
                for (String arg : args) {
                    System.out.print(getArgument(script, arg));
                }
                System.out.println();
                return false;
            }
        });
        
        registerGlobalCommand("set", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                setValue(script, args.get(0), args.get(1));
                return false;
            }
        });

        registerGlobalCommand("set_if_not_exist", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String arg = args.get(0);
                if (!isValidVar(arg)) {
                    throw new Exception("Invalid variable argument '" + arg 
                            + "' at line " + script.getLineNumber());
                }
                Object value;
                if (isLocalVar(arg)) {
                    value = script.getLocalVars().get(arg);
                }
                else if (isGlobalVar(arg)) {
                     value = VARS.get(arg);
                }
                else {
                    value = Game.getTexts() != null 
                            ? Game.getTexts().get(arg) : null;
                }
                if (value == null) {
                    setValue(script, args.get(0), args.get(1));
                }
                return false;
            }
        });

        registerGlobalCommand("add", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                int a = (Integer) getArgument(script, args.get(1));
                int b = (Integer) getArgument(script, args.get(2));
                setValue(script, args.get(0), String.valueOf(a + b));
                return false;
            }
        });

        registerGlobalCommand("sub", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                int a = (Integer) getArgument(script, args.get(1));
                int b = (Integer) getArgument(script, args.get(2));
                setValue(script, args.get(0), String.valueOf(a - b));
                return false;
            }
        });

        registerGlobalCommand("mul", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                int a = (Integer) getArgument(script, args.get(1));
                int b = (Integer) getArgument(script, args.get(2));
                setValue(script, args.get(0), String.valueOf(a * b));
                return false;
            }
        });
        
        registerGlobalCommand("div", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                int a = (Integer) getArgument(script, args.get(1));
                int b = (Integer) getArgument(script, args.get(2));
                setValue(script, args.get(0), String.valueOf(a / b));
                return false;
            }
        });

        registerGlobalCommand("goto", new Command() {
            
            private int gotoInstructionPointer;
            
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                gotoInstructionPointer = -1;
                String label = (String) getArgument(script, args.get(0));
                gotoInstructionPointer = script.labels.get(label);
                return false;
            }

            @Override
            public void nextLineNumber(Script script) {
                script.instructionPointer = gotoInstructionPointer;
            }
            
        });
        
        registerGlobalCommand("if", new Command() {
            
            private int gotoInstructionPointer;
            
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                Object a = getArgument(script, args.get(0));
                String comp = (String) getArgument(script, args.get(1));
                Object b = getArgument(script, args.get(2));
                String label = (String) getArgument(script, args.get(3));
                boolean condition = false;
                if (a instanceof String && b instanceof String) {
                    switch (comp) {
                        case "==": condition = a.equals(b); break;
                        case "!=": condition = !(a.equals(b)); break;
                        default: 
                            throw new Exception("Invalid operation for "
                                    + "string vs string comparisson !");
                    }
                }
                else {
                    int ai = (Integer) a;
                    int bi = (Integer) b;
                    switch (comp) {
                        case ">": condition = ai > bi; break;
                        case "<": condition = ai < bi; break;
                        case "==": condition = ai == bi; break;
                        case "!=": condition = ai != bi; break;
                        case ">=": condition = ai >= bi; break;
                        case "<=": condition = ai <= bi; break;
                    }
                }
                gotoInstructionPointer = script.instructionPointer + 1;
                if (condition) {
                    gotoInstructionPointer = script.labels.get(label);
                }
                return false;
            }

            @Override
            public void nextLineNumber(Script script) {
                script.instructionPointer = gotoInstructionPointer;
            }
            
        });

        registerGlobalCommand("if_set", new Command() {

            private int gotoInstructionPointer;

            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String arg = args.get(0);
                String label = (String) getArgument(script, args.get(1));
                if (!isValidVar(arg)) {
                    throw new Exception("Invalid variable argument '" + arg 
                            + "' at line " + script.getLineNumber());
                }
                Object value;
                if (isLocalVar(arg)) {
                    value = script.getLocalVars().get(arg);
                }
                else if (isGlobalVar(arg)) {
                     value = VARS.get(arg);
                }
                else {
                    value = Game.getTexts() != null 
                            ? Game.getTexts().get(arg) : null;
                }
                gotoInstructionPointer = script.instructionPointer + 1;
                if (value != null) {
                    gotoInstructionPointer = script.labels.get(label);
                }
                return false;
            }
            
            @Override
            public void nextLineNumber(Script script) {
                script.instructionPointer = gotoInstructionPointer;
            }
            
        });
        
        registerGlobalCommand("if_not_set", new Command() {

            private int gotoInstructionPointer;

            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String arg = args.get(0);
                String label = (String) getArgument(script, args.get(1));
                if (!isValidVar(arg)) {
                    throw new Exception("Invalid variable argument '" + arg 
                            + "' at line " + script.getLineNumber());
                }
                Object value;
                if (isLocalVar(arg)) {
                    value = script.getLocalVars().get(arg);
                }
                else if (isGlobalVar(arg)) {
                     value = VARS.get(arg);
                }
                else {
                    value = Game.getTexts() != null 
                            ? Game.getTexts().get(arg) : null;
                }
                gotoInstructionPointer = script.instructionPointer + 1;
                if (value == null) {
                    gotoInstructionPointer = script.labels.get(label);
                }
                return false;
            }
            
            @Override
            public void nextLineNumber(Script script) {
                script.instructionPointer = gotoInstructionPointer;
            }
            
        });
     
        // if_option_menu_select col row 3 (just an example, number of options) 
        //        @@yes @@no @@more_option 
        //        "yes_label" "no_label" "more_option_label" "cancel_label"
        registerGlobalCommand("if_option_menu_select", new Command() {

            private int gotoInstructionPointer;

            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                int col = (Integer) getArgument(script, args.get(0));
                int row = (Integer) getArgument(script, args.get(1));
                int optionsCount = (Integer) getArgument(script, args.get(2));
                String[] options = new String[optionsCount];
                for (int i = 0; i < options.length; i++) {
                    options[i] = 
                            getArgument(script, args.get(3 + i)).toString();
                }
                int selectedOption = 
                        Dialog.showOptionsMenu(col, row, 0, 0, -1, options);
                
                if (selectedOption == -1) {
                    String label = getArgument(script
                            , args.get(3 + 2 * optionsCount)).toString();
                    
                    gotoInstructionPointer = script.labels.get(label);
                }
                else {
                    String label = getArgument(script, args.get(
                            3 + optionsCount + selectedOption)).toString();
                    
                    gotoInstructionPointer = script.labels.get(label);
                }
                return false;
            }
            
            @Override
            public void nextLineNumber(Script script) {
                script.instructionPointer = gotoInstructionPointer;
            }
            
        });

        //can_have_item ##item_id
        //##item_id >0 = itemId
        //          -1 = no more inventory empty slot
        //          -2 = you already have maximum allowed number of item
        //          -3 = player has one of parent items
        registerGlobalCommand("can_have_item", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                int itemId 
                    = (Integer) getArgument(script, args.get(0));
                
                Item item = Resource.getItemById(itemId);
                int canAddMoreItem 
                        = Inventory.checkCanAddItem(item.getId());

                itemId = canAddMoreItem < 0 
                                ? canAddMoreItem : item.getId();
                
                if (itemId > 0 && item.hasPlayerParentItem()) {
                    itemId = -3;
                }

                setValue(script, args.get(0), "" + itemId);
                return false;
            }
        });
        
        //show_shop_buy ##buy_item_id $$buy_item_name ##buy_item_price
        //##buy_item_id >0 = itemId
        //              -1 = no more inventory empty slot
        //              -2 = you already have maximum allowed number of item
        //              -3 = canceled
        // Note.: used only for items
        registerGlobalCommand("show_shop_buy", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String selectedItemIdVar = args.get(0);
                String selectedItemNameVar = args.get(1);
                String selectedItemBuyPriceVar = args.get(2);

                Item selectedItem = Shop.showShopBuyItem();
                if (selectedItem != null) {
                    int canAddMoreItem 
                            = Inventory.checkCanAddItem(selectedItem.getId());
                    
                    int itemId = canAddMoreItem < 0 
                                    ? canAddMoreItem : selectedItem.getId();
                    
                    setValue(script, selectedItemIdVar, "" + itemId);
                    
                    setValue(script, selectedItemNameVar
                                        , "\"" + selectedItem.getName() + "\"");
                    
                    setValue(script, selectedItemBuyPriceVar
                                                , "" + selectedItem.getBuy());
                }
                else {
                    // canceled
                    setValue(script, selectedItemIdVar, "-3");
                    setValue(script, selectedItemNameVar, "\"\"");
                    setValue(script, selectedItemBuyPriceVar, "-3");
                }
                return false;
            }
        });
        
        //has_items_to_sell ##ret_var
        //##ret_var == 1 = true (no items to sell)
        //          == 0 = false (player has items to sell)
        registerGlobalCommand("has_items_to_sell", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String retVar = args.get(0);

                if (Inventory.getEmptySlotsSize() == Inventory.MAX_SLOTS_SIZE) {
                    // player doesn't have any items yet
                    setValue(script, retVar, "1");
                }
                else {
                    setValue(script, retVar, "0");
                }
                return false;
            }
        });
        
        //show_shop_sell ##sell_item_id $$sell_item_name ##sell_item_price
        //##sell_item_id >0 = itemId
        //               -1 = player doesn't have any items yet
        //               -3 = canceled
        registerGlobalCommand("show_shop_sell", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String selectedItemIdVar = args.get(0);
                String selectedItemNameVar = args.get(1);
                String selectedItemSellPriceVar = args.get(2);

                if (Inventory.getEmptySlotsSize() == Inventory.MAX_SLOTS_SIZE) {
                    // player doesn't have any items yet
                    setValue(script, selectedItemIdVar, "-1");
                    setValue(script, selectedItemNameVar, "\"\"");
                    setValue(script, selectedItemSellPriceVar, "-1");
                    return false;
                }
                
                Item selectedItem = Inventory.showSelectItemDialog();
                
                if (selectedItem != null) {
                    setValue(script, selectedItemIdVar
                                                , "" + selectedItem.getId());
                    
                    setValue(script, selectedItemNameVar
                                        , "\"" + selectedItem.getName() + "\"");
                    
                    setValue(script, selectedItemSellPriceVar
                                                , "" + selectedItem.getSell());
                }
                else {
                    // canceled
                    setValue(script, selectedItemIdVar, "-3");
                    setValue(script, selectedItemNameVar, "\"\"");
                    setValue(script, selectedItemSellPriceVar, "-3");
                }
                return false;
            }
        });
    
        //show_shop_buy_weapon_armor ##buy_item_id $$buy_item_name
        //                   ##buy_item_price $$sell_item_name ##sell_item_price 
        //##buy_item_id >0 = itemId
        //              -1 = current equip cannot be sold
        //              -3 = canceled
        // Note.: used only for weapons, armor and shields
        registerGlobalCommand("show_shop_buy_weapon_armor", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String selectedItemIdVar = args.get(0);
                String selectedItemNameVar = args.get(1);
                String selectedItemBuyPriceVar = args.get(2);
                String selectedItemSellNameVar = args.get(3);
                String selectedItemSellPriceVar = args.get(4);

                Item selectedItem = Shop.showShopBuyItem();
                if (selectedItem != null) {
                    setValue(script, selectedItemIdVar
                                                , "" + selectedItem.getId());
                    
                    setValue(script, selectedItemNameVar
                                        , "\"" + selectedItem.getName() + "\"");
                    
                    setValue(script, selectedItemBuyPriceVar
                                                , "" + selectedItem.getBuy());
                    Item playerEquip = null;
                    switch (selectedItem.getType()) {
                        case WEAPON: 
                            playerEquip = Player.getWeapon();
                            break;
                        case ARMOR: 
                            playerEquip = Player.getArmor();
                            break;
                        case SHIELD: 
                            playerEquip = Player.getShield();
                            break;
                    }
                    if (playerEquip == null) {
                        setValue(script, selectedItemSellNameVar, "\"\"");
                        setValue(script, selectedItemSellPriceVar, "0");
                    }
                    else {
                        setValue(script, selectedItemSellNameVar
                                        , "\"" + playerEquip.getName() + "\"");

                        setValue(script, selectedItemSellPriceVar
                                                , "" + playerEquip.getSell());
                        
                        if (playerEquip != Item.EMPTY 
                                            && playerEquip.getSell() <= 0) {
                            
                            // current equip cannot be sold
                            setValue(script, selectedItemIdVar, "-1");
                        }
                    }
                }
                else {
                    // canceled
                    setValue(script, selectedItemIdVar, "-3");
                    setValue(script, selectedItemNameVar, "\"\"");
                    setValue(script, selectedItemBuyPriceVar, "-3");
                    setValue(script, selectedItemSellNameVar, "\"\"");
                    setValue(script, selectedItemSellPriceVar, "-3");
                }
                return false;
            }
        });
        

        //discard_item ##discard_item_id $$discard_item_name
        //##sell_item_id >0 = itemId
        //               -1 = player doesn't have any items yet
        //               -2 = can't discard the selected item (important item!)
        //               -3 = canceled
        //               -4 = cursed item !
        registerGlobalCommand("discard_item", new Command() {
            @Override
            public boolean execute(
                    Script script, List<String> args) throws Exception {
                
                String selectedItemIdVar = args.get(0);
                String selectedItemNameVar = args.get(1);

                Item selectedItem = Inventory.showSelectItemDialog();
                
                if (selectedItem != null) {
                    int itemId = selectedItem.getId();
                    if (Player.hasSpecificCurse(itemId)) {
                        itemId = -4;
                    }
                    else if (!selectedItem.isDisposable()) {
                        itemId = -2;
                    }
                    setValue(script, selectedItemIdVar, "" + itemId);
                    setValue(script, selectedItemNameVar
                                        , "\"" + selectedItem.getName() + "\"");
                }
                else {
                    // canceled
                    setValue(script, selectedItemIdVar, "-3");
                    setValue(script, selectedItemNameVar, "\"\"");
                }
                return false;
            }
        });
        
    }

    public abstract static class Command {
        
        public abstract boolean execute(
                Script script, List<String> args) throws Exception;
        
        public void nextLineNumber(Script script) {
            script.instructionPointer++;
        }
        
    }
    
    public static class ScriptLine {
        public int lineNumber;
        public String command;
        public List<String> args = new ArrayList<>();
    }

    public static Map<String, Object> getLocalVars() {
        return VARS;
    }
    
    public static boolean isValidVar(String varName) {
        return (varName.startsWith("#") || varName.startsWith("##")
                || varName.startsWith("$") || varName.startsWith("$$")
                || varName.startsWith("@@")) && varName.length() > 2;
    }

    public static boolean isLocalVar(String varName) {
        return (varName.startsWith("#") && !varName.startsWith("##"))
                || (varName.startsWith("$") && !varName.startsWith("$$"));
    }

    public static boolean isGlobalVar(String varName) {
        return varName.startsWith("##") || varName.startsWith("$$");
    }

    public static boolean isTextsVar(String varName) {
        return varName.startsWith("@@");
    }

    public static boolean isNumberVar(String varName) {
        return varName.startsWith("#");
    }

    public static boolean isTextVar(String varName) {
        return varName.startsWith("$") || varName.startsWith("@");
    }
    
    public static Object getGlobalValue(String varName) {
        return VARS.get(varName);
    }

    public static void setGlobalValue(String varName, Object varValue) {
        VARS.put(varName, varValue);
    }

    public static Map<String, Object> getVARS() {
        return VARS;
    }
    
    public static void setValue(
            Script script, String varName, String arg) throws Exception {
        
        if (!isValidVar(varName)) {
            throw new Exception("Invalid variable '" + varName 
                    + "' at line " + script.getLineNumber());
        }
        Object value = getArgument(script, arg);
        if ((isNumberVar(varName) && !(value instanceof Integer))
                || (isTextVar(varName) && !(value instanceof String))) {
            
            throw new Exception("Type mismatch for variable '" + varName 
                    + "' at line " + script.getLineNumber());
        }
        if (isLocalVar(varName)) {
            script.getLocalVars().put(varName, value);
        }
        else {
            VARS.put(varName, value);
        }
    }

    public static Object getArgument(
            Script script, String arg) throws Exception {
        
        if (!isValidVar(arg)) {
            if (Character.isDigit(arg.charAt(0))|| (arg.charAt(0) == '-' 
                                        && Character.isDigit(arg.charAt(1)))) {
                try {
                    return Integer.valueOf(arg);
                }
                catch (Exception e) {
                    throw new Exception("Invalid literal number '" + arg 
                            + "' at line " + script.getLineNumber());
                }
            }
            else if (arg.charAt(0) == '"') {
                return arg.substring(1, arg.length() - 1);
            }
            else {
                throw new Exception("Invalid argument value '" + arg 
                        + "' at line " + script.getLineNumber());
            }
        }
        Object value;
        if (isLocalVar(arg)) {
            value = script.getLocalVars().get(arg);
        }
        else if (isGlobalVar(arg)) {
             value = VARS.get(arg);
        }
        else {
            value = Game.getTexts() != null ? Game.getTexts().get(arg) : null;
        }
        if (value == null) {
            throw new Exception("Variable '" + arg + "' doesn't exist at line " 
                    + script.getLineNumber());
        }
        return value;
    }

    public static void registerGlobalCommand(String name, Command command) {
        GLOBAL_COMMANDS.put(name, command);
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface ScriptCommand {
        public String name();
    }
    
    // will register static class methods marked 
    // with @ScriptCommand as global commands
    public static void registerClassStaticCommands(Class registerClass) {
        for(Method method : registerClass.getDeclaredMethods()) {
            boolean annotated = false;
            String commandName = null;
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation instanceof ScriptCommand) {
                    // System.out.println("Annotation: " + annotation);
                    commandName = ((ScriptCommand) annotation).name();
                    annotated = true;
                    break;
                }
            }
            if (!annotated) {
                continue;
            }
            Command command = new Command() {
                final Method classMethod = method;
                final Object[] cmdArgs = new Object[method.getParameterCount()];
                
                @Override
                public boolean execute(
                        Script script, List<String> args) throws Exception {
                    
                    for (int i = 0; i < cmdArgs.length; i++) {
                        cmdArgs[i] = getArgument(script, args.get(i));
                    }
                    Object ret = classMethod.invoke(null, cmdArgs);
                    if (ret instanceof Boolean) {
                        return (Boolean) ret;
                    }
                    else {
                        return false;
                    }
                }
            };
            registerGlobalCommand(commandName, command);
        }
    }
    
    private Map<String, Integer> labels = new HashMap<String, Integer>();
    private final List<ScriptLine> lines = new ArrayList<>();
    private final Map<String, Command> localCommands = new HashMap<>();
    private int instructionPointer;

    public Script(String script) {
        ByteArrayInputStream bais = new ByteArrayInputStream(script.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(bais));
        parse(br);
    }
    
    public Script(BufferedReader br) {
        parse(br);
    }

    private void parse(BufferedReader br) {
        try {
            int lineNumberTmp = 0;
            int instructionPointerTmp = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(";")) {
                    lineNumberTmp++;
                    continue;
                }
                //System.out.print(line + " tokens: ");
                String tokens[] = line.split("\\ ");
                if (tokens[0].toLowerCase().equals("script_end")) {
                    return;
                }
                ScriptLine scriptLine = new ScriptLine();
                scriptLine.lineNumber = lineNumberTmp + 1;
                boolean stringStarted = false;
                String stringLiteral = "";
                for (int ti = 0; ti < tokens.length; ti++) {
                    String token = tokens[ti];
                    if (stringStarted && token.endsWith("\"")) {
                        stringLiteral += " " + token;
                        scriptLine.args.add(stringLiteral);
                        stringStarted = false;
                    }
                    else if (stringStarted) {
                        stringLiteral += " " + token;
                    }
                    else if (!stringStarted && token.startsWith("\"")) {
                        stringLiteral = token;
                        stringStarted = true;
                        if (token.endsWith("\"")) {
                            scriptLine.args.add(stringLiteral);
                            stringStarted = false;
                        }
                    }
                    else if (ti == 0 && token.endsWith(":")) {
                        // register label
                        String label = token.substring(0, token.length() - 1);
                        labels.put(label, instructionPointerTmp);
                    }
                    else if (scriptLine.command == null) {
                        scriptLine.command = token.toLowerCase();
                    }
                    else if (token.trim().isEmpty()) {
                        continue;
                    }
                    else {
                        scriptLine.args.add(token);
                    }
                    //System.out.print(token + ", ");
                }
                //System.out.println();
                if (scriptLine.command == null) {
                    scriptLine.command = "nop";
                }
                lines.add(scriptLine);
                lineNumberTmp++;
                instructionPointerTmp++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Script.class.getName())
                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
    }

    public Map<String, Command> getLocalCommands() {
        return localCommands;
    }

    public int getLineNumber() {
        return instructionPointer;
    }
    
    private final Set<String> removeLocalVars = new HashSet<>();
    
    public void clearLocalVars() {
        removeLocalVars.clear();
        for (String var : VARS.keySet()) {
            if (isLocalVar(var)) {
                removeLocalVars.add(var);
            }
        }
        for (String localVar : removeLocalVars) {
            VARS.remove(localVar);
        }
    }
    
    public boolean execute(String label) throws Exception {
        instructionPointer = 0;
        if (label != null) {
            Integer ip = labels.get(label);
            if (ip == null) {
                return false;
            }
            instructionPointer = ip;
        }
        while (instructionPointer >= 0 && instructionPointer < lines.size()) {
            ScriptLine line = lines.get(instructionPointer);
            if ("ret".equals(line.command)) {
                if (line.args.isEmpty()) return false;
                return 1 == (Integer) getArgument(this, line.args.get(0));
            }
            
            Command command = localCommands.get(line.command);
            if (command == null) {
                command = GLOBAL_COMMANDS.get(line.command);
            }
            
            if (command == null) {
                throw new Exception("Invalid command '" + line.command 
                        + "' at line " + line.lineNumber);
            }
            try {
                boolean exitMap = command.execute(this, line.args);
                if (exitMap) {
                    return true;
                }
            }
            catch (Exception e) {
                throw new Exception("Error executing command '" + line.command 
                        + "' at line " + line.lineNumber, e);
            }
            command.nextLineNumber(this);
        }
        return false;
    }
    
    public static boolean saveVars(int fileIndex) {
        String userDir = System.getProperty("user.home");
        File saveFile = new File(userDir + "/" + "save_" + fileIndex + ".dat");
        try (FileOutputStream fos = new FileOutputStream(saveFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos);) {
            
            oos.writeObject(VARS);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    // fileIndex must be 1, 2 or 3 in this implementation.
    public static Map<String, Object> loadVars(int fileIndex) {
        Map<String, Object> globalVars = null;
        String userDir = System.getProperty("user.home");
        File openFile = new File(userDir + "/" + "save_" + fileIndex + ".dat");
        try (FileInputStream fis = new FileInputStream(openFile);
             ObjectInputStream ois = new ObjectInputStream(fis);) {
            
            globalVars = (Map) ois.readObject();
        } catch (Exception ex) {
            return null;
        }
        return globalVars;
    }
    
    public static void main(String[] args) throws Exception {
        Color c = Util.getColor("0xff0000c0");
        System.out.println(c);
//        InputStream is 
//                = Script.class.getResourceAsStream("/res/script_test.scr");
//        
//        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//        Script sp = new Script(br);
//        br.close();
//        sp.execute(null);
    }
    
}
