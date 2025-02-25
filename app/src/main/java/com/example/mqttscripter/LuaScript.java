package com.example.mqttscripter;


import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.Map;


public class LuaScript {
    private final String fileName;
    private final String content;
    private final Exception error;

    private MQTTConsole mqttConsole = null;


    public LuaScript(String fileName, String content, Exception error) {
        this.fileName = fileName;
        this.content = content;
        this.error = error;
    }

    public LuaScript(String content){
        fileName = "";
        error = null;
        this.content = content;
    }

    public String getFileName(){
        return fileName;
    }

    public void setMqttConsole(MQTTConsole mqttConsole){
        this.mqttConsole = mqttConsole;
    }

    public void execute(Map<String, LuaValue> globalValues) {
        if (content == null || error != null) return;

        try {
            Globals globals = JsePlatform.standardGlobals();

            globals.set("print", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i <= args.narg(); i++) {
                        if (i > 1) sb.append("\t");
                        sb.append(args.arg(i).toString());
                    }
                    mqttConsole.print(sb.toString());
                    return LuaValue.NONE;
                }
            });

            if (globalValues != null) {
                for (Map.Entry<String, LuaValue> entry : globalValues.entrySet()) {
                    globals.set(entry.getKey(), entry.getValue());
                }
            }

            LuaValue chunk = globals.load(content);
            chunk.call();
        } catch (Exception e) {
            if(mqttConsole != null)
                mqttConsole.print(e.toString());
        }
    }
}
