package com.run.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Created by Valentyn.Polishchuk on 12/26/13
 */
public class ScriptRunLua {

    public static void main(String[] args) {
        ScriptRunLua lua = new ScriptRunLua();
        lua.run();
    }

    public void run() {
        String script = "lua/hello.lua";
        LuaValue luaValueG = JsePlatform.standardGlobals();
        luaValueG.get("dofile").call(LuaValue.valueOf(script));
    }

    public void run1() {
        //run the lua script defining your function
        LuaValue _G = JsePlatform.standardGlobals();
        _G.get("dofile").call( LuaValue.valueOf("test.lua"));

        //call the function MyAdd with two parameters 5, and 5
        LuaValue MyAdd = _G.get("MyAdd");
        LuaValue retvals = MyAdd.call(LuaValue.valueOf(5), LuaValue.valueOf(5));

        //print out the result from the lua function
        System.out.println(retvals.tojstring(1));
    }
}
