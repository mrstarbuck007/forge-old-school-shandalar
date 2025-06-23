package forge.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Clipboard;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import forge.Forge;
import forge.adventure.player.AdventurePlayer;
import forge.adventure.util.SaveFileData;
import forge.adventure.world.WorldSave;
import forge.adventure.world.WorldSaveHeader;
import forge.model.FModel;
import forge.util.Localizer;
import org.lwjgl.system.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.InflaterInputStream;

public class EditSave extends ApplicationAdapter {
    private int saveId;

    public static void main(String[] args) {
        int saveId = Integer.parseInt(args[0]);

        // This should fix MAC-OS startup without the need for -XstartOnFirstThread parameter
        if (SharedLibraryLoader.isMac) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        Localizer.getInstance().initialize("en-US", "./res/languages/");
        String assetsDir = Files.exists(Paths.get("./res")) ? "./" : "../forge-gui/";
        String switchOrientationFile = assetsDir + "switch_orientation.ini";

        ApplicationListener start = Forge.getApp(new EditSave(saveId), new Lwjgl3Clipboard(), new Main.DesktopAdapter(switchOrientationFile),
                assetsDir, false, false, 0, false, 0, "", "");

        FModel.initialize(null, null);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(start, config);
    }

    public EditSave(int saveId) {
        this.saveId = saveId;
    }

    @Override
    public void create() {
        WorldSaveHeader header = new WorldSaveHeader();
        AdventurePlayer player = new AdventurePlayer();

        String fileName = WorldSave.getSaveFile(saveId);
        if(!new File(fileName).exists())
            return;
        try {
            try(FileInputStream fos  = new FileInputStream(fileName);
                InflaterInputStream inf = new InflaterInputStream(fos);
                ObjectInputStream oos = new ObjectInputStream(inf))
            {
                header = (WorldSaveHeader) oos.readObject();
                SaveFileData mainData=(SaveFileData)oos.readObject();
                player.load(mainData.readSubData("player"));

                System.out.println(player.toJson());
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }


}
