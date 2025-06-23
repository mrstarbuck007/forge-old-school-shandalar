package forge.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import forge.Forge;
import forge.GuiMobile;
import forge.adventure.player.AdventurePlayer;
import forge.adventure.world.WorldSave;
import forge.gui.GuiBase;
import forge.model.FModel;
import forge.util.Localizer;
import org.lwjgl.system.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

public class EditSave extends ApplicationAdapter {
    private int saveId;

    public static void main(String[] args) {
        int saveId = Integer.parseInt(args[0]);

        Localizer.getInstance().initialize("en-US", "res/languages/");
        GuiBase.setInterface(new GuiMobile(Files.exists(Paths.get("./res"))?"./":"../forge-gui/"));

        // This should fix MAC-OS startup without the need for -XstartOnFirstThread parameter
        if (SharedLibraryLoader.isMac) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        FModel.initialize(null, null);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(new EditSave(saveId), config);
    }

    public EditSave(int saveId) {
        this.saveId = saveId;
    }

    @Override
    public void create() {
        if (WorldSave.load(saveId)) {
            AdventurePlayer player = WorldSave.getCurrentSave().getPlayer();
            System.out.println(player);
        }
    }


}
