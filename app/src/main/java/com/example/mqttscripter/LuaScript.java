package com.example.mqttscripter;


import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;


public class LuaScript {
    public final String fileName;
    public final String content;
    public final Exception error;

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

    public void execute() {
        if (content == null || error != null) return;

        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.load(content);
            chunk.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
