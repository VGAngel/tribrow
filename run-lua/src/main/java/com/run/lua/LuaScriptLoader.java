package com.run.lua;

//import org.keplerproject.luajava.LuaObject;
//import org.keplerproject.luajava.LuaState;
//import org.keplerproject.luajava.LuaStateFactory;

public class LuaScriptLoader {
//
//    private LuaState luaState;
//
//    public LuaScriptLoader(String fileFullName) {
//        luaState = LuaStateFactory.newLuaState();
//        luaState.openLibs();
//        luaState.LdoFile(fileFullName);
//    }
//
//    public void closeScript() {
//        luaState.close();
//    }
//
//    /**
//     * Метод, вызывающий Lua-функцию getRoomDescription, которая возвращает описание комнаты
//     *
//     * @return строка описания комнаты
//     */
//    public String getRoomDescription() {
//        luaState.getGlobal("getRoomDescription");
//        luaState.call(0, 1);
//        LuaObject lo = luaState.getLuaObject(1);
//        luaState.pop(1);
//        return lo.getString();
//    }
//
//    /**
//     * Метод, вызывающий определенную функцию в Lua-скрипте
//     *
//     * @param functionName - имя функции
//     * @param adapter      - объект LuaAdapter, описывающий параметры вызова функции
//     */
//    public void runScriptFunction(String functionName, LuaAdapter adapter) {
//        luaState.getGlobal(functionName);
//        luaState.pushJavaObject(adapter);
//        luaState.call(1, 0);
//    }
}