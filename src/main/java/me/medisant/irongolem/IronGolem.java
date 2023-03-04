package me.medisant.irongolem;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.medisant.irongolem.config.ModConfig;
import me.medisant.irongolem.skinchecker.SkinChecker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import nu.pattern.OpenCV;
import org.apache.commons.compress.utils.Lists;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Getter
public class IronGolem implements ModInitializer {

    private static IronGolem instance;
    private final Map<String, Mat> models = Maps.newHashMap(); // path, model
    private final List<String> checkedHashes = Lists.newArrayList();
    private SkinChecker skinChecker;
    private File modelsDir;
    private File modDir;

    public static IronGolem getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        OpenCV.loadLocally();
        init();
        this.registerCommands();
        ModConfig.init();
    }

    public void init() {
        instance = this;
        skinChecker = new SkinChecker(this);
        this.initFileSystem();
        this.loadModels();
        this.checkedHashes.clear();
    }

    private void initFileSystem() {
        File gameDir = FabricLoader.getInstance().getGameDir().toFile();
        this.modDir = new File(gameDir, "irongolem");
        this.modelsDir = new File(modDir, "models");
        try {
            if (!modelsDir.exists()) Files.createDirectories(modelsDir.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(
                ((dispatcher, registryAccess) -> dispatcher.register(
                        literal("irongolem")
                                .then(literal("init")
                                        .executes(context -> {
                                            IronGolem.getInstance().init();
                                            if (MinecraftClient.getInstance().player != null) {
                                                MinecraftClient.getInstance().player.sendMessage(Text.translatable("irongolem.message.init"));
                                            }

                                            return 1;
                                        }))
                                .then(literal("openFolder")
                                        .executes(context -> {
                                            Util.getOperatingSystem().open(this.getModDir());
                                            return 1;
                                        }))
                                .executes(context -> {
                                    // TODO open GUI
                                    System.out.println("ok");
                                    return 1;
                                })
                ))
        );
    }

    private void loadModels() {
        this.models.clear();
        for (File file : Objects.requireNonNull(this.modelsDir.listFiles())) {
            Mat mat = Imgcodecs.imread(file.getPath());
            this.models.putIfAbsent(file.getName(), mat);
        }
    }

    public void logSkinCheckerResult(String log) {
        File logFile = new File(this.modDir, "log.txt");
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(logFile, true));
            writer.write(log + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
