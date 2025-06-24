package forge.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Clipboard;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import forge.Forge;
import forge.adventure.data.AdventureQuestData;
import forge.adventure.data.AdventureQuestStage;
import forge.adventure.player.AdventurePlayer;
import forge.adventure.util.SaveFileData;
import forge.adventure.world.WorldSave;
import forge.adventure.world.WorldSaveHeader;
import forge.model.FModel;
import forge.util.Localizer;
import org.lwjgl.system.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class EditSave extends ApplicationAdapter {
    private final int saveId;
    private final byte clearFlagValue;
    private final byte travelFlagValue;
    private final byte isDryRun;
    private final String clearStatus;
    private final String travelStatus;

    public static void main(String[] args) {
        int saveId = Integer.parseInt(args[0]);
        byte clearFlagValue = Byte.parseByte(args[1]);
        byte travelFlagValue = Byte.parseByte(args[2]);
        String clearStatus = args[3];
        String travelStatus = args[4];
        byte isDryRun = Byte.parseByte(args[5]);

        // This should fix MAC-OS startup without the need for -XstartOnFirstThread parameter
        if (SharedLibraryLoader.isMac) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        String assetsDir = Files.exists(Paths.get("./res")) ? "./" : "../forge-gui/";
        Localizer.getInstance().initialize("en-US", assetsDir + "res/languages/");
        String switchOrientationFile = assetsDir + "switch_orientation.ini";

        ApplicationListener start = Forge.getApp(new EditSave(saveId, clearFlagValue, travelFlagValue, isDryRun, clearStatus, travelStatus), new Lwjgl3Clipboard(), new Main.DesktopAdapter(switchOrientationFile),
                assetsDir, false, false, 0, false, 0, "", "");

        FModel.initialize(null, null);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(start, config);
    }

    public EditSave(int saveId, byte clearFlagValue, byte travelFlagValue, byte isDryRun, String clearStatus, String travelStatus) {
        this.saveId = saveId;
        this.clearFlagValue = clearFlagValue;
        this.travelFlagValue = travelFlagValue;
        this.isDryRun = isDryRun;
        this.clearStatus = clearStatus;
        this.travelStatus = travelStatus;
    }

    @Override
    public void create() {
        WorldSaveHeader header;
        AdventurePlayer player = new AdventurePlayer();
        SaveFileData mainData;

        String fileName = WorldSave.getSaveFile(saveId);
        if(!new File(fileName).exists())
            return;
        try {
            try(FileInputStream fis  = new FileInputStream(fileName);
                InflaterInputStream inf = new InflaterInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(inf))
            {
                header = (WorldSaveHeader) ois.readObject();
                mainData = (SaveFileData) ois.readObject();
                player.load(mainData.readSubData("player"));
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println(player.toJson());

        for (AdventureQuestData quest : player.getQuests()) {
            if (quest.getID() == 36) {
                for (AdventureQuestStage stage : quest.stages) {
                    if (stage.name.equals("Clear")) {
                        stage.mapFlagValue = clearFlagValue;
                        stage.setStatus(clearStatus);
                    }
                    else if (stage.name.equals("Travel")) {
                        stage.mapFlagValue = travelFlagValue;
                        stage.setStatus(travelStatus);
                    }
                }
                break;
            }
        }

        System.out.println(player.toJson());

        if (isDryRun != 0) {
            System.out.println("Dry Run - exiting");
            return;
        }
        System.out.println("Saving file");

        try {
            try(FileOutputStream fos =  new FileOutputStream(fileName);
                DeflaterOutputStream def= new DeflaterOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(def))
            {
                oos.writeObject(header);
                mainData.store("player",player.save());

                oos.writeObject(mainData);
            }

            System.out.println("Success!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
