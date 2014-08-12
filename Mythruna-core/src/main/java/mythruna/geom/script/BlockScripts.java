package mythruna.geom.script;

import java.io.PrintStream;
import javax.script.Bindings;
import mythruna.script.ScriptManager;

public class BlockScripts
{
    public BlockScripts()
    {
    }

    public static void initialize()
    {
        ScriptManager scripts = new ScriptManager(new Object[0]);
        scripts.setScriptExtension("blocks.groovy");

        scripts.addScript("/mythruna/geom/script/block-library.groovy");
        scripts.addScript("/scripts/blocks/default.groovy");

        scripts.initialize();

        System.out.println("Resulting bindings:" + scripts.getBindings().entrySet());
    }
}